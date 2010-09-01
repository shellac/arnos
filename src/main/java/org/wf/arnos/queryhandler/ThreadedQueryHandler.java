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
package org.wf.arnos.queryhandler;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.sparql.Result;
import org.wf.arnos.queryhandler.task.FetchBooleanResponseTask;
import org.wf.arnos.queryhandler.task.FetchModelResponseTask;
import org.wf.arnos.queryhandler.task.FetchResultSetResponseTask;
import org.wf.arnos.queryhandler.task.FetchUpdateResponseTask;

/**
 * A query handler that uses multithreading to handle endpoint quering.
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ThreadedQueryHandler implements QueryHandlerInterface
{
    /**
     * The cache handler, autowired in.
     */
    @Autowired(required = false)
    private transient CacheHandlerInterface cacheHandler;

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ThreadedQueryHandler.class);

    /**
     * Default length for results stringbuffer constructor.
     */
    private static final int DEFAULT_SB_LENGTH = 133;

    /**
     * Spring's taskexecutor for handling threads.
     */
    @Autowired
    private transient ThreadPoolTaskExecutor taskExecutor;

    /**
     * Sets the task executor.
     * @param paramTaskExecutor A given task executor
     */
    public final void setTaskExecutor(final ThreadPoolTaskExecutor paramTaskExecutor)
    {
        this.taskExecutor = paramTaskExecutor;
    }

    /**
     * Public accessor for cache (if present).
     * @return CacheHandler supplied by spring, or <code>null</code> otherwise
     */
    public final CacheHandlerInterface getCache()
    {
        return cacheHandler;
    }

    /**
     * Sets the cache handler.
     * @param cache Cache implementing the CacheHandlerInterface
     */
    public final void setCache(final CacheHandlerInterface cache)
    {
        this.cacheHandler = cache;
    }

    /**
     * Check to see if cache has been set.
     * @return Boolean, <code>true</code> if a cache exists, <code>false</code> otherwise
     */
    public final boolean hasCache()
    {
        return cacheHandler != null;
    }


    /**
     * Handles the federated CONSTRUCT sparql query across endpoints.
     * @param query SPARQL CONSTRUCT query
     * @param endpoints List of endpoints to conduct query accross
     * @return Result as an xml string
     */
    public final String handleConstruct(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        LOG.info("handling CONSTRUCT");

        Model model = fetchModelsAndWait(projectName, query, endpoints);
        return mergeModelResults(model, query);
    }

    /**
     * This method handles a SELECT SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL SELECT query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    public final String handleSelect(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        LOG.info("handling SELECT");

        List<Result> selectResultList = fetchResultSetAndWait(projectName, query, endpoints);

        StringBuffer content = new StringBuffer(DEFAULT_SB_LENGTH);

        content.append("<?xml version=\"1.0\"?><sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"><head>");

        // add head info
        List<String> vars = query.getResultVars();
        for (String var : vars)
        {
            content.append("<variable name=\"");
            content.append(Result.escapeXMLEntities(var));
            content.append("\"/>");
        }
        content.append("</head><results>");

        // collate all responses
        boolean hasLimit = false;
        boolean distinct = false;
        long limit = -1;

        if (query.hasLimit())
        {
            limit = query.getLimit();
            hasLimit = true;
        }

        if (query.isDistinct())
        {
            distinct = true;
        }

        if (query.hasOrderBy())
        {
            sortResults(selectResultList, query.getOrderBy());
        }

        for (int i = 0; i < selectResultList.size(); i++)
        {
            Result r = selectResultList.get(i);
            boolean add = true;

            if (!hasLimit || limit > 0)
            {
                if (hasLimit) limit--;
            }
            else
            {
                add = false;
            }

            if (distinct)
            {
                // check a duplicate result hasn't already been added
                boolean match = false;
                for (int j = 0; j < i; j++)
                {
                    if (r.equals(selectResultList.get(j))) match = true;
                }
                if (match) add = false;
            }
System.out.println(r.toXML());
            if (add) content.append(r.toXML());
        }

        content.append("</results></sparql>");

        selectResultList.clear();

        return content.toString();
    }

    /**
     * This method handles a ASK SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL ASK query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    public final String handleAsk(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        LOG.info("handling ASK");

        List <Boolean> askResultList = new LinkedList<Boolean>();
        CountDownLatch doneSignal = new CountDownLatch(endpoints.size());

        // fire off a thread to handle quering each endpoint
        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);

            taskExecutor.execute(new FetchBooleanResponseTask(this, askResultList, query.serialize(), url, projectName, doneSignal));
        }

        // block until all threads have finished
        try
        {
            doneSignal.await();
        }
        catch (InterruptedException ex)
        {
            LOG.warn("Error while waiting on threads", ex);
        }

        // once threads have compeleted, construct results
        LOG.debug("Threads completed, constructing results");

        StringBuffer content = new StringBuffer(DEFAULT_SB_LENGTH);

        content.append("<?xml version=\"1.0\"?><sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"><head></head>");

        // calculate the response to provide (in this case, any single yes from
        // one of the endpoints indicates we can handle this query, so return true.
        boolean finalResult = false;
        for (boolean b : askResultList)
        {
            if (b) finalResult = true;
        }

        content.append("<results><boolean>" + finalResult + "</boolean></results></sparql>");

        askResultList.clear();

        return content.toString();
    }

    /**
     * This method handles a DESCRIBE SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL DESCRIBE query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    public final String handleDescribe(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        LOG.info("handling DESCRIBE");

        Model model = fetchModelsAndWait(projectName, query, endpoints);
        return mergeModelResults(model, query);
    }

    /**
     * This method handles a SPARQL UPDATE query.
     * It forward the query onto the provided endpoint and returns any response.
     * @param s Update query
     * @param endpoint Endpoint to query
     * @return Response string
     */
    public final String handleUpdate(final String projectName, final String s, final Endpoint endpoint)
    {
        LOG.info("handling UPDATE");

        return fetchUpdateQueryAndWait(projectName, s, endpoint);
    }

    /**
     * Merge all result models together and re-issue the request over the merged model.
     * @param mergedResults The merged results from endpoints
     * @param query Query returning a RDF model (CONSTRUCT or DESCRIBE)
     * @return Model serialised as a string
     */
    private String mergeModelResults(Model mergedResults, final Query query)
    {

        // now the model has been generated, run the query on the merged results
        QueryExecution qexec = QueryExecutionFactory.create(query, mergedResults);

        Model resultModel;
        if (query.isConstructType())
        {
            resultModel = qexec.execConstruct();
        }
        else
        {
            resultModel = qexec.execDescribe();
        }

        // create a string writer to print the model to
        StringWriter wr = new StringWriter();

        // write out the model
        resultModel.write(wr);

        // close the models as we don't need them any more
        resultModel.close();

        mergedResults.close();

        // return our string results
        return wr.toString();
    }

    /**
     * Issues the query across all given endpoints.
     * This method blocks until all results are returned
     * @param query Query returning a RDF model (CONSTRUCT or DESCRIBE)
     * @param endpoints Set of endpoints
     * @return Combined set of results as a Jena Model
     */
    private Model fetchModelsAndWait(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        // start a new model
        Model mergedResults = ModelFactory.createDefaultModel();

        CountDownLatch doneSignal = new CountDownLatch(endpoints.size());

        // fire off a thread to handle quering each endpoint
        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);

            taskExecutor.execute(new FetchModelResponseTask(this, mergedResults, query.serialize(), url, projectName, doneSignal));
        }

        // block until all threads have finished
        try
        {
            doneSignal.await();
        }
        catch (InterruptedException ex)
        {
            LOG.warn("Error while waiting on threads", ex);
        }

        return mergedResults;
    }

    /**
     * Issues the query across all given endpoints.
     * This method blocks until all results are returned
     * @param query Query returning a RDF model (CONSTRUCT or DESCRIBE)
     * @param endpoints Set of endpoints
     */
    private List<Result> fetchResultSetAndWait(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        List<Result> selectResultList = new LinkedList<Result>();
        CountDownLatch doneSignal = new CountDownLatch(endpoints.size());

        // fire off a thread to handle quering each endpoint
        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);

            taskExecutor.execute(new FetchResultSetResponseTask(this, selectResultList, query.serialize(), url, projectName, doneSignal));
        }

        // block until all threads have finished
        try
        {
            doneSignal.await();
        }
        catch (InterruptedException ex)
        {
            LOG.warn("Error while waiting on threads", ex);
        }

        // once threads have compeleted, construct results
        LOG.debug("Threads completed, constructing results");

        return selectResultList;
    }

   /**
     * Issues the update query to the given endpoint.
     * This method blocks until the query has finished
     * @param query U{DATE Query (INSERT, DELETE, etc)
     * @param endpoint Endpoint to query
    * @return the result (if any)
     */
    private String fetchUpdateQueryAndWait(final String projectName, final String query, final Endpoint endpoint)
    {
        CountDownLatch doneSignal = new CountDownLatch(1);

        StringBuffer result = new StringBuffer();

        // fire off a thread to handle quering each endpoint
        String url = endpoint.getLocation();
        LOG.debug("Querying " + url);

        taskExecutor.execute(new FetchUpdateResponseTask(this, result, query, url, projectName, doneSignal));

        // block until all threads have finished
        try
        {
            doneSignal.await();
        }
        catch (InterruptedException ex)
        {
            LOG.warn("Error while waiting on thread", ex);
        }

        return result.toString();
    }

    /**
     * Uses Jena's SortedResultSet to sort the results.
     * @param selectResultList Reference to the list of results to sort
     * @param conditions The sort conditions from the query
     */
    private void sortResults(List<Result> selectResultList, final List<SortCondition> conditions)
    {
        Comparator<Binding> comparator = new BindingComparator(conditions);
        SortedSet<Binding> sorted = new TreeSet<Binding>(comparator);

        List<Result> sortedResultList = new LinkedList<Result>();

        for (Result r : selectResultList)
        {
            Binding b = r.getBinding();
            sorted.add(b);
        }

        for (Binding b : sorted)
        {
            for (Result r : selectResultList)
            {
                if (r.getBinding().equals(b))
                {
                    sortedResultList.add(r);
                    selectResultList.remove(r);
                    break;
                }
            }
        }

        selectResultList.clear();
        selectResultList.addAll(sortedResultList);
    }
}
