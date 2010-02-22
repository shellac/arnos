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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.controller.model.Endpoint;

/**
 * A native implementation which uses string concatination to merge results together.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class SimpleQueryHandler implements QueryHandlerInterface 
{
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SimpleQueryHandler.class);

    /**
     * SPARQL SELECT tag used to do string concatination.
     */
    public static final String TAG = "results";

    /**
     * The cache handler, autowired in.
     */
    @Autowired(required = false)
    private transient CacheHandlerInterface cacheHandler;

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
     * This implementation, simple contatinates all query results.
     * @param queryString SPARQL query to execute
     * @param endpoints List of endpoint urls to run the query against
     * @return An RDF model
     */
    public final String handleQuery(final String queryString, final List<Endpoint> endpoints)
    {
        LOG.debug("Querying against  " + endpoints.size() + " endpoints");

        ArrayList<String> results = new ArrayList<String>();

        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);
            results.add(JenaQueryWrapper.getInstance().execSelect(queryString, url));
        }

        StringBuffer finalResult = new StringBuffer("");

        if (results.size() > 0)
        {

            int insertionPoint = results.get(0).toLowerCase().lastIndexOf("</" + TAG + ">");

            finalResult.append(results.get(0).substring(0, insertionPoint));
            for (int i = 0; i < results.size(); i++)
            {
                String s = extractResults(results.get(i));
                finalResult.append(s);
            }

            finalResult.append(results.get(0).substring(insertionPoint));
        }

        return finalResult.toString();
    }

    private static final String extractResults(final String s)
    {
        try
        {
            String newString = s;
            newString = newString.substring(newString.toLowerCase().indexOf("<" + TAG + ">") + TAG.length() + 2);
            newString = newString.substring(0, newString.toLowerCase().lastIndexOf("</" + TAG + ">"));
            return newString;
        }
        catch (Exception ex)
        {
            LOG.error(ex);
            return s;
        }
    }
}
