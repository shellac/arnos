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
import org.junit.Test;
import org.wf.arnos.queryhandler.JenaQueryWrapper;
import org.wf.arnos.queryhandler.JenaQueryWrapperTest;
import static org.junit.Assert.*;
import org.wf.arnos.queryhandler.task.mocks.MockJenaQueryWrapper;
import org.wf.arnos.queryhandler.task.mocks.MockThreadedQueryHandler;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class FetchSelectResponseTaskTest {

    MockThreadedQueryHandler mockThreadedQueryHandler;

    MockJenaQueryWrapper mockQueryWrapper;

    
    @Test
    public void testRun()
    {
        System.out.println("testRun");

        CountDownLatch doneSignal = new CountDownLatch(1);

        mockQueryWrapper = new MockJenaQueryWrapper();

        mockThreadedQueryHandler = new MockThreadedQueryHandler();

        FetchSelectResponseTask fetcher = new FetchSelectResponseTask(mockThreadedQueryHandler,
                JenaQueryWrapperTest.ENDPOINT1,
                JenaQueryWrapperTest.SELECT_QUERY,
                doneSignal)
        {
            @Override
            protected JenaQueryWrapper getQueryWrapper()
            {
                return mockQueryWrapper;
            }
        };

        fetcher.run();

        // now check we've got the expected number of results
        int expectedResultsAdded = 7;
        assertEquals("One model added", expectedResultsAdded, mockThreadedQueryHandler.resultsAdded);
        assertEquals("Latch correctly set", 0, doneSignal.getCount());
    }

    @Test
    public void testInstantiation()
    {
        // for complete coverage, test creating the original object

        CountDownLatch doneSignal = new CountDownLatch(1);

        FetchConstructResponseTask fetcher = new FetchConstructResponseTask(mockThreadedQueryHandler,
                JenaQueryWrapperTest.ENDPOINT1,
                JenaQueryWrapperTest.SELECT_QUERY,
                doneSignal);

        JenaQueryWrapper wrapper = fetcher.getQueryWrapper();
        assertTrue(!(wrapper instanceof MockJenaQueryWrapper));
    }
}