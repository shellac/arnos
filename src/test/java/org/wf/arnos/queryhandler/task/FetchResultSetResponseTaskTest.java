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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.cachehandler.SimpleCacheHandler;
import org.wf.arnos.cachehandler.SimpleCacheHandlerTest;
import org.wf.arnos.queryhandler.JenaQueryWrapper;
import org.wf.arnos.utils.Sparql;
import org.wf.arnos.queryhandler.QueryHandlerInterface;
import org.wf.arnos.queryhandler.QueryWrapperInterface;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import org.easymock.EasyMockSupport;
import org.wf.arnos.controller.model.sparql.Result;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class FetchResultSetResponseTaskTest extends EasyMockSupport
{
    String selectQuery = Sparql.SELECT_QUERY_BOOKS;
    String selectResult = Sparql.getResult(Sparql.ENDPOINT1_URL,selectQuery);

    QueryHandlerInterface mockThreadedQueryHandler;

    QueryWrapperInterface mockQueryWrapper;

    CountDownLatch doneSignal;

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

        String selectResult = Sparql.getResult(Sparql.ENDPOINT1_URL, selectQuery);

        List <Result> results = new LinkedList<Result>();
        FetchResultSetResponseTask fetcher = new FetchResultSetResponseTask(mockThreadedQueryHandler,
                results,
                Sparql.ENDPOINT1_URL,
                selectQuery,
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
                andReturn(selectResult);

        expect(mockQueryWrapper.stringToResultSet(selectResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToResultSet(selectResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Got expected number of results",7, results.size());

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }

    @Test
    public void testMockUseOfCache()
    {
        System.out.println("testMockUseOfCache");

        CacheHandlerInterface mockCache = createMock(CacheHandlerInterface.class);

        List <Result> results = new LinkedList<Result>();

        FetchResultSetResponseTask fetcher = new FetchResultSetResponseTask(mockThreadedQueryHandler,
                results,
                Sparql.ENDPOINT1_URL,
                selectQuery,
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

        expect(mockCache.contains((String) notNull())).andReturn(false);
        
        mockCache.put((String)anyObject(), (String)anyObject());

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull())).
                andReturn(selectResult);

        expect(mockQueryWrapper.stringToResultSet(selectResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToResultSet(selectResult));
        
        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Got expected number of results",7, results.size());
        
        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());

        // run the query again, check the query wrapper is not called

        resetAll();
        
        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(mockCache).anyTimes();

        expect(mockCache.contains((String) notNull())).andReturn(true);
        expect(mockCache.get((String)anyObject())).andReturn(selectResult);

        expect(mockQueryWrapper.stringToResultSet(selectResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToResultSet(selectResult));

        results.clear();
        
        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Got expected number of results",7, results.size());
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

        List <Result> results = new LinkedList<Result>();

        FetchResultSetResponseTask fetcher = new FetchResultSetResponseTask(mockThreadedQueryHandler,
                results,
                Sparql.ENDPOINT1_URL,
                selectQuery,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };


        assertFalse(cache.contains(fetcher.cacheKey));

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(cache).anyTimes();

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull())).
                andReturn(selectResult);

        expect(mockQueryWrapper.stringToResultSet(selectResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToResultSet(selectResult));

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Got expected number of results",7, results.size());

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());

        // check the results has been put into the cache
        assertTrue(cache.contains(fetcher.cacheKey));

        resetAll();

        // run the query again, check the cache was used

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(cache).anyTimes();
        
        expect(mockQueryWrapper.stringToResultSet(selectResult))
                .andReturn(JenaQueryWrapper.getInstance().stringToResultSet(selectResult));

        results.clear();
        
        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Got expected number of results",7, results.size());

        assertTrue(cache.contains(fetcher.cacheKey));
    }

    @Test
    public void testInstantiation()
    {
        System.out.println("testInstantiation");
        // for complete coverage, test creating the original object

        List <Result> results = new LinkedList<Result>();

        FetchResultSetResponseTask fetcher = new FetchResultSetResponseTask(mockThreadedQueryHandler,
                results,
                Sparql.ENDPOINT1_URL,
                selectQuery,
                doneSignal);

        QueryWrapperInterface wrapper = fetcher.getQueryWrapper();
        assertTrue(wrapper instanceof JenaQueryWrapper);
    }


    @Test
    public void testExceptionThrowing()
    {
        System.out.println("testExceptionThrowing");

        List <Result> results = new LinkedList<Result>();

        FetchResultSetResponseTask fetcher = new FetchResultSetResponseTask(mockThreadedQueryHandler,
                results,
                Sparql.ENDPOINT1_URL,
                selectQuery,
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

        List <Result> results = new LinkedList<Result>();

        FetchResultSetResponseTask fetcher = new FetchResultSetResponseTask(mockThreadedQueryHandler,
                results,
                Sparql.ENDPOINT1_URL,
                selectQuery,
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

        replayAll();

        fetcher.run();

        verifyAll();

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }
}