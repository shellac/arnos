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

import org.wf.arnos.utils.Sparql;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import static org.junit.Assert.*;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.sparql.Result;
import org.wf.arnos.utils.LocalServer;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ThreadedQueryHandlerTest extends EasyMockSupport {
    Query selectQuery = QueryFactory.create(Sparql.SELECT_QUERY_BOOKS);
    Query constructQuery = QueryFactory.create(Sparql.CONSTRUCT_QUERY_BOOKS);
    Query askQuery = QueryFactory.create(Sparql.ASK_QUERY_ALICE);
    Query describeQuery = QueryFactory.create(Sparql.DESCRIBE_QUERY_BOOK_2);

    // maximum number of results we're expecting
    private static final int MAX_LIMIT = 10;

    // minimum number of results we're expecting
    private static final int MIN_LIMIT = 5;

    // restrict timeout to save us waiting for default timeouts to complete when running tests.
    // If arnos.connection.timeout is added as a command line argument,
    // that value is used instead.
    private static int maxtimeout = 500;

    @BeforeClass
    public static void setUp() throws Exception
    {
        // check to see if a connection timeout has been specified
        String prop = System.getProperty("arnos.connection.timeout");
        if (prop == null)
        {
            System.out.println("ThreadedQueryHandlerTest WARN: Unable to find system property, using default timeout of "+maxtimeout+" instead.");
            System.out.println("If running tests repeatedly, you probably want to make sure you set the following system properties: arnos.connection.timeout and arnos.request.timeout");

            // note that if this test is being run together with others, the jenaquerywrapper may have already been initialized
            System.setProperty("arnos.connection.timeout",""+maxtimeout);
            System.setProperty("arnos.request.timeout",""+maxtimeout);
        }
        else
        {
            maxtimeout = Integer.parseInt(prop);
        }
        System.out.println("ThreadedQueryHandlerTest INFO: Timeout set to "+maxtimeout);

        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        LocalServer.start();
    }

    @AfterClass
    public static void tearDown() {
        LocalServer.stop();
    }

    @Test
    public void testHandleSelect()
    {
        System.out.println("testHandleSelect");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        String result = queryHandler.handleSelect(selectQuery, endpoints);

        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        int numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals("Results with endpoints 1 & 2",7,numResults);

        // now add another result set
        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));
        result = queryHandler.handleSelect(selectQuery, endpoints);
        results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals("Results with all endpoints",Sparql.MAX_LIMIT,numResults);

        // remove query limit
        Query noLimitSelectQuery = QueryFactory.create(Sparql.SELECT_QUERY_BOOKS_NO_LIMIT);
        result = queryHandler.handleSelect(noLimitSelectQuery, endpoints);
        results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals("Results with all endpoints",11,numResults);
    }


    @Test
    public void testHandleDistinctSelect()
    {
        System.out.println("testHandleDistinctSelect");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        Query query = QueryFactory.create(Sparql.SELECT_QUERY_PEOPLE);
        String result = queryHandler.handleSelect(query, endpoints);

        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        int numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals("Results non-distinct people",6,numResults);

        // now use the distinct query to filter results
        query = QueryFactory.create(Sparql.SELECT_QUERY_PEOPLE_DISTINCT);
        result = queryHandler.handleSelect(query, endpoints);
        results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals("Results with all endpoints",4,numResults);
    }

    @Test
    public void testHandleOrderedSelect()
    {
        System.out.println("testHandleOrderedSelect");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        Query query = QueryFactory.create(Sparql.SELECT_QUERY_PEOPLE_ORDERED);
        String result = queryHandler.handleSelect(query, endpoints);

        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        String previous = "";
        int numResults = 0;
        while (results.hasNext())
        {
            numResults++;
            QuerySolution sol = results.next();
            Result r = new Result(sol);

            String next = r.getValues().get(0);
            assertTrue(next +" > " + previous, next.compareTo(previous) > 0);
            previous = next;
        }

        assertEquals("Expeced number of ordered results",4,numResults);
    }

    @Test
    public void testHandleDescribe()
    {
        System.out.println("testHandleDescribe");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        String queryBefore = describeQuery.toString();
        String result = queryHandler.handleDescribe(describeQuery, endpoints);

        assertEquals(queryBefore,describeQuery.toString());

        assertTrue(result.contains("Harry Potter and the Chamber of Secrets"));
        assertTrue(result.contains("J.K. Rowling"));
        assertFalse(result.contains("dc:description"));
        assertFalse(result.contains("Scholastic Paperbacks"));

        // run the same query again to make sure we get the same results
        result = queryHandler.handleDescribe(describeQuery, endpoints);

        assertTrue(result.contains("Harry Potter and the Chamber of Secrets"));
        assertTrue(result.contains("J.K. Rowling"));
        assertFalse(result.contains("dc:description"));
        assertFalse(result.contains("Scholastic Paperbacks"));

        // add our slightly more knowledgeable endpoint
        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));

        result = queryHandler.handleDescribe(describeQuery, endpoints);

        assertTrue(result.contains("Harry Potter and the Chamber of Secrets"));
        assertTrue(result.contains("J.K. Rowling"));
        assertTrue(result.contains("dc:description"));
        assertTrue(result.contains("Scholastic Paperbacks"));
    }

    @Test
    public void testHandleConstruct()
    {
        System.out.println("testHandleConstruct");
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        String result = queryHandler.handleDescribe(constructQuery.cloneQuery(), endpoints);

        assertEquals(7,StringUtils.countMatches(result,"rdf:about"));

        endpoints.remove(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));

        result = queryHandler.handleDescribe(constructQuery.cloneQuery(), endpoints);

        assertEquals(3,StringUtils.countMatches(result,"rdf:about"));

        // test distinct construct
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));

        constructQuery.setLimit(10);
        constructQuery.setDistinct(false);
        result = queryHandler.handleDescribe(constructQuery.cloneQuery(), endpoints);
        assertEquals(8,StringUtils.countMatches(result,"rdf:about"));
        
        constructQuery.setDistinct(true);
        result = queryHandler.handleDescribe(constructQuery.cloneQuery(), endpoints);
        assertEquals(8,StringUtils.countMatches(result,"rdf:about"));
    }

    @Test
    public void testHandleUpdateSelect()
    {
        System.out.println("testHandleUpdateSelect");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        String query = Sparql.UPDATE_QUERY;
        String expectedResult = Sparql.UPDATE_QUERY_RESULT;

        String result = queryHandler.handleUpdate(query, new Endpoint(Sparql.ENDPOINT1_URL));

        assertEquals("Results non-distinct people",expectedResult,result);
    }

    @Test
    public void testTimeouts()
    {
        System.out.println("testTimeouts");
        System.out.println("Testing timeout with construct");

        int maxMultipleOfTimeoutToExpect = maxtimeout + (maxtimeout/2);

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT4_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        long start = System.currentTimeMillis();
        String result = queryHandler.handleDescribe(constructQuery.cloneQuery(), endpoints);
        long end = System.currentTimeMillis();

        // check we've got the result we expected
        assertEquals(7,StringUtils.countMatches(result,"rdf:about"));

        // check this didn't take longer then the timeout
        assertTrue("Min timeout reached ("+(end-start)+")", end-start >= maxtimeout);
        assertTrue("Max timeout within limits ("+(end-start)+")", end-start < maxMultipleOfTimeoutToExpect);

        System.out.println("Testing timeout with describe");

        start = System.currentTimeMillis();
        result = queryHandler.handleDescribe(describeQuery, endpoints);
        end = System.currentTimeMillis();

        assertTrue("Min timeout reached ("+(end-start)+" >= " + maxtimeout+")", end-start >= maxtimeout);
        assertTrue("Max timeout within limits ("+(end-start)+")", end-start < maxMultipleOfTimeoutToExpect);


        assertTrue(result.contains("Harry Potter and the Chamber of Secrets"));
        assertTrue(result.contains("J.K. Rowling"));
        assertFalse(result.contains("dc:description"));
        assertFalse(result.contains("Scholastic Paperbacks"));

        Query query = QueryFactory.create(Sparql.SELECT_QUERY_PEOPLE_ORDERED);

        start = System.currentTimeMillis();
        result = queryHandler.handleSelect(query, endpoints);
        end = System.currentTimeMillis();
        
        assertTrue("Min timeout reached ("+(end-start)+")", end-start >= maxtimeout);
        assertTrue("Max timeout within limits ("+(end-start)+")", end-start < maxMultipleOfTimeoutToExpect);

        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        String previous = "";
        int numResults = 0;
        while (results.hasNext())
        {
            numResults++;
            QuerySolution sol = results.next();
            Result r = new Result(sol);

            String next = r.getValues().get(0);
            assertTrue(next +" > " + previous, next.compareTo(previous) > 0);
            previous = next;
        }

        assertEquals("Expeced number of ordered results",4,numResults);

        System.out.println("Testing timeout with select");

        result = queryHandler.handleSelect(selectQuery, endpoints);

        results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals("Results with endpoints 1 & 2",7,numResults);
    }

    @Test
    public void testCacheHandling()
    {
        CacheHandlerInterface mockCache = createMock(CacheHandlerInterface.class);

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        assertEquals(false,queryHandler.hasCache());
        queryHandler.setCache(mockCache);
        assertEquals(true,queryHandler.hasCache());

        assertEquals(mockCache,queryHandler.getCache());

    }
}