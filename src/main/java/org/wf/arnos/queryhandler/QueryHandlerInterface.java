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
import com.hp.hpl.jena.rdf.model.Model;
import java.util.List;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.sparql.Result;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public interface QueryHandlerInterface
{
    /**
     * Handles the federated CONSTRUCT sparql query across endpoints.
     * @param query SPARQL CONSTRUCT query
     * @param endpoints List of endpoints to conduct query accross
     * @return Result as an xml string
     */
    String handleConstruct(Query query, List<Endpoint> endpoints);

    /**
     * This method handles a SELECT SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL SELECT query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    String handleSelect(Query query, List<Endpoint> endpoints);

    /**
     * This method handles a ASK SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL ASK query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    String handleAsk(Query query, List<Endpoint> endpoints);

    /**
     * This method handles a DESCRIBE SPARQL query.
     * It uses threads to query each endpoint and then combines the responses.
     * @param query SPARQL DESCRIBE query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    String handleDescribe(Query query, List<Endpoint> endpoints);

    /**
     * This method handles a SPARQL UPDATE query.
     * It forward the query onto the provided endpoint and returns any response.
     * @param query SPARQL UPDATE query (not a Query object as it can't parse UPDATE statements)
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    String handleUpdate(String query, Endpoint endpoint);

    /**
     * Public accessor for cache (if present).
     * @return CacheHandler supplied by spring, or <code>null</code> otherwise
     */
    CacheHandlerInterface getCache();

    /**
     * Sets the cache handler.
     * @param cache Cache implementing the CacheHandlerInterface
     */
    void setCache(CacheHandlerInterface cache);

    /**
     * Check to see if cache has been set.
     * @return Boolean, <code>true</code> if a cache exists, <code>false</code> otherwise
     */
    boolean hasCache();
}
