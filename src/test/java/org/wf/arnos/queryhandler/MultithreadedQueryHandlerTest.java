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
import com.hp.hpl.jena.query.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.utils.LocalServer;
import org.wf.arnos.utils.Sparql;

/**
 * See http://today.java.net/pub/a/today/2003/08/06/multithreadedTests.html
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class MultithreadedQueryHandlerTest {

    CountDownLatch doneSignal;
    ThreadPoolTaskExecutor taskExecutor;

    static int limit = 10;
    static HashMap<Endpoint,Integer> matrix = new HashMap<Endpoint,Integer>();
    static Endpoint[] endpointArray;
    static ThreadedQueryHandler queryHandler;
    static Query sparqlQuery;
    static
    {
         matrix.put(new Endpoint(Sparql.ENDPOINT1_URL), 7);
         matrix.put(new Endpoint(Sparql.ENDPOINT2_URL), 0);
         matrix.put(new Endpoint(Sparql.ENDPOINT3_URL), 4);

        // copy endpoints to array
        endpointArray = new Endpoint[matrix.size()];
        matrix.keySet().toArray(endpointArray);

        queryHandler = new ThreadedQueryHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(matrix.size());
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        String query = Sparql.SELECT_QUERY_BOOKS;
        sparqlQuery = QueryFactory.create(query);
    }


    static int failed = 0;;
    static ArrayList<String> messages = new ArrayList<String>();

    static void setFail(String msg)
    {
        failed++;
        messages.add(msg);
    }

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
    public void testSimUsers()
    {
        System.out.println("testSimUsers");

        int numberOfTests = 200;

        doneSignal = new CountDownLatch(numberOfTests);
        
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.initialize();
        taskExecutor.setCorePoolSize(10);
        for (int i=0; i<numberOfTests;i++)
        {
            taskExecutor.execute(new Request(doneSignal), 100);
        }

        // block until all threads have finished
        try
        {
            doneSignal.await();
        }
        catch (InterruptedException ex)
        {
            fail(ex.toString());
        }

        if (failed > 0)
        {
            for (String message : messages)
            {
                System.err.println(message);
            }
            fail(failed + " threads failed");
        }
    }

    private class Request implements Runnable
    {
        CountDownLatch latch;
        String id;

        private Request(CountDownLatch latch)
        {
            this.latch = latch;
            this.id = "Request-"+latch.getCount();
        }
        
        public void run()
        {
            try
            {
                // choose a random number of endpoints
                long seed = System.currentTimeMillis()+latch.getCount();
                print("Seed is " +  System.currentTimeMillis() + "+" + latch.getCount() + " = " + seed);
                Random rand = new Random(seed);

                int randNum = rand.nextInt(matrix.size()) + 1;

                List<Endpoint> selectedEndpoints = new ArrayList<Endpoint>();

                int expectedResults = 0;
                for (int i = 0; i < randNum; i++)
                {
                    int nextRandomNum = rand.nextInt(matrix.size()-1) + 1;

                    Endpoint ep = endpointArray[nextRandomNum];
                    selectedEndpoints.add(ep);
                    expectedResults += matrix.get(ep);
                }

                if (expectedResults > limit) expectedResults = limit;
                print("Issuing request over " + randNum + " endpoints, expected "+ expectedResults + " results");

                //issue request
                issueRequest(selectedEndpoints,expectedResults);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                // decrement latch
                latch.countDown();
            }

        }

        private void issueRequest(List<Endpoint> endpoints, int expectedResults)
        {

            String result = queryHandler.handleSelect(sparqlQuery, endpoints);
            ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(result);

            int numResults = 0;
            while (results.hasNext())
            {
                results.next();
                numResults++;
            }

            print("Got " + numResults + " results");
            
            if (numResults != expectedResults)
            {
                setFail("["+id+"] "+ "Got " + numResults + " results, expected " + expectedResults);
            }
        }

        private void print(String msg)
        {
             System.out.println("["+id+"] "+msg);
        }
    }
}
