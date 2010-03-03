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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
    public static final String ENDPOINT1 = "http://localhost:"+PORT_NUMBER+"/books";
    public static final String ENDPOINT2 = "http://localhost:"+PORT_NUMBER+"/education";

    // maximum number of results we're expecting
    public static final int MAX_LIMIT = 10;

    // minimum number of results we're expecting
    public static final int MIN_LIMIT = 5;

    public static final String SELECT_QUERY = "PREFIX books:   <http://example.org/book/>\n"+
        "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"+
        "SELECT ?book ?title\n"+
        "WHERE \n"+
        "  { ?book dc:title ?title }    LIMIT "+MAX_LIMIT;

    public static final String CONSTRUCT_QUERY = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"+
        "CONSTRUCT { $book dc:title $title }\n"+
        "WHERE\n"+
        "  { $book dc:title $title }    LIMIT "+MAX_LIMIT;

    public static final String ASK_QUERY = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"+
        "ASK  { ?x foaf:name  \"Alice\" }";

    public static final String EXPECTED_SELECT_RESULT = "<?xml version=\"1.0\"?>\n" +
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

    public static final String EXPECTED_CONSTRUCT_RESULT = "<?xml version=\"1.0\"?>\n"+
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

    public static final String EXPECTED_ASK_RESULT = "<?xml version=\"1.0\"?>\n"+
        "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"+
        "  <head></head>\n"+
        "  <results>\n"+
        "    <boolean>true</boolean>\n"+
        "  </results>\n"+
        "</sparql>";


    Handler fakeEndpointHandler =new AbstractHandler()
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException
        {
            System.out.println("Target is " +target);

            response.setContentType("text/xml");
            response.setStatus(HttpServletResponse.SC_OK);

            String query = request.getParameter("query");

            if (query.indexOf("CONSTRUCT ") > 0)
            {
                System.out.println("CONSTRUCT request");
                response.getWriter().print(EXPECTED_CONSTRUCT_RESULT);
            }
            else if (query.indexOf("DESCRIBE ") > 0)
            {
                System.out.println("DESCRIBE request");
    //            response.getWriter().print(EXPECTED_DESCRIBE_RESULT);
            }
            else if (query.indexOf("SELECT ") > 0)
            {
                System.out.println("SELECT request");
                response.getWriter().print(EXPECTED_SELECT_RESULT);
            }
            else
            {
                System.out.println("ASK request");
                response.getWriter().print(EXPECTED_ASK_RESULT);
            }

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

        String actualResult = JenaQueryWrapper.getInstance().execQuery(CONSTRUCT_QUERY, ENDPOINT1);

        assertEquals(EXPECTED_CONSTRUCT_RESULT, actualResult);
    }

    @Test
    public void testStringToModel()
    {
        // TODO: Find a way of mocking up a real http connection - Jersey?
        Model model = ModelFactory.createDefaultModel();

        Model actualModel = JenaQueryWrapper.getInstance().stringToModel(EXPECTED_CONSTRUCT_RESULT);
        
        Resource book = model.createResource("http://example.org/book/book2");
        Property title = model.createProperty("http://purl.org/dc/elements/1.1/","title");

        // Can also create statements directly . . .
        Statement statement = model.createStatement(book,title,"Harry Potter and the Chamber of Secrets");

//        RDFNode n = null;
//
//        Selector selector = new SimpleSelector(book,null,n);
//
//        StmtIterator it = actualModel.listStatements(selector);
//        while (it.hasNext())
//        {
//            Statement s = it.next();
//        }
        assertTrue(actualModel.contains(statement));
    }

    @Test
    public void testExecSelect()
    {
        System.out.println("testExecSelect");

        String actualResult = JenaQueryWrapper.getInstance().execQuery(SELECT_QUERY, ENDPOINT1);

        assertEquals(EXPECTED_SELECT_RESULT, actualResult);
    }

    @Test
    public void testStringToResultSet()
    {
        ResultSet results = JenaQueryWrapper.getInstance().stringToResultSet(EXPECTED_SELECT_RESULT);

        int numResults = 0;
        while (results.hasNext())
        {
            results.next();
            numResults++;
        }
        assertEquals(numResults,7);
    }

    @Test
    public void testExecAsk()
    {
        System.out.println("testExecAsk");

        String actualResult = JenaQueryWrapper.getInstance().execQuery(ASK_QUERY, ENDPOINT1);

        assertEquals(EXPECTED_ASK_RESULT, actualResult);
    }

    @Test
    public void testStringToBoolean()
    {
        boolean b = JenaQueryWrapper.getInstance().stringToBoolean(EXPECTED_ASK_RESULT);
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(EXPECTED_ASK_RESULT.replace("true", "false"));
        assertFalse(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(EXPECTED_ASK_RESULT.replace(">", ">\n"));
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(EXPECTED_ASK_RESULT.replace(">", "> \n  "));
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(EXPECTED_ASK_RESULT.toUpperCase());
        assertTrue(b);
        b = JenaQueryWrapper.getInstance().stringToBoolean(EXPECTED_ASK_RESULT.replace(">", "&gt;"));
        assertFalse(b);
    }
}