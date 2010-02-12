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
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.sparql.Result;

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
     * How long (ms) the main thread should wait when checking for thread completion.
     */
    private static final long MAIN_THREAD_WAIT = 30000;

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
     * Stores the number of threads that will get executed (one per endpoint).
     */
    private transient int threadsToWaitFor;

    /**
     * Tracks the number of completed threads.
     */
    private transient AtomicInteger threadsCompleted = new AtomicInteger();

    /**
     * Method for letting a thread inform the handler that it's finished processing.
     */
    public final void setCompleted()
    {
        synchronized (threadsCompleted)
        {
            threadsCompleted.incrementAndGet();
            threadsCompleted.notifyAll();
        }
    }

    /**
     * A container for SPARQL SELECT results gathered by the threads.
     */
    private transient List<Result> selectResultList;

    /**
     * Adds a result.
     * @param r A single SPARQL SELECT result object.
     */
    public final void addResult(final Result r)
    {
        synchronized (this)
        {
            selectResultList.add(r);
        }
    }

    /**
     * Holder for construct query results.
     */
    private transient Model constructModel;

    /**
     * Adds a construct query results model.
     * @param m Model to add
     */
    public final void addResult(final Model m)
    {
        synchronized (this)
        {
            constructModel.add(m);
        }
    }

    /**
     * This implementation, simple contatinates all query results.
     * @param queryString SPARQL query to execute
     * @param endpoints List of endpoint urls to run the query against
     * @return An RDF model
     */
    public final String handleQuery(final String queryString, final List<Endpoint> endpoints)
    {
        LOG.debug("Querying against  " + endpoints.size() + " endpoints");

        // reset conditions
        threadsCompleted.set(0);
        threadsToWaitFor = endpoints.size();

        // process the SPARQL query to best determin how to handle this query
        Query query = QueryFactory.create(queryString);

        if (query.getQueryType() == Query.QueryTypeSelect)
        {
            // this is a simple select query. Results can be appended, and limited as required
            return handleSelect(query, endpoints);
        }
        else if (query.getQueryType() == Query.QueryTypeConstruct)
        {
            return handleConstruct(query, endpoints);
        }
        else
        {
            LOG.warn("Unable to handle this query type");
        }

        return "";
    }


    private String handleConstruct(final Query query, final List<Endpoint> endpoints)
    {
        // start a new model
        constructModel = ModelFactory.createDefaultModel();

        // fire off a thread to handle quering each endpoint
        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);

            taskExecutor.execute(new FetchConstructResponseTask(this, query, url));
        }

        // block until all threads have finished
        waitForThreadsToComplete();

        StringWriter wr = new StringWriter();
        constructModel.write(wr);
        return wr.toString();
    }


    /**
     * This method handles a SELECT SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL SELECT query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    private String handleSelect(final Query query, final List<Endpoint> endpoints)
    {
        selectResultList = new LinkedList<Result>();

        // fire off a thread to handle quering each endpoint
        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);

            taskExecutor.execute(new FetchSelectResponseTask(this, query, url));
        }

        // block until all threads have finished
        waitForThreadsToComplete();

        // once threads have compeleted, construct results
        LOG.debug("Threads completed, constructing results");

        StringBuffer content = new StringBuffer(DEFAULT_SB_LENGTH);

        content.append("<?xml version=\"1.0\"?><sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"><head>");

        // add head info
        List<String> vars = query.getResultVars();
        for (String var : vars)
        {
            content.append("<varaible name=\"");
            content.append(var);
            content.append("\"/>");
        }
        content.append("</head><results>");

        // collate all responses
        boolean hasLimit = false;
        long limit = -1;

        if (query.hasLimit())
        {
            limit = query.getLimit();
            hasLimit = true;
        }

        for (Result r : selectResultList)
        {
            if (!hasLimit || limit > 0)
            {
                if (hasLimit) limit--;
                content.append(r.toXML());
            }
        }

        content.append("</results></sparql>");

        clearUp();

        return content.toString();
    }

    
    /**
     * Blocking method to wait until all threads have finished processing.
     * @param threadsToWaitFor Number of threads currently running
     */
    private void waitForThreadsToComplete()
    {
        synchronized (threadsCompleted)
        {
            while (threadsCompleted.get() < threadsToWaitFor)
            {
                try
                {
                    threadsCompleted.wait();
                }
                catch (InterruptedException ex)
                {
                    LOG.error("Error while waiting on threads", ex);
                }
            }
        }
    }


    /**
     * Clean up any used resources.
     */
    private void clearUp()
    {
        selectResultList.clear();
    }


    /**
     * Task to handle sparql query.
     */
    static abstract class BasicResponseTask implements Runnable
    {
        /**
         * Handle to query processor for posting results back.
         */
        protected final transient ThreadedQueryHandler handler;

        /**
         * Endpoint url.
         */
        protected final transient String url;

        /**
         * Query to execute.
         */
        protected final transient Query query;

        /**
         * Constructor for thread.
         * @param paramHandler handling class
         * @param paramQuery SPARQL query
         * @param paramUrl Endpoint url
         */
        protected BasicResponseTask(final ThreadedQueryHandler paramHandler, final Query paramQuery,  final String paramUrl)
        {
            super();
            this.handler = paramHandler;
            this.query = paramQuery;
            this.url = paramUrl;
        }
    }

    /**
     * Handles obtaining select query from endpoint and parsing result set.
     */
    static class FetchSelectResponseTask extends BasicResponseTask
    {
        /**
         * Constructor for thread.
         * @param paramHandler handling class
         * @param paramQuery SPARQL query
         * @param paramUrl Endpoint url
         */
        FetchSelectResponseTask(final ThreadedQueryHandler paramHandler, final Query paramQuery,  final String paramUrl)
        {
            super(paramHandler, paramQuery, paramUrl);
        }

        /**
         * Executes the query on the specified endpoint and processes the results.
         */
        @Override
        public void run()
        {
            QueryEngineHTTP qehttp = QueryExecutionFactory.createServiceRequest(url, query);

            try
            {
                ResultSet resultSet = qehttp.execSelect();

                while (resultSet.hasNext())
                {
                    QuerySolution sol = resultSet.next();
                    handler.addResult(new Result(sol));
                }
            }
            catch (QueryExceptionHTTP qhttpe)
            {
                LOG.error("Unable to execute query against " + url);
            }
            finally
            {
                qehttp.close();
                handler.setCompleted();
            }
        }
    }

    /**
     * Handles obtaining construct query from endpoint and parsing result set.
     */
    static class FetchConstructResponseTask extends BasicResponseTask
    {
        /**
         * Constructor for thread.
         * @param paramHandler handling class
         * @param paramQuery SPARQL query
         * @param paramUrl Endpoint url
         */
        FetchConstructResponseTask(final ThreadedQueryHandler paramHandler, final Query paramQuery,  final String paramUrl)
        {
            super(paramHandler, paramQuery, paramUrl);
        }

        /**
         * Executes the query on the specified endpoint and processes the results.
         */
        @Override
        public void run()
        {
            QueryEngineHTTP qehttp = QueryExecutionFactory.createServiceRequest(url, query);

            try
            {
                Model model  = qehttp.execConstruct();
System.out.println("Obtained:"+model.size());
                handler.addResult(model);
            }
            catch (QueryExceptionHTTP qhttpe)
            {
                LOG.error("Unable to execute query against " + url);
            }
            finally
            {
                qehttp.close();
                handler.setCompleted();
            }
        }
    }

}
