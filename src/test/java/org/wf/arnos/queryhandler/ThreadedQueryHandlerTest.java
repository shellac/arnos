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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import static org.junit.Assert.*;
import org.wf.arnos.controller.model.Endpoint;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ThreadedQueryHandlerTest {

    private static final int LIMIT = 10;

    @Before
    public void setUp()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
    }

    @Test
    public void testHandleQuery()
    {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint("http://services.data.gov.uk/analytics/sparql"));
        endpoints.add(new Endpoint("http://services.data.gov.uk/education/sparql"));


        String selectQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"+
"SELECT DISTINCT ?type\n"+
"    WHERE {\n"+
"      ?thing a ?type\n"+
"    }    LIMIT "+LIMIT;

        String constructQuery = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"+
"CONSTRUCT { $book dc:title $title }\n"+
"WHERE\n"+
"  { $book dc:title $title }    LIMIT "+LIMIT;

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        String selectQueryResults = queryHandler.handleQuery(selectQuery, endpoints);

        assertTrue(StringUtils.isNotEmpty(selectQueryResults));
        assertTrue("Expected reasonable size results",selectQueryResults.length() > 1200);

        int bindings = StringUtils.countMatches(selectQueryResults, "<binding");
        assertTrue(LIMIT >=bindings);

        String constructQueryResults = queryHandler.handleQuery(constructQuery, endpoints);

        assertTrue(StringUtils.isNotEmpty(constructQueryResults));

        int rdfDesc = StringUtils.countMatches(constructQueryResults, "<rdf:Description");

        // statistics/directgov produces 7 matches
        assertTrue(LIMIT >= rdfDesc);

        // add a new endpoint
        endpoints.add(new Endpoint("http://sparql.org/books"));

        // rerun query
        constructQueryResults = queryHandler.handleQuery(constructQuery, endpoints);

        rdfDesc = StringUtils.countMatches(constructQueryResults, "<rdf:Description");
        assertTrue(LIMIT >= rdfDesc);
    }
}