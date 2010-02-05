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
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wf.arnos.controller.model.Endpoint;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class SimpleQueryHandler implements QueryHandlerInterface 
{
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(SimpleQueryHandler.class);

    /**
     * This implementation, simple contatinates all query results.
     * @param queryString SPARQL query to execute
     * @param endpoints List of endpoint urls to run the query against
     * @return An RDF model
     */
    public final String handleQuery(final String queryString, final List<Endpoint> endpoints)
    {
        LOG.debug("Querying against  " + endpoints.size() + " endpoints");
        Model model = ModelFactory.createDefaultModel();

        ArrayList<String> results = new ArrayList<String>();

        for (Endpoint ep : endpoints)
        {
            String url = ep.getLocation();
            LOG.debug("Querying " + url);
            Query query = QueryFactory.create(queryString);
            QueryEngineHTTP qehttp = QueryExecutionFactory.createServiceRequest(url, query);
            try
            {
                results.add(execSelect(queryString,url));
            }
            catch (QueryExceptionHTTP qhttpe)
            {
                LOG.error("Unable to execute query against " + url);
            }
            finally
            {
                qehttp.close();
            }
        }

        StringBuffer finalResult = new StringBuffer("");

        if (results.size() > 0)
        {

            int insertionPoint = results.get(0).toLowerCase().lastIndexOf("</"+TAG+">");

            finalResult.append(results.get(0).substring(0,insertionPoint));
            for (int i=0; i < results.size(); i++)
            {
                String s = extractResults(results.get(i));
                finalResult.append(s);
            }

            finalResult.append(results.get(0).substring(insertionPoint));
        }

        return finalResult.toString();
    }

    private static final String TAG = "results";

    public String execSelect(String querystring, String service)
    {
        HttpQuery httpQuery = makeHttpQuery(querystring, service);
        // TODO Allow other content types.
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec();

        String content = "";
        try {
            content = convertStreamToString(in);
        } catch (Exception ex) {
            LOG.error(ex);
        }

        return content;
    }

    private String extractResults(String s)
    {
        try {
            String newString = s;
            newString = newString.substring(newString.toLowerCase().indexOf("<"+TAG+">")+TAG.length()+2);
            newString = newString.substring(0, newString.toLowerCase().lastIndexOf("</"+TAG+">"));
            return newString;
        } catch (Exception ex) {
            LOG.error(ex);
            return s;
        }
    }

    private HttpQuery makeHttpQuery(String queryString, String service)
    {
        HttpQuery httpQuery = new HttpQuery(service) ;
        httpQuery.addParam(HttpParams.pQuery, queryString );

//        for ( Iterator<String> iter = defaultGraphURIs.iterator() ; iter.hasNext() ; )
//        {
//            String dft = iter.next() ;
//            httpQuery.addParam(HttpParams.pDefaultGraph, dft) ;
//        }
//        for ( Iterator<String> iter = namedGraphURIs.iterator() ; iter.hasNext() ; )
//        {
//            String name = iter.next() ;
//            httpQuery.addParam(HttpParams.pNamedGraph, name) ;
//        }

//        httpQuery.setBasicAuthentication(user, password) ;
        return httpQuery ;
    }

    public String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}
