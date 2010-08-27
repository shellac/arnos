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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @Autowired(required = false)
    public transient CacheHandlerInterface cacheHandler;

    /**
     * Primary SPARQL Endpoint of the arnos service. Runs the provided
     * query over all defined endpoints for that project.
     * @param projectName Name of project
     * @param query SPARQL Query
     * @param writer Writer to send results to
     */
    @RequestMapping(value = "/{projectName}/query")
    public final void executeQueryAcrossAllEndpoints(@PathVariable final String projectName,
                                                 @RequestParam("query") final String query,
                                                 final java.io.Writer writer)
    {
        checkProject(projectName);

        List<Endpoint> endpoints = manager.getEndpoints(projectName);

        String result = handleQuery(projectName, query, endpoints);

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
    @RequestMapping(value = "/{projectName}/{endpointList}/query")
    public final void executeGetQuery(@PathVariable final String projectName,
                                                @PathVariable final String endpointList,
                                                 @RequestParam("query") final String query,
                                                 final java.io.Writer writer)
    {
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
                    endpointSubset.add(ep);
                }
            }
        }

        String result = handleQuery(projectName, query, endpointSubset);

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
    @RequestMapping(value = "/{projectName}/{endpointList}/query", method = RequestMethod.POST)
    public final void executePostQuery(@PathVariable final String projectName,
                                                 @PathVariable final String endpointList,
                                                 @RequestParam("query") final String query,
                                                 final java.io.Writer writer)
    {
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
                    endpointSubset.add(ep);
                }
            }
        }

        String result;
        if (endpointSubset.size() != 1)
        {
            result = "<error>POST method should only be made to a single endpoint</error>";
        }
        else if (isUpdateQuery(query))
        {
            Endpoint ep = endpointSubset.get(0);
            logger.info("QueryType is SPARQL UPDATE");
            result = queryHandler.handleUpdate(projectName, query, ep);
            // clear the cache elements for this project
            if (cacheHandler != null)
            {
                logger.debug("Update invalidates cache. Flushing...");
                cacheHandler.flush(projectName, ep);
            }
        }
        else
        {
            result = "<error>Incorrect http method used</error>";
            logger.error(result);
        }

        try
        {
            writer.append(result);
            writer.flush();
        }
        catch (Exception e)
        {
            logger.error("Unable to write output", e);
        }

        // flush cache as an update has been made to endpoints
        // could this be limited to specific endpoint?
    }

    /**
     * This implementation, simple contatinates all query results.
     * @param queryString SPARQL query to execute
     * @param endpoints List of endpoint urls to run the query against
     * @return An RDF model
     */
    private final String handleQuery(final String project, final String queryString, List<Endpoint> endpoints)
    {
        if (queryString == null) return "";

        String cacheString = generateCacheKey(project, queryString, endpoints);

        if (cacheHandler != null && cacheHandler.contains(project, cacheString) )
        {
            logger.debug("Fetching result from cache");
            return cacheHandler.get(project, cacheString);
        }

        String result = "";

        logger.info("Cache miss");

        logger.debug("Querying against " + endpoints.size() + " endpoints");

        // process the SPARQL query to best determin how to handle this query
        try
        {
            Query query = QueryFactory.create(queryString);

            if (query.getQueryType() == Query.QueryTypeSelect)
            {
                result = queryHandler.handleSelect(project, query, endpoints);
            }
            else if (query.getQueryType() == Query.QueryTypeConstruct)
            {
                result = queryHandler.handleConstruct(project, query, endpoints);
            }
            else if (query.getQueryType() == Query.QueryTypeAsk)
            {
                result = queryHandler.handleAsk(project, query, endpoints);
            }
            else
            {
                result = queryHandler.handleDescribe(project, query, endpoints);
            }

            // put this result into the cache if available
            if (cacheHandler != null)
            {
                logger.debug("Caching result");
                cacheHandler.put(project, endpoints, cacheString, result);
            }
        }
        catch (QueryParseException qpe)
        {
            logger.error(qpe.getMessage());
            result = "<error>Unknown query type</error>";
        }

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

    /**
     * Generates a unique key for this request.
     * @param project
     * @param queryString
     * @param endpoints
     * @return
     */
    private String generateCacheKey(final String project, final String queryString, List<Endpoint> endpoints)
    {
        // generate the cache key for this query
        String cacheString = queryString;

        // sort the list so that caching is order-independent
        Collections.sort(endpoints);

        for (Endpoint e : endpoints) { cacheString += e.getIdentifier(); }

        return cacheString;
    }
}
