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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.cachehandler.SimpleCacheHandler;
import org.wf.arnos.cachehandler.SimpleCacheHandlerTest;
import org.wf.arnos.queryhandler.JenaQueryWrapper;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import org.easymock.EasyMockSupport;
import org.wf.arnos.utils.Sparql;
import org.wf.arnos.queryhandler.QueryHandlerInterface;
import org.wf.arnos.queryhandler.QueryWrapperInterface;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class FetchModelResponseTaskTest extends EasyMockSupport
{
    String constructQuery = Sparql.CONSTRUCT_QUERY_BOOKS;
    String constructResult = Sparql.getResult(Sparql.ENDPOINT1_URL,constructQuery);

    QueryHandlerInterface mockThreadedQueryHandler;

    QueryWrapperInterface mockQueryWrapper;

    CountDownLatch doneSignal;

    String projectName = "testProject";

    @Before
    public void setUp()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        mockQueryWrapper = createMock(QueryWrapperInterface.class);
        mockThreadedQueryHandler = createMock(QueryHandlerInterface.class);
        doneSignal = new CountDownLatch(1);
    }
    
    @Test
    public void testRun()
    {
        System.out.println("testRun");

        Model model = ModelFactory.createDefaultModel();
        
        FetchModelResponseTask fetcher = new FetchModelResponseTask(mockThreadedQueryHandler,
                model,
                Sparql.ENDPOINT1_URL,
                constructQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        expect(mockThreadedQueryHandler.hasCache()).andReturn(false).anyTimes();
        
        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull())).
                andReturn(constructResult);

        expect(mockQueryWrapper.stringToModel(constructResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToModel(constructResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals(7,model.size());

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());

        model.removeAll();
        
        resetAll();

        expect(mockThreadedQueryHandler.hasCache()).andReturn(false).anyTimes();
        
        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull())).
                andReturn(constructResult);

        expect(mockQueryWrapper.stringToModel(constructResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToModel(constructResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals(7,model.size());
    }

    @Test
    public void testMockUseOfCache()
    {
        System.out.println("testMockUseOfCache");

        CacheHandlerInterface mockCache = createMock(CacheHandlerInterface.class);

        Model model = ModelFactory.createDefaultModel();

        FetchModelResponseTask fetcher = new FetchModelResponseTask(mockThreadedQueryHandler,
                model,
                Sparql.ENDPOINT1_URL,
                constructQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(mockCache).anyTimes();

        expect(mockCache.contains((String) notNull(), (String) notNull())).andReturn(false);
        mockCache.put((String)anyObject(), (String)anyObject(), (String)anyObject());

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull())).
                andReturn(constructResult);

        expect(mockQueryWrapper.stringToModel(constructResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToModel(constructResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals(7,model.size());

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());

        // run the query again, check the query wrapper is not called
        
        model.removeAll();

        resetAll();

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(mockCache).anyTimes();

        expect(mockCache.contains((String) notNull(), (String) notNull())).andReturn(true);
        expect(mockCache.get((String)anyObject(), (String)anyObject())).andReturn(constructResult);

        expect(mockQueryWrapper.stringToModel(constructResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToModel(constructResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals(7,model.size());
    }

    @Test
    public void testRealUseOfCache()
    {
        System.out.println("testRealUseOfCache");

        CacheHandlerInterface cache = null;
        try
        {
            cache = new SimpleCacheHandler(new File(SimpleCacheHandlerTest.CACHE_SETTINGS));
        }
        catch (Exception ex)
        {
            fail("Unable to create cache");
        }

        Model model = ModelFactory.createDefaultModel();

        FetchModelResponseTask fetcher = new FetchModelResponseTask(mockThreadedQueryHandler,
                model,
                Sparql.ENDPOINT1_URL,
                constructQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        assertFalse(cache.contains(projectName,fetcher.cacheKey));

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(cache).anyTimes();

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull())).
                andReturn(constructResult);

        expect(mockQueryWrapper.stringToModel(constructResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToModel(constructResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals(7,model.size());

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());

        // check the results has been put into the cache
        assertTrue(cache.contains(projectName,fetcher.cacheKey));

        model.removeAll();
        
        resetAll();

        // run the query again, check the cache was used

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(cache).anyTimes();
        
        expect(mockQueryWrapper.stringToModel(constructResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToModel(constructResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals(7,model.size());

        assertTrue(cache.contains(projectName,fetcher.cacheKey));
    }

    @Test
    public void testInstantiation()
    {
        System.out.println("testInstantiation");

        // for complete coverage, test creating the original object
        
        CountDownLatch doneSignal = new CountDownLatch(1);

        Model model = ModelFactory.createDefaultModel();

        FetchModelResponseTask fetcher = new FetchModelResponseTask(mockThreadedQueryHandler,
                model,
                Sparql.ENDPOINT1_URL,
                Sparql.CONSTRUCT_QUERY_BOOKS,
                projectName,
                doneSignal);

        QueryWrapperInterface wrapper = fetcher.getQueryWrapper();
        assertTrue(wrapper instanceof JenaQueryWrapper);
    }


    @Test
    public void testExceptionThrowing()
    {
        System.out.println("testExceptionThrowing");

        Model model = ModelFactory.createDefaultModel();

        FetchModelResponseTask fetcher = new FetchModelResponseTask(mockThreadedQueryHandler,
                model,
                Sparql.ENDPOINT1_URL,
                constructQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        expect(mockThreadedQueryHandler.hasCache())
                .andReturn(false)
                .anyTimes();

        Exception expectedException = new RuntimeException();

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull()))
                .andThrow(expectedException);

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }

    @Test
    public void testExceptionHandling()
    {
        System.out.println("testExceptionHandling");

        Model model = ModelFactory.createDefaultModel();

        FetchModelResponseTask fetcher = new FetchModelResponseTask(mockThreadedQueryHandler,
                model,
                Sparql.ENDPOINT1_URL,
                constructQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        expect(mockThreadedQueryHandler.hasCache())
                .andReturn(false)
                .anyTimes();

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull()))
                .andReturn("");

        model = ModelFactory.createDefaultModel();

        replayAll();

        fetcher.run();

        verifyAll();

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());

        resetAll();

        expect(mockThreadedQueryHandler.hasCache())
                .andReturn(false)
                .anyTimes();

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull()))
                .andReturn(null);

        model = ModelFactory.createDefaultModel();
        
        replayAll();

        fetcher.run();

        verifyAll();

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }
}