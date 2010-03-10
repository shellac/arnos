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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.queryhandler.mocks.MockThreadPoolTaskExecutor;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ThreadedQueryHandlerTest {

    // maximum number of results we're expecting
    private static final int MAX_LIMIT = 10;

    // minimum number of results we're expecting
    private static final int MIN_LIMIT = 5;

    @Before
    public void setUp()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
    }

    @Test
    public void testTaskInitalization()
    {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint(JenaQueryWrapperTest.ENDPOINT1_URL));
        endpoints.add(new Endpoint(JenaQueryWrapperTest.ENDPOINT2_URL));
        
        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();

        MockThreadPoolTaskExecutor executor = new MockThreadPoolTaskExecutor(queryHandler);
        queryHandler.setTaskExecutor(executor);

        Query selectQuery = QueryFactory.create(JenaQueryWrapperTest.getSelectQuery());
        Query constructQuery = QueryFactory.create(JenaQueryWrapperTest.getConstructQuery());
        Query askQuery = QueryFactory.create(JenaQueryWrapperTest.getAskQuery());

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

        endpoints.add(new Endpoint(JenaQueryWrapperTest.ENDPOINT3_URL));

        results = queryHandler.handleAsk(askQuery, endpoints);

        assertEquals(0, executor.selectTasksRunning);
        assertEquals(0, executor.constructTasksRunning);
        assertEquals(3, executor.askTasksRunning);
        assertEquals(0, executor.describeTasksRunning);
    }

    @Test
    public void testHandleSelect()
    {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // add test endpoints - unit test relies on successful connection with following endpoints
        endpoints.add(new Endpoint("http://services.data.gov.uk/analytics/sparql"));
        endpoints.add(new Endpoint("http://services.data.gov.uk/education/sparql"));

        ThreadedQueryHandler queryHandler = new ThreadedQueryHandler();
    }

    @Test
    public void testHandleDescribe()
    {

    }


}