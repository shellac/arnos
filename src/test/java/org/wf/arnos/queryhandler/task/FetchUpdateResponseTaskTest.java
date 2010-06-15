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
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
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
public class FetchUpdateResponseTaskTest extends EasyMockSupport
{
    String updateQuery = Sparql.UPDATE_QUERY;
    String queryResult = Sparql.UPDATE_QUERY_RESULT;

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

        StringBuffer result = new StringBuffer();
        
        FetchUpdateResponseTask fetcher = new FetchUpdateResponseTask(mockThreadedQueryHandler,
                result,
                Sparql.ENDPOINT1_URL,
                updateQuery,
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
                andReturn(queryResult);

        replayAll();

        fetcher.run();

        verifyAll();

        assertEquals("Result is as expected", queryResult, result.toString());

        // now check we've got the expected number of results
        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }

    @Test
    public void testInstantiation()
    {
        System.out.println("testInstantiation");

        // for complete coverage, test creating the originall object
        
        CountDownLatch doneSignal = new CountDownLatch(1);

        StringBuffer result = null;

        FetchUpdateResponseTask fetcher = new FetchUpdateResponseTask(mockThreadedQueryHandler,
                result,
                Sparql.ENDPOINT1_URL,
                updateQuery,
                projectName,
                doneSignal);

        QueryWrapperInterface wrapper = fetcher.getQueryWrapper();
        assertTrue(wrapper instanceof JenaQueryWrapper);

        assertEquals(null,fetcher.getFromCache());
    }


    @Test
    public void testExceptionThrowing()
    {
        System.out.println("testExceptionThrowing");

        CountDownLatch doneSignal = new CountDownLatch(1);

        StringBuffer result = null;

        FetchUpdateResponseTask fetcher = new FetchUpdateResponseTask(mockThreadedQueryHandler,
                result,
                Sparql.ENDPOINT1_URL,
                updateQuery,
                projectName,
                doneSignal)
        {
            @Override
            protected QueryWrapperInterface getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        Exception expectedException = new RuntimeException();

        expect(mockQueryWrapper.execQuery((String) notNull(), (String) notNull()))
                .andThrow(expectedException);

        replayAll();

        fetcher.run();

        verifyAll();

        assertNull(result);

        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }
}