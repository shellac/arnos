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
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.cachehandler.SimpleCacheHandler;
import org.wf.arnos.cachehandler.SimpleCacheHandlerTest;
import org.wf.arnos.controller.model.sparql.Result;
import org.wf.arnos.queryhandler.JenaQueryWrapper;
import org.wf.arnos.queryhandler.QueryHandlerInterface;
import org.wf.arnos.queryhandler.QueryWrapperInterface;
import org.wf.arnos.utils.Sparql;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class AbstractResponseTaskTest extends EasyMockSupport {
    String selectQuery = Sparql.SELECT_QUERY_BOOKS;
    String selectResult = new Sparql().getResult(Sparql.ENDPOINT1_URL,selectQuery);

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
    public void testCacheLeakage()
    {
        System.out.println("testRealUseOfCache");

        CacheHandlerInterface cache = null;
        try
        {
            cache = new SimpleCacheHandler(new File(SimpleCacheHandlerTest.CACHE_SETTINGS));
            cache.flushAll(projectName);
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
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        assertFalse(cache.contains(projectName, fetcher.cacheKey));

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
        assertTrue(cache.contains(projectName, fetcher.cacheKey));

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

        assertTrue(cache.contains(projectName, fetcher.cacheKey));

        resetAll();

        List <Boolean> askResults = new LinkedList<Boolean>();

        // test cache with different response task
        String askQuery = Sparql.ASK_QUERY_ALICE;
        String askResult = new Sparql().getResult(Sparql.ENDPOINT1_URL, askQuery);
        FetchBooleanResponseTask fetcher2 = new FetchBooleanResponseTask(mockThreadedQueryHandler,
                askResults,
                Sparql.ENDPOINT1_URL,
                askQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        assertFalse("Cache should not contain the responsetask key",cache.contains(projectName, fetcher2.cacheKey));

        expect(mockThreadedQueryHandler.hasCache()).andReturn(true).anyTimes();
        expect(mockThreadedQueryHandler.getCache()).andReturn(cache).anyTimes();

        expect(mockQueryWrapper.execQuery(Sparql.ENDPOINT1_URL, askQuery)).andReturn(askResult);
        expect(mockQueryWrapper.stringToBoolean(askResult))
                .andReturn(true);

        replayAll();

        fetcher2.run();

        verifyAll();

        assertEquals("Got the expected number of results",1,askResults.size());
        assertTrue("Got the expected result", askResults.get(0));

        assertTrue(cache.contains(projectName, fetcher2.cacheKey));
    }
}