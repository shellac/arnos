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
package org.wf.arnos.queryhandler.task;

import java.util.concurrent.CountDownLatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wf.arnos.queryhandler.JenaQueryWrapper;
import org.wf.arnos.queryhandler.QueryHandlerInterface;
import org.wf.arnos.queryhandler.QueryWrapperInterface;

/**
 * Task to handle sparql query.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */

abstract class AbstractResponseTask implements Runnable
{
    /**
     * Logger.
     */
    private static Log LOG;

    /**
     * Handle to query processor for posting results back.
     */
    protected final transient QueryHandlerInterface handler;

    /**
     * Endpoint url.
     */
    protected final transient String url;

    /**
     * Query to execute.
     */
    protected final transient String query;

    /**
     * A latch to signal when thread completed.
     */
    protected final transient CountDownLatch doneSignal;

    /**
     * Cache key lookup value.
     */
    protected final transient String cacheKey;

    /**
     * A handle to the query wrapper.
     */
    protected final transient QueryWrapperInterface querywrapper;

    /**
     * Constructor for thread.
     * @param paramHandler handling class
     * @param paramQuery SPARQL query
     * @param paramUrl Endpoint url
     * @param paramDoneSignal Latch signal to use to notify parent when completed
     */
    protected AbstractResponseTask(final QueryHandlerInterface paramHandler,
                                                           final String paramQuery,
                                                           final String paramUrl,
                                                           final CountDownLatch paramDoneSignal)
    {
        super();

        LOG = LogFactory.getLog(this.getClass());

        this.handler = paramHandler;
        this.query = paramQuery;
        this.url = paramUrl;
        this.doneSignal = paramDoneSignal;

        // calculate the key to use for cache lookup.
        this.cacheKey = query + url;

        this.querywrapper = getQueryWrapper();
    }

    /**
     * Returns a JenaQueryWrapper object.
     * We do this as an aid to unit testing.
     * @return JenaQueryWrapper
     */
    protected QueryWrapperInterface getQueryWrapper()
    {
        return JenaQueryWrapper.getInstance();
    }

    /**
     * Worker's run method.
     */
    public abstract void run();

    /**
     * Lookup a string from cache.
     * @return Value in cache, <code>null</code> if missing/cache not present
     */
    public String getFromCache()
    {
        // check cache copy
        if (handler.hasCache() && handler.getCache().contains(cacheKey))
        {
            LOG.debug("Lookup query from cache");

            return handler.getCache().get(cacheKey);
        }
        return null;
    }

    /**
     * Put an entry into the cache.
     * @param s Value to store in cache
     */
    public void putInCache(final String s)
    {
        if (handler.hasCache())
        {
            LOG.debug("Putting result in cache");
            handler.getCache().put(cacheKey, s);
        }
    }

}
