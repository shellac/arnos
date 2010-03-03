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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * This class provides an customized version of Jena's execSelect and execConstruct methods.
 * We do this because it allows us to insert a layer of caching of the endpoint results.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public interface QueryWrapperInterface
{

    /**
     * Executes the provided query against an endpoint.
     * This method returns the raw html result as a string
     * @param querystring SPARQL CONSTRUCT query
     * @param service URL endpoint
     * @return Query result as a string
     */
    String execQuery(final String querystring, final String service);

    /**
     * Converts a string into a model.
     * @param s Raw xml result
     * @return Model representation
     */
    Model stringToModel(final String s);

    /**
     * Converts a sparql select query result into a resultSet object.
     * @param s Raw xml result
     * @return ResultSet object
     */
    ResultSet stringToResultSet(final String s);

    /**
     * Converts an ASK response into a boolean value.
     * @param s Raw xml result
     * @return Boolean answer
     */
    boolean stringToBoolean(final String s);
}
