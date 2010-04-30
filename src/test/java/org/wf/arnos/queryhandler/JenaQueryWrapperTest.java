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

import java.io.IOException;
import org.wf.arnos.utils.Sparql;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;
import org.wf.arnos.utils.LocalServer;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class JenaQueryWrapperTest {
    
    @Before
    public void setUp() throws Exception
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        LocalServer.start();
    }

    @After
    public void tearDown() {
        LocalServer.stop();
    }

    @Test
    public void testExecConstruct()
    {
        System.out.println("testExecConstruct");

        String query = Sparql.CONSTRUCT_QUERY_BOOKS;
        String result = Sparql.getResult(Sparql.ENDPOINT1_URL,query);

        String actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL);

        assertEquals(result, actualResult);

        // force post method
        JenaQueryWrapper.getInstance().setUrlLimit(10);

        JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL);

        assertEquals(result, actualResult);
    }

    @Test
    public void testExecSelect()
    {
        System.out.println("testExecSelect");

        String query = Sparql.SELECT_QUERY_BOOKS;
        String result = Sparql.getResult(Sparql.ENDPOINT1_URL,query);

        String actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL);

        assertEquals(result, actualResult);

        // force post method
        JenaQueryWrapper.getInstance().setUrlLimit(10);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL);

        assertEquals(result, actualResult);
    }

    @Test
    public void testExecAsk()
    {
        System.out.println("testExecAsk");

        String query = Sparql.ASK_QUERY_ALICE;
        String result = Sparql.getResult(Sparql.ENDPOINT1_URL,query);

        String actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL);

        assertEquals(result, actualResult);

        // force post method
        JenaQueryWrapper.getInstance().setUrlLimit(10);

        JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL);

        assertEquals(result, actualResult);
    }

    @Test
    public void testExecDescribe()
    {
        System.out.println("testExecAsk");

        String describeQuery1 = Sparql.DESCRIBE_QUERY_BOOK_2;
        String describeResult1 = Sparql.getResult(Sparql.ENDPOINT1_URL,describeQuery1);
        String describeQuery2 = Sparql.DESCRIBE_QUERY_BOOK_3;
        String describeResult2 = Sparql.getResult(Sparql.ENDPOINT1_URL,describeQuery2);

        String actualResult = JenaQueryWrapper.getInstance().execQuery(describeQuery1, Sparql.ENDPOINT1_URL);

        assertEquals(describeResult1, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(describeQuery2, Sparql.ENDPOINT1_URL);

        assertEquals(describeResult2, actualResult);

        // force post method
        JenaQueryWrapper.getInstance().setUrlLimit(10);

        actualResult = JenaQueryWrapper.getInstance().execQuery(describeQuery1, Sparql.ENDPOINT1_URL);

        assertEquals(describeResult1, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(describeQuery2, Sparql.ENDPOINT1_URL);

        assertEquals(describeResult2, actualResult);

    }

    @Test
    public void testStringToModel()
    {
        Model model = ModelFactory.createDefaultModel();

        String result = Sparql.getResult(Sparql.ENDPOINT1_URL, Sparql.CONSTRUCT_QUERY_BOOKS);

        Model actualModel = JenaQueryWrapper.getInstance().stringToModel(result);

        Resource book = model.createResource("http://example.org/book/book2");
        Property title = model.createProperty("http://purl.org/dc/elements/1.1/","title");

        // Can also create statements directly . . .
        Statement statement = model.createStatement(book,title,"Harry Potter and the Chamber of Secrets");

        assertTrue(actualModel.contains(statement));

        actualModel  = JenaQueryWrapper.getInstance().stringToModel(null);
        assertTrue(null != actualModel);
        assertTrue(actualModel.isEmpty());
        actualModel  = JenaQueryWrapper.getInstance().stringToModel("");
        assertTrue(null != actualModel);
        assertTrue(actualModel.isEmpty());
    }

    @Test
    public void testStringToBoolean()
    {
        String askResult = Sparql.getResult(Sparql.ENDPOINT1_URL, Sparql.ASK_QUERY_ALICE);

        boolean b = JenaQueryWrapper.getInstance().stringToBoolean(askResult);
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(askResult.replace("true", "false"));
        assertFalse(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(askResult.replace(">", ">\n"));
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(askResult.replace(">", "> \n  "));
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(askResult.toUpperCase());
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(askResult.replace(">", "&gt;"));
        assertFalse(b);

        assertFalse(JenaQueryWrapper.getInstance().stringToBoolean(null));
        assertFalse(JenaQueryWrapper.getInstance().stringToBoolean(""));
    }

    @Test
    public void testStringToResultSet()
    {
        String result = Sparql.getResult(Sparql.ENDPOINT1_URL, Sparql.SELECT_QUERY_BOOKS);

        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(result);

        int numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals(numResults,7);

        assertNull(JenaQueryWrapper.getInstance().stringToResultSet(null));
        assertNull(JenaQueryWrapper.getInstance().stringToResultSet(""));
    }

    @Test
    public void testconvertStreamToString() throws IOException
    {
        assertEquals("",JenaQueryWrapper.getInstance().convertStreamToString(null));
    }


    @Test
    public void testExceptionHandling()
    {
        System.out.println("testExceptionHandling");

        String query = Sparql.SELECT_QUERY_BOOKS;
        String result = "";

        // force get method
        JenaQueryWrapper.getInstance().setUrlLimit(10000);

        String actualResult = JenaQueryWrapper.getInstance().execQuery(query, "http://localhost:7999/nothing");

        assertEquals(result, actualResult);

        JenaQueryWrapper.getInstance().execQuery(query, null);

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query.substring(0, 20), Sparql.ENDPOINT1_URL+"error");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT1_URL+"error");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT4_URL+"toolong");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT4_URL+"interrupt");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT4_URL+"414");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT4_URL+"404");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, Sparql.ENDPOINT4_URL+"301");

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, "xyz://"+Sparql.ENDPOINT4_URL);

        assertEquals(result, actualResult);

        // force post method
        JenaQueryWrapper.getInstance().setUrlLimit(10);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, "xyz://"+Sparql.ENDPOINT4_URL);

        assertEquals(result, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(query, "http://localhost:7999/nothing");

        assertEquals(result, actualResult);
    }

}
