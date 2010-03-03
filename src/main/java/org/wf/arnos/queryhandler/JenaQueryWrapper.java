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
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wf.arnos.exception.ArnosRuntimeException;

/**
 * This class provides an customized version of Jena's execSelect and execConstruct methods.
 * We do this because it allows us to insert a layer of caching of the endpoint results.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class JenaQueryWrapper implements QueryWrapperInterface
{
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(JenaQueryWrapper.class);

   /**
    * A handle to the unique Singleton instance.
    */
   private static final JenaQueryWrapper SINGLETON;

   static
   {
        try
        {
            // Perform initialization here
            SINGLETON = new JenaQueryWrapper();
        }
        catch (Throwable e)
        {
            throw new ArnosRuntimeException(e);
        }
   }

   /**
    * @return The unique instance of this class.
    */
   public static JenaQueryWrapper getInstance()
   {
        return SINGLETON;
    }

   /**
    * The constructor.
    */
   protected JenaQueryWrapper()
   {
       // made private to prevent others from instantiating this class.
   }

    /**
     * Executes the provided query against a given endpoint.
     * This method returns the raw result as a string
     * @param querystring SPARQL query
     * @param service URL endpoint
     * @return Query result as a string
     */
    public final String execQuery(final String querystring, final String service)
    {
        HttpQuery httpQuery = makeHttpQuery(querystring, service);
        httpQuery.setAccept(HttpParams.contentTypeResultsXML);
        InputStream in = httpQuery.exec();

        String content = "";
        try
        {
            content = convertStreamToString(in);
        }
        catch (Exception ex)
        {
            LOG.error("Unable to convert input stream to string", ex);
        }
        return content;
    }

    /**
     * Converts a sparql select query result into a resultSet object.
     * @param s Raw xml result
     * @return ResultSet object
     */
    public final ResultSet stringToResultSet(final String s)
    {
        return ResultSetFactory.fromXML(s);
    }

    /**
     * Converts a string into a model.
     * @param s Raw xml result
     * @return Model representation
     */
    public final Model stringToModel(final String s)
    {
        Model model = GraphUtils.makeJenaDefaultModel();
        StringReader in = new StringReader(s);
        model.read(in, null);
        return model;
    }

    /**
     * Interprets the response of an ASK query to a boolean value.
     * @param s Raw xml result
     * @return Boolean value representation
     */
    public final boolean stringToBoolean(final String s)
    {
        String check = s.toLowerCase(Locale.ENGLISH);
        check = check.replace("\n", "");
        check = check.replace("\r", "");
        check = check.replace(" ", "");
        if (check.indexOf("<boolean>true</boolean>") > 0) return true;
        return false;
    }

    /**
     * Constructs a HttpQuery object.
     * @param queryString query parameter
     * @param service Endpoint URL
     * @return HttpQuery object
     */
    private HttpQuery makeHttpQuery(final String queryString, final String service)
    {
        HttpQuery httpQuery = new HttpQuery(service);
        httpQuery.addParam(HttpParams.pQuery, queryString);
        return httpQuery;
    }

    /**
     * Utility function to convert input stream to a string object.
     * @param is InputStream
     * @return Contents of inputstream as a String
     * @throws IOException Throws exception
     */
    public static String convertStreamToString(final InputStream is) throws IOException
    {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        if (is == null)
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            String line = "";

            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null)
                {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line);
                }
            }
            finally
            {
                is.close();
            }

            return sb.toString();
        }
    }
}
