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

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.cachehandler.SimpleCacheHandler;
import org.wf.arnos.cachehandler.SimpleCacheHandlerTest;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.Project;
import org.wf.arnos.controller.model.ProjectsManager;
import org.wf.arnos.exception.ResourceNotFoundException;
import org.wf.arnos.queryhandler.ThreadedQueryHandler;
import org.wf.arnos.utils.LocalServer;
import org.wf.arnos.utils.Sparql;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class QueryControllerTest extends EasyMockSupport
{
    Project p;
    Endpoint ep1 = new Endpoint(Sparql.ENDPOINT1_URL); // 7 books
    Endpoint ep2 = new Endpoint(Sparql.ENDPOINT2_URL); // 0 books
    Endpoint ep3 = new Endpoint(Sparql.ENDPOINT3_URL); // 4 books
    private QueryController controller;
    static String PROJECT_NAME = "testproject";
    static String QueryString = Sparql.SELECT_QUERY_BOOKS;


    @Before
    public void setUp()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        Logger.getLogger("org.wf.arnos.controller.QueryController").setLevel(Level.ALL);

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        ProjectsManager manager = new ProjectsManager();
        
        // setup default projects
        p = new Project(PROJECT_NAME);
        p.addEndpoint(Sparql.ENDPOINT1_URL);
        p.addEndpoint(Sparql.ENDPOINT2_URL);
        p.addEndpoint(Sparql.ENDPOINT3_URL);
        manager.addProject(p);

        controller = new QueryController();
        controller.manager = manager;
        controller.queryHandler = queryHandler;

        CacheHandlerInterface cache = null;
        try
        {
            cache = new SimpleCacheHandler(new File(SimpleCacheHandlerTest.CACHE_SETTINGS));
        }
        catch (Exception ex)
        {
            System.out.println("Throwing error");
            ex.printStackTrace();
            fail("Unable to create cache");
        }

        cache.flushAll(PROJECT_NAME);
        controller.cacheHandler = cache;
        QueryController.logger = LogFactory.getLog(QueryController.class);
    }

    @BeforeClass
    public static void setUpClass()
    {
        LocalServer.start();
    }

    @AfterClass
    public static void tearDownClass() {
        LocalServer.stop();
    }

    @Test
    public void testExecuteQueryAcrossAllEndpoints()
    {
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, writer);
        StringBuffer buffer = writer.getBuffer();
        int countReturnedInstances = StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">");
        System.out.println("testExecuteQueryAcrossAllEndpoints\n"+buffer.toString());
        assertEquals(Sparql.MAX_LIMIT,countReturnedInstances);

        // test output is valid xml
        assertTrue("Is valid xml", Sparql.validateXML(buffer.toString()));
    }

    @Test
    public void testMissingCache()
    {
        controller.cacheHandler = null;
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, writer);
        StringBuffer buffer = writer.getBuffer();
        int countReturnedInstances = StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">");
        assertEquals(Sparql.MAX_LIMIT,countReturnedInstances);
    }

    @Test
    public void testWithCache()
    {
        CacheHandlerInterface mockCache = createMock(CacheHandlerInterface.class);
        controller.cacheHandler = mockCache;
        String testContent = "abc";

        StringWriter writer = new StringWriter();

        expect(mockCache.contains((String) notNull(), (String) notNull())).andReturn(false);
        mockCache.put((String) notNull(), (List<Endpoint>) notNull(),  (String) notNull(),  (String) notNull());

        replayAll();

        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, writer);

        verifyAll();

        resetAll();
        writer = new StringWriter();

        expect(mockCache.contains((String) notNull(), (String) notNull())).andReturn(true);
        expect(mockCache.get((String) notNull(), (String) notNull())).andReturn(testContent);

        replayAll();

        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, writer);

        verifyAll();

        String output = writer.getBuffer().toString();
        assertEquals(testContent,output);
    }


    @Test
    public void testExecuteQueryWithSpecificEndpoints()
    {
        StringWriter writer = new StringWriter();
        StringBuffer buffer;

        // test with no endpoints
        String noEndPoints = "";
        int expected_noEndPoints = 0;
        
        controller.executeGetQuery(PROJECT_NAME, noEndPoints, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_noEndPoints,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));
        writer = new StringWriter();

        // test with the first endpoint
        String oneEndpoint = ep1.getIdentifier();
        int expected_oneEndpoint = 7;
        controller.executeGetQuery(PROJECT_NAME, oneEndpoint, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_oneEndpoint,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));
        writer = new StringWriter();

        // test with endpoints 1 & 2
        String twoEndpoints = ep1.getIdentifier() + "+" + ep2.getIdentifier();
        int expected_twoEndpoints = 7;
        controller.executeGetQuery(PROJECT_NAME, twoEndpoints, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_twoEndpoints,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));
        writer = new StringWriter();

        // test with endpoints 1 & 3
        String anotherTwoEndpoints = ep1.getIdentifier() + "+" + ep3.getIdentifier();
        int expected_anotherTwoEndpoints = Sparql.MAX_LIMIT;
        controller.executeGetQuery(PROJECT_NAME, anotherTwoEndpoints, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_anotherTwoEndpoints,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));
        writer = new StringWriter();

        // test with endpoints 2 & 3
        String thirdEndpoint = ep2.getIdentifier() + "+" + ep3.getIdentifier();
        int expected_thirdEndpoint = 4;
        controller.executeGetQuery(PROJECT_NAME, thirdEndpoint, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_thirdEndpoint,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));
        writer = new StringWriter();

        // test with all endpoints 1, 2 & 3
        String allEndpoints = ep1.getIdentifier() + "+" + ep2.getIdentifier() + "+" + ep3.getIdentifier();
        int expected_allEndpoints = Sparql.MAX_LIMIT;
        controller.executeGetQuery(PROJECT_NAME, allEndpoints, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_allEndpoints,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));
        writer = new StringWriter();

        // test with duplicate endpoints 3, 2 & 3
        String duplicateEndpoints = ep3.getIdentifier() + "+" + ep2.getIdentifier() + "+" + ep3.getIdentifier();
        int expected_duplicateEndpoints = 8;
        controller.executeGetQuery(PROJECT_NAME, duplicateEndpoints, QueryString, writer);
        buffer = writer.getBuffer();
        assertEquals(expected_duplicateEndpoints,StringUtils.countMatches(buffer.toString(),"<binding name=\"title\">"));

        // test with post request
        controller.executePostQuery(PROJECT_NAME, duplicateEndpoints, QueryString, writer);
        buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("error"));
    }

    @Test
    public void testUTF8Handling()
    {
        StringWriter writer = new StringWriter();
        StringBuffer buffer;

        String expectedResponse = "我叫柯睿思";

        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, writer);
    
        buffer = writer.getBuffer();
        System.out.println(expectedResponse);
        System.out.println(buffer.toString());
        assertTrue(buffer.toString().contains(expectedResponse));
    }

    @Test
    public void testEdgeCases()
    {
        StringWriter writer = new StringWriter();
        try
        {
            controller.executeQueryAcrossAllEndpoints(null, QueryString, writer);
            fail("ResourceNotFoundException not thrown");
        }
        catch (ResourceNotFoundException e)
        {
            // expected result
        }

        try
        {
            controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, null);
        }
        catch (Exception e)
        {
            fail("Exception is thrown");
        }

        try
        {
            controller.executeQueryAcrossAllEndpoints(PROJECT_NAME+"other", QueryString, writer);
            fail("ResourceNotFoundException not thrown");
        }
        catch (ResourceNotFoundException e)
        {
            // expected result
        }

        writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, null, writer);
        StringBuffer buffer = writer.getBuffer();
        assertEquals("",buffer.toString());
        
        writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, "", writer);
        buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("error"));
    }

    @Test
    public void testAskQuery()
    {
        String query = Sparql.ASK_QUERY_ALICE;
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, query, writer);
        StringBuffer buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("true"));

        assertTrue("Is valid xml", Sparql.validateXML(buffer.toString()));
    }

    @Test
    public void testDescribeQuery()
    {
        String query = Sparql.DESCRIBE_QUERY_BOOK_2;
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, query, writer);
        StringBuffer buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("j.k. rowling"));

        assertTrue("Is valid xml", Sparql.validateXML(buffer.toString()));
    }

    @Test
    public void testUnknownQuery()
    {
        String query = Sparql.UPDATE_QUERY;
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, query, writer);
        StringBuffer buffer = writer.getBuffer();
        assertTrue(buffer.toString().contains("error"));

        assertTrue("Is valid xml", Sparql.validateXML(buffer.toString()));
    }

    @Test
    public void testConstructQuery()
    {
        String query = Sparql.CONSTRUCT_QUERY_BOOKS;
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, query, writer);
        StringBuffer buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("semantic web programming"));

        assertTrue("Is valid xml", Sparql.validateXML(buffer.toString()));
    }

    @Test
    public void testUpdateQuery()
    {
        String query = Sparql.UPDATE_QUERY;
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, query, writer);
        StringBuffer buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("error"));

        writer = new StringWriter();
        controller.executePostQuery(PROJECT_NAME, ep1.getIdentifier(), query, writer);
        buffer = writer.getBuffer();
        assertEquals(Sparql.UPDATE_QUERY_RESULT, buffer.toString());

        // test error handling

        // test sending an update as a get
        writer = new StringWriter();
        buffer = writer.getBuffer();
        controller.executeGetQuery(PROJECT_NAME, ep1.getIdentifier(), query, writer);
        buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("error"));

        writer = new StringWriter();
        buffer = writer.getBuffer();
        controller.executePostQuery(PROJECT_NAME, ep1.getIdentifier(), Sparql.ASK_QUERY_ALICE, writer);
        buffer = writer.getBuffer();
        assertTrue(buffer.toString().toLowerCase().contains("error"));

        // test cache clearing
        CacheHandlerInterface mockCache = createMock(CacheHandlerInterface.class);

        controller.cacheHandler = mockCache;

        mockCache.flush(PROJECT_NAME, ep1);

        writer = new StringWriter();

        replayAll();

        controller.executePostQuery(PROJECT_NAME, ep1.getIdentifier(), query, writer);

        verifyAll();

        controller.cacheHandler = null;

        resetAll();
        
        replayAll();

        controller.executePostQuery(PROJECT_NAME, ep1.getIdentifier(), query, writer);

        verifyAll();
    }
}
