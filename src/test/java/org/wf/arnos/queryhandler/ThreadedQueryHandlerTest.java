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
import com.hp.hpl.jena.query.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import static org.junit.Assert.*;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.queryhandler.mocks.MockThreadPoolTaskExecutor;
import org.wf.arnos.utils.LocalServer;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ThreadedQueryHandlerTest {
    Query selectQuery = QueryFactory.create(Sparql.getSelectQuery());
    Query constructQuery = QueryFactory.create(Sparql.getConstructQuery());
    Query askQuery = QueryFactory.create(Sparql.getAskQuery());

    // maximum number of results we're expecting
    private static final int MAX_LIMIT = 10;

    // minimum number of results we're expecting
    private static final int MIN_LIMIT = 5;

    @BeforeClass
    public static void setUp() throws Exception
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        LocalServer.start();
    }

    @AfterClass
    public static void tearDown() {
        LocalServer.stop();
    }


    @Test
    public void testTaskInitalization()
    {
        System.out.println("testTaskInitalization");

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(Sparql.ENDPOINT1_URL));
        endpoints.add(new Endpoint(Sparql.ENDPOINT2_URL));
        
        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();

        MockThreadPoolTaskExecutor executor = new MockThreadPoolTaskExecutor(queryHandler);
        queryHandler.setTaskExecutor(executor);

        String results = queryHandler.handleSelect(selectQuery, endpoints);

        assertEquals(2, executor.selectTasksRunning);
        assertEquals(0, executor.constructTasksRunning);
        assertEquals(0, executor.askTasksRunning);
        assertEquals(0, executor.describeTasksRunning);

        executor.reset();

        results = queryHandler.handleConstruct(constructQuery, endpoints);

        assertEquals(0, executor.selectTasksRunning);
        assertEquals(2, executor.constructTasksRunning);
        assertEquals(0, executor.askTasksRunning);
        assertEquals(0, executor.describeTasksRunning);

        executor.reset();

        endpoints.add(new Endpoint(Sparql.ENDPOINT3_URL));

        results = queryHandler.handleAsk(askQuery, endpoints);

        assertEquals(0, executor.selectTasksRunning);
        assertEquals(0, executor.constructTasksRunning);
        assertEquals(3, executor.askTasksRunning);
        assertEquals(0, executor.describeTasksRunning);
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
    }

    @Test
    public void testHandleDescribe()
    {
        System.out.println("testHandleDescribe");
    }


}