/*
 * Copyright (c) 2009, University of Bristol
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3) Neither the name of the University of Bristol nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.wf.arnos.controller;

import java.util.List;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.ProjectsManager;
import org.wf.arnos.exception.ResourceNotFoundException;
import org.wf.arnos.logger.Logger;
import org.wf.arnos.queryhandler.QueryHandlerInterface;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
@Controller
public class QueryController
{
    /**
     * Logger.
     */
    @Logger
    public static transient Log logger;

    /**
     * The projects manager, autowired in.
     */
    @Autowired
    public transient ProjectsManager manager;

    /**
     * The query handler.
     */
    @Autowired
    public transient QueryHandlerInterface queryHandler;

    /**
     * The cache handler, autowired in.
     */
    public transient CacheHandlerInterface cacheHandler;

    /**
     * Primary SPARQL Endpoint of the arnos service. Runs the provided
     * query over all defined endpoints for that project.
     * @param projectName Name of project
     * @param query SPARQL Query
     * @param writer Writer to send results to
     */
    @RequestMapping(value = "/{projectName}/query")
    public final void executeQuery(@PathVariable final String projectName,
                                                 @RequestParam("query") final String query,
                                                 final java.io.Writer writer)
    {
        checkProject(projectName);

        List<Endpoint> endpoints = manager.getEndpoints(projectName);

        String result = handleQuery(query, endpoints);

        try
        {
            writer.append(result);
            writer.flush();
        }
        catch (Exception e)
        {
            logger.error("Unable to write output", e);
        }
    }

    /**
     * Runs the provided query over endpoints listed in the endpoints variable.
     * @param projectName Name of project
     * @param endpointList set of endpoint ids seperated by plus symbol. E.g. id1+id2+id3
     * @param query SPARQL Query
     * @param writer Writer to send results to
     */
    @RequestMapping(value = "/{projectName}/{endpoints}/query")
    public final void executeQuery(@PathVariable final String projectName,
                                                @PathVariable final String endpointList,
                                                 @RequestParam("query") final String query,
                                                 final java.io.Writer writer)
    {
        System.out.println("endpointList:" + endpointList);
        checkProject(projectName);

        List<Endpoint> endpoints = manager.getEndpoints(projectName);

        List<Endpoint> endpointSubset = new ArrayList();

        String [] endpointIDs = endpointList.split("\\+");

        for(String id : endpointIDs)
        {
            for (Endpoint ep : endpoints)
            {
                if (ep.getIdentifier().equals(id))
                {
                    System.out.println("Adding id");
                    endpointSubset.add(ep);
                }
            }
        }

        String result = handleQuery(query, endpointSubset);

        try
        {
            writer.append(result);
            writer.flush();
        }
        catch (Exception e)
        {
            logger.error("Unable to write output", e);
        }
    }

    /**
     * This implementation, simple contatinates all query results.
     * @param queryString SPARQL query to execute
     * @param endpoints List of endpoint urls to run the query against
     * @return An RDF model
     */
    private final String handleQuery(final String queryString, List<Endpoint> endpoints)
    {
        String result = null;

        // generate the cache key for this query
        String cacheString = queryString;

        // sort the list so that caching is order-independent
        Collections.sort(endpoints);

        for (Endpoint e : endpoints) { cacheString += e.getIdentifier(); }

        if (queryString == null) return "";

        if (cacheHandler != null)
        {
            logger.debug("Fetching result from cache");
            result = cacheHandler.get(cacheString);
        }
        
        if (result == null)
        {
            logger.info("Cache miss. Passing to " + queryHandler.getClass());

            logger.debug("Querying against " + endpoints.size() + " endpoints");

            // process the SPARQL query to best determin how to handle this query
            try
            {
                Query query = QueryFactory.create(queryString);

                logger.debug("Query type:"+ query.getQueryType());

                if (query.getQueryType() == Query.QueryTypeSelect)
                {
                    result = queryHandler.handleSelect(query, endpoints);
                }
                else if (query.getQueryType() == Query.QueryTypeConstruct)
                {
                    result = queryHandler.handleConstruct(query, endpoints);
                }
                else if (query.getQueryType() == Query.QueryTypeAsk)
                {
                    result = queryHandler.handleAsk(query, endpoints);
                }
                else if (query.getQueryType() == Query.QueryTypeDescribe)
                {
                    result = queryHandler.handleDescribe(query, endpoints);
                }
                else
                {
                    logger.warn("Unknown query type");
                }

                // put this result into the cache if available
                if (cacheHandler != null)
                {
                    logger.debug("Caching result");
                    cacheHandler.put(cacheString, result);
                }
            }
            catch (QueryParseException qpe)
            {
                if (isUpdateQuery(queryString))
                {
                    logger.info("QueryType is SPARQL UPDATE");
                    if (endpoints.size() != 1) return "Error: UPDATE Query can only run over a single endpoint";
                    result = queryHandler.handleUpdate(queryString, endpoints.get(0));
                }
                else
                {
                    logger.error(qpe.getMessage());
                    result = "";
                }
            }
        } // END if (result == null)

        return result;
    }


    /**
     * Checks for the existance of the project, throwing a runtime exception
     * if not found.
     * @param projectName Name of project, provided from request
     */
    private void checkProject(final String projectName)
    {
        if (StringUtils.isEmpty(projectName))
        {
            if (logger.isDebugEnabled()) logger.debug("Missing project name");
            throw new ResourceNotFoundException();
        }

        if (manager.hasProject(projectName))
        {
            return;
        }

        if (logger.isDebugEnabled()) logger.debug("Listing failed, no project named '" + projectName + "'");
        throw new ResourceNotFoundException();
    }

    /**
     * Checks if given query is an update query
     * @param s
     * @return <code>true</code> if query is a sparql update, <code>false</code> otherwise
     */
    private boolean isUpdateQuery(String s)
    {
        ArrayList <String>commands = new ArrayList<String>();
        commands.add("MODIFY");
        commands.add("INSERT");
        commands.add("LOAD");
        commands.add("DELETE");
        commands.add("CREATE");
        commands.add("DROP");
        commands.add("CLEAR");

        for (String command : commands)
        {
            if (s.contains(command)) return true;
        }

        return false;
    }
}
