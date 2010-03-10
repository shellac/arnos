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
package org.wf.arnos.queryhandler.mocks;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.queryhandler.ThreadedQueryHandler;
import org.wf.arnos.queryhandler.task.FetchBooleanResponseTask;
import org.wf.arnos.queryhandler.task.FetchModelResponseTask;
import org.wf.arnos.queryhandler.task.FetchResultSetResponseTask;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class MockThreadPoolTaskExecutor extends ThreadPoolTaskExecutor
{
    ThreadedQueryHandler handler;

    public MockThreadPoolTaskExecutor(ThreadedQueryHandler handler)
    {
        this.handler = handler;
    }

    public int selectTasksRunning = 0;

    public int constructTasksRunning = 0;

    public int askTasksRunning = 0;

    public int describeTasksRunning = 0;

    public void reset()
    {
        selectTasksRunning = 0;
        constructTasksRunning = 0;
        askTasksRunning = 0;
        describeTasksRunning = 0;
    }

    public void execute(Runnable task)
    {
        if (task instanceof FetchModelResponseTask)
        {
            constructTasksRunning++;
        }
        if (task instanceof FetchResultSetResponseTask)
        {
            selectTasksRunning++;
        }
        if (task instanceof FetchBooleanResponseTask)
        {
            askTasksRunning++;
        }

        handler.getLatch().countDown();
    }
}
