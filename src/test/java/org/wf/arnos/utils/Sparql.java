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
package org.wf.arnos.utils;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Notes:
 * Endpoint 1 and 3 generally provide valid data. Endpoint 2 never has any information.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class Sparql {

    // point the endpoints to our dummy Jetty server
    public static final String ENDPOINT1_URL = LocalServer.SERVER_URL+"/books/1";
    public static final String ENDPOINT2_URL = LocalServer.SERVER_URL+"/books/2";
    public static final String ENDPOINT3_URL = LocalServer.SERVER_URL+"/books/3";

    // maximum number of results we're expecting
    public static final int MAX_LIMIT = 10;

    // minimum number of results we're expecting
    public static final int MIN_LIMIT = 5;


    
    /*** SELECT QUERIES ***/
    /*=================*/

    public static final String SELECT_QUERY_BOOKS = "PREFIX books:   <http://example.org/book/>\n"
        + "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"
        + "SELECT ?book ?title\n"
        + "WHERE \n"
        + "  { ?book dc:title ?title }    LIMIT "+MAX_LIMIT;

    public static final String SELECT_QUERY_PEOPLE = "PREFIX people:   <http://example.org/people/>\n"
        + "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"
        + "SELECT ?name\n"
        + "WHERE \n"
        + "  { ?name dc:title ?title }    LIMIT "+MAX_LIMIT;

    public static final String SELECT_QUERY_PEOPLE_DISTINCT = "PREFIX people:   <http://example.org/people/>\n"
        + "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"
        + "SELECT DISTINCT ?name\n"
        + "WHERE \n"
        + "  { ?name dc:title ?title }    LIMIT "+MAX_LIMIT;

    public static final String SELECT_QUERY_PEOPLE_ORDERED = "PREFIX people:   <http://example.org/people/>\n"
        + "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"
        + "SELECT DISTINCT ?name\n"
        + "WHERE \n"
        + "  { ?name dc:title ?title } ORDER BY ?name ?title   LIMIT "+MAX_LIMIT;

    private static final String SELECT_RESULT_7_ITEMS = "<?xml version=\"1.0\"?>\n"
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
        + "        <literal>Harry Potter and the Deathly Hallows</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book2</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Harry Potter and the Chamber of Secrets</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book4</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Harry Potter and the Goblet of Fire</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book6</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Harry Potter and the Half-Blood Prince</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book1</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Harry Potter and the Philosopher's Stone</literal>\n"
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
        + "        <literal>Harry Potter and the Order of the Phoenix</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "  </results>\n"
        + "</sparql>";

    private static final String SELECT_RESULT_EMPTY_BOOKS = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head>\n"
        + "    <variable name=\"book\"/>\n"
        + "    <variable name=\"title\"/>\n"
        + "  </head>\n"
        + "  <results>\n"
        + "  </results>\n"
        + "</sparql>";

    private static final String SELECT_RESULT_EMPTY_PEOPLE = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head>\n"
        + "    <variable name=\"name\"/>\n"
        + "  </head>\n"
        + "  <results>\n"
        + "  </results>\n"
        + "</sparql>";

    private static final String SELECT_RESULT_2_PEOPLE = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head>\n"
        + "    <variable name=\"name\"/>\n"
        + "  </head>\n"
        + "  <results>\n"
        + "    <result>\n"
        + "      <binding name=\"name\">\n"
        + "        <literal>Beth</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"name\">\n"
        + "        <literal>Chris</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "  </results>\n"
        + "</sparql>";

    private static final String SELECT_RESULT_4_PEOPLE = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head>\n"
        + "    <variable name=\"name\"/>\n"
        + "  </head>\n"
        + "  <results>\n"
        + "    <result>\n"
        + "      <binding name=\"name\">\n"
        + "        <literal>Adam</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"name\">\n"
        + "        <literal>Beth</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"name\">\n"
        + "        <literal>Chris</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
         + "    <result>\n"
        + "      <binding name=\"name\">\n"
        + "        <literal>Daisy</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "  </results>\n"
        + "</sparql>";

    private static final String SELECT_RESULT_4_ADDITIONAL_ITEMS = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head>\n"
        + "    <variable name=\"book\"/>\n"
        + "    <variable name=\"title\"/>\n"
        + "  </head>\n"
        + "  <results>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book8</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Semantic Web Programming</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book9</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Semantic Web for the Working Ontologist: Effective Modeling in RDFS and OWL</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book10</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Semantic Web For Dummies</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "    <result>\n"
        + "      <binding name=\"book\">\n"
        + "        <uri>http://example.org/book/book11</uri>\n"
        + "      </binding>\n"
        + "      <binding name=\"title\">\n"
        + "        <literal>Practical RDF</literal>\n"
        + "      </binding>\n"
        + "    </result>\n"
        + "  </results>\n"
        + "</sparql>";

    private static Map<String,QueryMap> selectResults = new HashMap<String,QueryMap>();

    static
    {
        // associate endpoints, queries and results
        QueryMap temp = new QueryMap();
        temp.put(SELECT_QUERY_BOOKS,SELECT_RESULT_7_ITEMS);
        temp.put(SELECT_QUERY_PEOPLE,SELECT_RESULT_2_PEOPLE);
        temp.put(SELECT_QUERY_PEOPLE_DISTINCT,SELECT_RESULT_2_PEOPLE);
        temp.put(SELECT_QUERY_PEOPLE_ORDERED,SELECT_RESULT_2_PEOPLE);
        selectResults.put(ENDPOINT1_URL,temp);

        temp = new QueryMap();
        temp.put(SELECT_QUERY_BOOKS,SELECT_RESULT_EMPTY_BOOKS);
        temp.put(SELECT_QUERY_PEOPLE,SELECT_RESULT_4_PEOPLE);
        temp.put(SELECT_QUERY_PEOPLE_DISTINCT,SELECT_RESULT_4_PEOPLE);
        temp.put(SELECT_QUERY_PEOPLE_ORDERED,SELECT_RESULT_4_PEOPLE);
        selectResults.put(ENDPOINT2_URL, temp);

        temp = new QueryMap();
        temp.put(SELECT_QUERY_BOOKS,SELECT_RESULT_4_ADDITIONAL_ITEMS);
        temp.put(SELECT_QUERY_PEOPLE,SELECT_RESULT_EMPTY_PEOPLE);
        temp.put(SELECT_QUERY_PEOPLE_DISTINCT,SELECT_RESULT_EMPTY_PEOPLE);
        temp.put(SELECT_QUERY_PEOPLE_ORDERED,SELECT_RESULT_EMPTY_PEOPLE);
        selectResults.put(ENDPOINT3_URL, temp);
    }



    /*** CONSTRUCT QUERIES ***/
    /*===================*/

    public static final String CONSTRUCT_QUERY_BOOKS = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n"
        + "CONSTRUCT { $book dc:title $title }\n"
        + "WHERE\n"
        + "  { $book dc:title $title }    LIMIT "+MAX_LIMIT;
    private static final String CONSTRUCT_RESULT_7_BOOKS = "<?xml version=\"1.0\"?>\n"
        + "<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"
        + "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
        + "    xmlns:ns=\"http://example.org/ns#\"\n"
        + "    xmlns=\"http://example.org/book/\">\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book3\">\n"
        + "    <dc:title>Harry Potter and the Prisoner Of Azkaban</dc:title>\n"
        + "  </rdf:Description>\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book7\">\n"
        + "    <dc:title>Harry Potter and the Deathly Hallows</dc:title>\n"
        + "  </rdf:Description>\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book2\">\n"
        + "    <dc:title>Harry Potter and the Chamber of Secrets</dc:title>\n"
        + "  </rdf:Description>\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book5\">\n"
        + "    <dc:title>Harry Potter and the Order of the Phoenix</dc:title>\n"
        + "  </rdf:Description>\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book4\">\n"
        + "    <dc:title>Harry Potter and the Goblet of Fire</dc:title>\n"
        + "  </rdf:Description>\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book6\">\n"
        + "    <dc:title>Harry Potter and the Half-Blood Prince</dc:title>\n"
        + "  </rdf:Description>\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book1\">\n"
        + "    <dc:title>Harry Potter and the Philosopher's Stone</dc:title>\n"
        + "  </rdf:Description>\n"
        + "</rdf:RDF>";

    private static Map<String,QueryMap> constructResults = new HashMap<String,QueryMap>();

    static
    {
        QueryMap temp = new QueryMap();
        temp.put(CONSTRUCT_QUERY_BOOKS,CONSTRUCT_RESULT_7_BOOKS);
        constructResults.put(ENDPOINT1_URL,temp);
    }




    /*** ASK QUERIES ***/
    /*==============*/

    public static final String ASK_QUERY_ALICE = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
        + "ASK  { ?x foaf:name  \"Alice\" }";

    public static final String ASK_QUERY_BOB = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
        + "ASK  { ?x foaf:name  \"Bob\" }";

    private static final String ASK_RESULT_TRUE = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head></head>\n"
        + "  <results>\n"
        + "    <boolean>true</boolean>\n"
        + "  </results>\n"
        + "</sparql>";

    private static final String ASK_RESULT_FALSE = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head></head>\n"
        + "  <results>\n"
        + "    <boolean>false</boolean>\n"
        + "  </results>\n"
        + "</sparql>";

    private static Map<String,QueryMap> askResults = new HashMap<String,QueryMap>();

    static
    {
        QueryMap temp = new QueryMap();
        temp.put(ASK_QUERY_ALICE,ASK_RESULT_TRUE);
        temp.put(ASK_QUERY_BOB,ASK_RESULT_FALSE);
        askResults.put(ENDPOINT1_URL, temp );

        temp = new QueryMap();
        temp.put(ASK_QUERY_ALICE,ASK_RESULT_FALSE);
        temp.put(ASK_QUERY_BOB,ASK_RESULT_TRUE);
        askResults.put(ENDPOINT2_URL, temp );

        temp = new QueryMap();
        temp.put(ASK_QUERY_ALICE,ASK_RESULT_FALSE);
        temp.put(ASK_QUERY_BOB,ASK_RESULT_FALSE);
        askResults.put(ENDPOINT3_URL, temp );
    }



    /*** DESCRIBE QUERIES ***/
    /*==================*/

    public static final String DESCRIBE_QUERY_BOOK_2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
        + "DESCRIBE <http://example.org/book/book2> WHERE {}";

    public static final String DESCRIBE_QUERY_BOOK_3 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n"
        + "DESCRIBE <http://example.org/book/book3> WHERE {}";

    private static final String DESCRIBE_RESULT_BOOK_2 = "<?xml version=\"1.0\"?>\n"
        + "<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"
        + "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
        + "    xmlns:ns=\"http://example.org/ns#\"\n"
        + "    xmlns=\"http://example.org/book/\">\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book2\">\n"
        + "    <dc:creator rdf:parseType=\"Resource\">\n"
        + "      <vcard:N rdf:parseType=\"Resource\">\n"
        + "        <vcard:Given>Joanna</vcard:Given>\n"
        + "        <vcard:Family>Rowling</vcard:Family>\n"
        + "      </vcard:N>\n"
        + "      <vcard:FN>J.K. Rowling</vcard:FN>\n"
        + "    </dc:creator>\n"
        + "    <dc:title>Harry Potter and the Chamber of Secrets</dc:title>\n"
        + "  </rdf:Description>\n"
        + "</rdf:RDF>";

    private static final String DESCRIBE_RESULT_BOOK_2_ADDITIONAL = "<?xml version=\"1.0\"?>\n"
        + "<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"
        + "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
        + "    xmlns:ns=\"http://example.org/ns#\"\n"
        + "    xmlns=\"http://example.org/book/\">\n"
        + "  <rdf:Description rdf:about=\"http://example.org/book/book2\">\n"
        + "    <dc:publisher>Scholastic Paperbacks</dc:publisher>\n"
        + "    <dc:title>Harry Potter and the Chamber of Secrets</dc:title>\n"
        + "    <dc:description>Grade 3-8-Fans of the phenomenally popular Harry \n"
        + "Potter and the Sorcerer's Stone (Scholastic, 1998) won't be disappointed \n"
        + "when they rejoin Harry, now on break after finishing his first year at Hogwarts \n"
        + "School of Witchcraft and Wizardry. Reluctantly spending the summer with the \n"
        + "Dursleys, his mean relatives who fear and detest magic, Harry is soon whisked \n"
        + "away by his friends Ron, Fred, and George Weasley, who appear at his window \n"
        + "in a flying Ford Anglia to take him away to enjoy the rest of the holidays with \n"
        + "their very wizardly family. Things don't go as well, though, when the school term \n"
        + "begins. Someone, or something, is (literally) petrifying Hogwarts' residents one by \n"
        + "one and leaving threatening messages referring to a Chamber of Secrets and \n"
        + "an heir of Slytherin.</dc:description>\n"
        + "  </rdf:Description>\n"
        + "</rdf:RDF>";

    private static final String DESCRIBE_RESULT_EMPTY_BOOK = "<?xml version=\"1.0\"?>\n"
        + "<rdf:RDF\n"
        + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "    xmlns:vcard=\"http://www.w3.org/2001/vcard-rdf/3.0#\"\n"
        + "    xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
        + "    xmlns:ns=\"http://example.org/ns#\"\n"
        + "    xmlns=\"http://example.org/book/\">\n"
        + "</rdf:RDF>";


    private static Map<String,QueryMap> describeResults = new HashMap<String,QueryMap>();

    static
    {
        QueryMap temp = new QueryMap();
        temp.put(DESCRIBE_QUERY_BOOK_2, DESCRIBE_RESULT_BOOK_2);
        temp.put(DESCRIBE_QUERY_BOOK_3, DESCRIBE_RESULT_EMPTY_BOOK);
        describeResults.put(ENDPOINT1_URL, temp);

        temp = new QueryMap();
        temp.put(DESCRIBE_QUERY_BOOK_2, DESCRIBE_RESULT_EMPTY_BOOK);
        temp.put(DESCRIBE_QUERY_BOOK_3, DESCRIBE_RESULT_EMPTY_BOOK);
        describeResults.put(ENDPOINT2_URL, temp);

        temp = new QueryMap();
        temp.put(DESCRIBE_QUERY_BOOK_2, DESCRIBE_RESULT_BOOK_2_ADDITIONAL);
        temp.put(DESCRIBE_QUERY_BOOK_3, DESCRIBE_RESULT_EMPTY_BOOK);
        describeResults.put(ENDPOINT3_URL, temp);
    }

    public static String getResult(String endpoint, String query)
    {
        String result = null;
        if (selectResults.containsKey(endpoint)) result = selectResults.get(endpoint).get(query);
        if (result != null) return result;

        if (constructResults.containsKey(endpoint)) result = constructResults.get(endpoint).get(query);
        if (result != null) return result;

        if (askResults.containsKey(endpoint)) result = askResults.get(endpoint).get(query);
        if (result != null) return result;

        if (describeResults.containsKey(endpoint)) result = describeResults.get(endpoint).get(query);
        return result;
    }

    @Test
    public void test()
    {
        assertEquals(Sparql.SELECT_RESULT_7_ITEMS,Sparql.getResult(Sparql.ENDPOINT1_URL,Sparql.SELECT_QUERY_BOOKS));
        assertEquals(Sparql.DESCRIBE_RESULT_BOOK_2_ADDITIONAL,Sparql.getResult(Sparql.ENDPOINT3_URL,Sparql.DESCRIBE_QUERY_BOOK_2));
        assertEquals(Sparql.DESCRIBE_RESULT_BOOK_2_ADDITIONAL,Sparql.getResult(Sparql.ENDPOINT3_URL,Sparql.DESCRIBE_QUERY_BOOK_2));
    }
}