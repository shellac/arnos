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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import static org.junit.Assert.*;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class JenaQueryWrapperTest {

    // Jetty server configuration
    private Server server;
    final static int PORT_NUMBER = 9090;

    // point the endpoints to our dummy Jetty server
    private static final String ENDPOINT1 = "/books/1";
    private static final String ENDPOINT2 = "/books/2";
    private static final String ENDPOINT3 = "/books/2";

    public static final String ENDPOINT1_URL = "http://localhost:"+PORT_NUMBER+ENDPOINT1;
    public static final String ENDPOINT2_URL = "http://localhost:"+PORT_NUMBER+ENDPOINT2;
    public static final String ENDPOINT3_URL = "http://localhost:"+PORT_NUMBER+ENDPOINT3;

    // maximum number of results we're expecting
    public static final int MAX_LIMIT = 10;

    // minimum number of results we're expecting
    public static final int MIN_LIMIT = 5;


    
    /*** SELECT QUERIES ***/
    /*=================*/

    private static final String SELECT_QUERY_1 = "PREFIX books:   <http://example.org/book/>\n"+
        "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"+
        "SELECT ?book ?title\n"+
        "WHERE \n"+
        "  { ?book dc:title ?title }    LIMIT "+MAX_LIMIT;
    private static final String SELECT_RESULT_1 = "<?xml version=\"1.0\"?>\n" +
        "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"+
        "  <head>\n"+
        "    <variable name=\"book\"/>\n"+
        "    <variable name=\"title\"/>\n"+
        "  </head>\n"+
        "  <results>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book7</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Deathly Hallows</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book2</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Chamber of Secrets</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book4</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Goblet of Fire</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book6</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Half-Blood Prince</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book1</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Philosopher's Stone</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book3</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Prisoner Of Azkaban</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "    <result>\n"+
        "      <binding name=\"book\">\n"+
        "        <uri>http://example.org/book/book5</uri>\n"+
        "      </binding>\n"+
        "      <binding name=\"title\">\n"+
        "        <literal>Harry Potter and the Order of the Phoenix</literal>\n"+
        "      </binding>\n"+
        "    </result>\n"+
        "  </results>\n"+
        "</sparql>";

    private static Map<String,String []> selectResults = new HashMap<String,String []>();

    static
    {
        selectResults.put(ENDPOINT1_URL,new String [] {SELECT_QUERY_1,SELECT_RESULT_1} );
    }

    public static String getSelectQuery(String endpoint) { return getQuery(endpoint, selectResults); }
    public static String getSelectQuery() { return getQuery(selectResults); }
    public static String getSelectResult(String endpoint) { return getResult(endpoint, selectResults); }
    public static String getSelectResult() { return getResult(selectResults); }



    /*** CONSTRUCT QUERIES ***/
    /*===================*/

    private static final String CONSTRUCT_QUERY_1 = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"+
        "CONSTRUCT { $book dc:title $title }\n"+
        "WHERE\n"+
        "  { $book dc:title $title }    LIMIT "+MAX_LIMIT;

    private static final String CONSTRUCT_RESULT_1 = "<?xml version=\"1.0\"?>\n"+
        "<rdf:RDF\n"+
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
        "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"+
        "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
        "    xmlns:ns=\"http://example.org/ns#\"\n"+
        "    xmlns=\"http://example.org/book/\">\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book3\">\n"+
        "    <dc:title>Harry Potter and the Prisoner Of Azkaban</dc:title>\n"+
        "  </rdf:Description>\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book7\">\n"+
        "    <dc:title>Harry Potter and the Deathly Hallows</dc:title>\n"+
        "  </rdf:Description>\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book2\">\n"+
        "    <dc:title>Harry Potter and the Chamber of Secrets</dc:title>\n"+
        "  </rdf:Description>\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book5\">\n"+
        "    <dc:title>Harry Potter and the Order of the Phoenix</dc:title>\n"+
        "  </rdf:Description>\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book4\">\n"+
        "    <dc:title>Harry Potter and the Goblet of Fire</dc:title>\n"+
        "  </rdf:Description>\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book6\">\n"+
        "    <dc:title>Harry Potter and the Half-Blood Prince</dc:title>\n"+
        "  </rdf:Description>\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book1\">\n"+
        "    <dc:title>Harry Potter and the Philosopher's Stone</dc:title>\n"+
        "  </rdf:Description>\n"+
        "</rdf:RDF>";

    private static Map<String,String []> constructResults = new HashMap<String,String []>();

    static
    {
        constructResults.put(ENDPOINT1_URL,new String [] {CONSTRUCT_QUERY_1,CONSTRUCT_RESULT_1} );
    }

    public static String getConstructQuery(String endpoint) { return getQuery(endpoint, constructResults); }
    public static String getConstructQuery() { return getQuery(constructResults); }
    public static String getConstructResult(String endpoint) { return getResult(endpoint, constructResults); }
    public static String getConstructResult() { return getResult(constructResults); }



    /*** ASK QUERIES ***/
    /*==============*/

    private static final String ASK_QUERY_1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
        "ASK  { ?x foaf:name  \"Alice\" }";
    private static final String ASK_RESULT_1 = "<?xml version=\"1.0\"?>\n"+
        "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"+
        "  <head></head>\n"+
        "  <results>\n"+
        "    <boolean>true</boolean>\n"+
        "  </results>\n"+
        "</sparql>";

    private static final String ASK_QUERY_2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
        "ASK  { ?x foaf:name  \"Bob\" }";
    private static final String ASK_RESULT_2 = "<?xml version=\"1.0\"?>\n"+
        "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"+
        "  <head></head>\n"+
        "  <results>\n"+
        "    <boolean>false</boolean>\n"+
        "  </results>\n"+
        "</sparql>";

    private static Map<String,String []> askResults = new HashMap<String,String []>();

    static
    {
        askResults.put(ENDPOINT1_URL,new String [] {ASK_QUERY_1,ASK_RESULT_1} );
        askResults.put(ENDPOINT2_URL,new String [] {ASK_QUERY_2,ASK_RESULT_2} );
    }

    public static String getAskQuery(String endpoint) { return getQuery(endpoint, askResults); }
    public static String getAskQuery() { return getQuery(askResults); }
    public static String getAskResult(String endpoint) { return getResult(endpoint, askResults); }
    public static String getAskResult() { return getResult(askResults); }



    /*** DESCRIBE QUERIES ***/
    /*==================*/

    private static final String DESCRIBE_QUERY_1 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
        "DESCRIBE <http://example.org/book/book2>";
    public static final String DESCRIBE_RESULT_1 = "<?xml version=\"1.0\"?>\n"+
        "<rdf:RDF\n"+
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
        "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"+
        "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
        "    xmlns:ns=\"http://example.org/ns#\"\n"+
        "    xmlns=\"http://example.org/book/\">\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book2\">\n"+
        "    <dc:creator rdf:parseType=\"Resource\">\n"+
        "      <vcard:N rdf:parseType=\"Resource\">\n"+
        "        <vcard:Given>Joanna</vcard:Given>\n"+
        "        <vcard:Family>Rowling</vcard:Family>\n"+
        "      </vcard:N>\n"+
        "      <vcard:FN>J.K. Rowling</vcard:FN>\n"+
        "    </dc:creator>\n"+
        "    <dc:title>Harry Potter and the Chamber of Secrets</dc:title>\n"+
        "  </rdf:Description>\n"+
        "</rdf:RDF>";

    private static final String DESCRIBE_QUERY_2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
        "DESCRIBE <http://example.org/book/book2>";
    public static final String DESCRIBE_RESULT_2 = "<?xml version=\"1.0\"?>\n"+
        "<rdf:RDF\n"+
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
        "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"+
        "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
        "    xmlns:ns=\"http://example.org/ns#\"\n"+
        "    xmlns=\"http://example.org/book/\">\n"+
        "</rdf:RDF>\n";

    private static final String DESCRIBE_QUERY_3 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
        "DESCRIBE <http://example.org/book/book2>";
    public static final String DESCRIBE_RESULT_3 = "<?xml version=\"1.0\"?>\n"+
        "<rdf:RDF\n"+
        "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
        "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"+
        "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
        "    xmlns:ns=\"http://example.org/ns#\"\n"+
        "    xmlns=\"http://example.org/book/\">\n"+
        "  <rdf:Description rdf:about=\"http://example.org/book/book2\">\n"+
        "    <dc:creator rdf:parseType=\"Resource\">\n"+
        "      <vcard:N rdf:parseType=\"Resource\">\n"+
        "        <vcard:Given>Joanna</vcard:Given>\n"+
        "        <vcard:Family>Rowling</vcard:Family>\n"+
        "      </vcard:N>\n"+
        "      <vcard:FN>J.K. Rowling</vcard:FN>\n"+
        "    </dc:creator>\n"+
        "    <dc:title>Harry Potter and the Chamber of Secrets</dc:title>\n"+
        "  </rdf:Description>\n"+
        "</rdf:RDF>";
    private static Map<String,String []> describeResults = new HashMap<String,String []>();

    static
    {
        describeResults.put(ENDPOINT1_URL,new String [] {DESCRIBE_QUERY_1, DESCRIBE_RESULT_1});
        describeResults.put(ENDPOINT2_URL,new String [] {DESCRIBE_QUERY_2, DESCRIBE_RESULT_2});
        describeResults.put(ENDPOINT3_URL,new String [] {DESCRIBE_QUERY_3, DESCRIBE_RESULT_3});
    }

    public static String getDescribeQuery(String endpoint) { return getQuery(endpoint, describeResults); }
    public static String getDescribeQuery() { return getQuery(describeResults); }
    public static String getDescribeResult(String endpoint) { return getResult(endpoint, describeResults); }
    public static String getDescribeResult() { return getResult(describeResults); }

    // generic map lookup functions
    private static String getQuery(String endpoint, Map<String,String []> queryMap) { return queryMap.get(endpoint)[0]; }
    private static String getQuery(Map<String,String []> queryMap) { return queryMap.get(ENDPOINT1_URL)[0]; }
    private static String getResult(String endpoint, Map<String,String []> queryMap) { return queryMap.get(endpoint)[1]; }
    private static String getResult(Map<String,String []> queryMap) { return queryMap.get(ENDPOINT1_URL)[1]; }


    Handler fakeEndpointHandler =new AbstractHandler()
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException
        {
            System.out.println("Target is " +target);

            response.setContentType("text/xml");
            response.setStatus(HttpServletResponse.SC_OK);

            String query = request.getParameter("query");

            String thisEndpoint = "http://localhost:"+PORT_NUMBER+target;
            String result = "";
            String queryType = "Unknown";

            if (query.indexOf("CONSTRUCT ") > 0)
            {
                queryType = "CONSTRUCT";
                result = getConstructResult(thisEndpoint);
            }
            else if (query.indexOf("DESCRIBE ") > 0)
            {
                queryType = "DESCRIBE";
                result = getDescribeResult(thisEndpoint);
            }
            else if (query.indexOf("SELECT ") > 0)
            {
                queryType = "SELECT";
                result = getSelectResult(thisEndpoint);
            }
            else
            {
                queryType = "ASK";
                result = getAskResult(thisEndpoint);
            }

            System.out.println(queryType + " request");
            if (result == null) 
            {
                throw new RuntimeException("No response found for " + queryType + " query type send to "+target);
            }

            response.getWriter().print(result);

            ((Request)request).setHandled(true);
        }
    };

    @Before
    public void setUp() throws Exception
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        server = new Server(PORT_NUMBER);
        server.setHandler(fakeEndpointHandler);
        server.start();
    }

    @After
    public void tearDown() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExecConstruct()
    {
        System.out.println("testExecConstruct");

        String actualResult = JenaQueryWrapper.getInstance().execQuery(CONSTRUCT_QUERY_1, ENDPOINT1_URL);

        assertEquals(CONSTRUCT_RESULT_1, actualResult);
    }

    @Test
    public void testExecSelect()
    {
        System.out.println("testExecSelect");

        String actualResult = JenaQueryWrapper.getInstance().execQuery(SELECT_QUERY_1, ENDPOINT1_URL);

        assertEquals(SELECT_RESULT_1, actualResult);
    }

    @Test
    public void testExecAsk()
    {
        System.out.println("testExecAsk");

        String askQuery = getAskQuery(ENDPOINT1_URL);
        String actualResult = JenaQueryWrapper.getInstance().execQuery(askQuery, ENDPOINT1_URL);

        assertEquals(ASK_RESULT_1, actualResult);
    }

    @Test
    public void testExecDescribe()
    {
        System.out.println("testExecAsk");

        String describeQuery = getDescribeQuery(ENDPOINT1_URL);
        String describeResult = getDescribeResult(ENDPOINT1_URL);
        String actualResult = JenaQueryWrapper.getInstance().execQuery(describeQuery, ENDPOINT1_URL);

        assertEquals(describeResult, actualResult);

        actualResult = JenaQueryWrapper.getInstance().execQuery(describeQuery, ENDPOINT2_URL);

        assertEquals("", actualResult);
    }

    @Test
    public void testStringToModel()
    {
        Model model = ModelFactory.createDefaultModel();

        Model actualModel = JenaQueryWrapper.getInstance().stringToModel(CONSTRUCT_RESULT_1);

        Resource book = model.createResource("http://example.org/book/book2");
        Property title = model.createProperty("http://purl.org/dc/elements/1.1/","title");

        // Can also create statements directly . . .
        Statement statement = model.createStatement(book,title,"Harry Potter and the Chamber of Secrets");

        assertTrue(actualModel.contains(statement));
    }

    @Test
    public void testStringToBoolean()
    {
        String askResult = getAskResult(ENDPOINT1_URL);

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
    }

    @Test
    public void testStringToResultSet()
    {
        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(SELECT_RESULT_1);

        int numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals(numResults,7);
    }
}