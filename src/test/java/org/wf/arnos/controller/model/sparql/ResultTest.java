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
package org.wf.arnos.controller.model.sparql;

import com.hp.hpl.jena.sparql.function.library.matches;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.wf.arnos.utils.Sparql;
import static org.junit.Assert.*;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ResultTest {


    public static String SELECT_RESULT_7_BOOKS = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head>\n"
        + "    <variable name=\"book\"/>\n"
        + "    <variable name=\"title\"/>\n"
        + "  </head>\n"
        + "  <results>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book7</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Harry Potter &amp; the Deathly Hallows</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book2</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal xml:lang='en'>Harry Potter and the Chamber of Secrets</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book4</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal datatype='http://www.w3.org/2001/XMLSchema#string'>Harry Potter and the Goblet of Fire</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book6</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <bnode>r1</bnode>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book1</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <bnode>r2</bnode>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book3</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Harry Potter and the Prisoner Of Azkaban</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book5</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <bnode>r2</bnode>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "  </results>\n"
        + "</sparql>";

    @Test
    public void testHashCode()
    {
        List<org.wf.arnos.controller.model.sparql.Result> results = new ArrayList<org.wf.arnos.controller.model.sparql.Result>();

        HashMap <Result,Integer> hash = new HashMap<Result,Integer>();

        ResultSet resultSet = ResultSetFactory.fromXML(SELECT_RESULT_7_BOOKS);
        int i = 0 ;
        while (resultSet.hasNext())
        {
            QuerySolution sol = resultSet.next();
            Result r = new Result(sol);
            results.add(r);
            hash.put(r, i++);
        }

        assertEquals("Hashmap is the same size as Set",results.size(),hash.size());

        assertFalse(results.get(0).equals(null));

        for (i =1; i<results.size(); i++)
        {
            Result previous = results.get(i-1);
            Result r = results.get(i);

            boolean compare = r.equals(previous);
            assertTrue(compare == previous.equals(r));

            int hash1 = r.hashCode();
            r.bindings.clear();
            int hash2 = r.hashCode();
            r.values.clear();
            int hash3 = r.hashCode();

            assertEquals(true,hash1 != hash2 && hash1 != hash3 && hash2 != hash3);
        }
    }

    @Test
    public void testXMLGeneration()
    {
        ResultSet resultSet = ResultSetFactory.fromXML(SELECT_RESULT_7_BOOKS);
        int i = 0 ;
        String totalXml = "";
        while (resultSet.hasNext())
        {
            QuerySolution sol = resultSet.next();
            Result r = new Result(sol);

            String xml = r.toXML();
            assertTrue(Sparql.validateXML(xml));

            totalXml+= xml;
        }

        // test bnode generation
        assertEquals(3, SubstrCount(totalXml,"<bnode>"));

        Pattern p = Pattern.compile("<bnode>[^<]*</bnode>");
        Matcher m = p.matcher(totalXml);
        HashMap <String,String> matches = new HashMap<String,String>();

        boolean result = m.find();
        // Loop through and create a new String
        // with the replacements
        while(result) {
            String match = totalXml.substring(m.start(),m.end());
            matches.put(match, "1");
            result = m.find();
        }

        assertEquals("2 unique blank nodes", 2, matches.size());

        // test additional attributes are retained
        assertTrue(totalXml.contains("datatype"));
        assertTrue(totalXml.contains("xml:lang"));
    }

    private int SubstrCount(String haystack, String needle)
    {
        int lastIndex = 0;
        int count =0;

        while(lastIndex != -1)
        {

               lastIndex = haystack.indexOf(needle,lastIndex+1);

               if( lastIndex != -1){
                     count ++;
              }
        }
        return count;
    }
}