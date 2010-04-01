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
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.Params;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.util.Convert;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;
import com.hp.hpl.jena.util.FileUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wf.arnos.exception.ArnosRuntimeException;

/**
 * This class provides an customized version of Jena's execSelect and execConstruct methods.
 * We do this because it allows us to insert a layer of caching of the endpoint 
 * results and configure the url connection directly.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class JenaQueryWrapper implements QueryWrapperInterface
{
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(JenaQueryWrapper.class);

    private static final String CONTENT_TYPE_RESULT = HttpParams.contentTypeResultsXML;

    private static final int CONNECTION_TIMEOUT = 2 * 1000;
    private static final int REQUEST_TIMEOUT = 10 * 1000;

    private static transient HttpURLConnection httpConnection = null;

    private static final int URL_LIMIT = 2 * 1024;

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
       System.setProperty("sun.net.client.defaultConnectTimeout", "1000");
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
        try
        {
            InputStream in = exec(querystring, service);
            return convertStreamToString(in);
        }
        catch (Exception ex)
        {
            LOG.error("Error querying "+service+". " + ex.getMessage());
            return "";
        }
    }

    /**
     * Converts a sparql select query result into a resultSet object.
     * @param s Raw xml result
     * @return ResultSet object
     */
    public final ResultSet stringToResultSet(final String s)
    {
        if (s != null && s.length() > 0)
        {
            return ResultSetFactory.fromXML(s);
        }

        return null;
    }

    /**
     * Converts a string into a model.
     * @param s Raw xml result
     * @return Model representation
     */
    public final Model stringToModel(final String s)
    {
        Model model = GraphUtils.makeJenaDefaultModel();

        if (s != null && s.length() > 0)
        {
            StringReader in = new StringReader(s);
            model.read(in, null);
        }
        return model;
    }

    /**
     * Interprets the response of an ASK query to a boolean value.
     * @param s Raw xml result
     * @return Boolean value representation
     */
    public final boolean stringToBoolean(final String s)
    {
        if (s == null || s.length() < 1) return false;
        
        String check = s.toLowerCase(Locale.ENGLISH);
        check = check.replace("\n", "");
        check = check.replace("\r", "");
        check = check.replace(" ", "");
        if (check.indexOf("<boolean>true</boolean>") > 0) return true;
        return false;
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


    /**
     * Return whether this request will go by GET or POST.
     * @param queryString Query string
     * @param serviceURL Endpoint address
     * @return boolean
     */
    private boolean usesPOST(final String queryString, final String serviceURL)
    {
        Params p = new Params();
        p.addParam(HttpParams.pQuery, queryString);

        String s = p.httpString();

        return serviceURL.length() + s.length() >= URL_LIMIT;
    }


    /** Execute the operation
     * @return Model    The resulting model
     * @throws QueryExceptionHTTP
     */
    private InputStream exec(final String queryString, final String serviceURL) throws QueryExceptionHTTP
    {
        Params p = new Params();
        p.addParam(HttpParams.pQuery, queryString);

        try {
            if (usesPOST(queryString, serviceURL)) return execPost(p, serviceURL);
            return execGet(p, serviceURL);
        } catch (QueryExceptionHTTP httpEx)
        {
            LOG.trace("Exception in exec", httpEx);
            throw httpEx;
        }
        catch (JenaException jEx)
        {
            LOG.trace("JenaException in exec", jEx);
            throw jEx ;
        }
    }


    private InputStream execGet(final Params p, final String serviceURL) throws QueryExceptionHTTP
    {
        URL target = null ;

        try {
            target = new URL(serviceURL+"?"+p.httpString()) ;
        }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: "+malEx) ; }

        LOG.debug("GET "+target.toExternalForm()) ;

        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestProperty("Accept", CONTENT_TYPE_RESULT) ;
            httpConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            httpConnection.setReadTimeout(REQUEST_TIMEOUT);
            httpConnection.setDoInput(true);
            httpConnection.connect();
            try
            {
                return execCommon();
            }
            catch (QueryExceptionHTTP qEx)
            {
                // Back-off and try POST if something complain about long URIs
                // Broken
                if (qEx.getResponseCode() == 414 /*HttpServletResponse.SC_REQUEST_URI_TOO_LONG*/ )
                    return execPost(p, serviceURL);
                throw qEx;
            }
        }
        catch (java.net.ConnectException connEx)
        { throw new QueryExceptionHTTP(QueryExceptionHTTP.NoServer, "Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new QueryExceptionHTTP(ioEx); }
    }

    private InputStream execPost(final Params p, final String serviceURL) throws QueryExceptionHTTP
    {
        URL target = null;

        try { target = new URL(serviceURL); }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: " + malEx); }

        LOG.debug("POST "+target.toExternalForm()) ;

        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestMethod("POST") ;
            httpConnection.setRequestProperty("Accept", CONTENT_TYPE_RESULT) ;
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") ;
            httpConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            httpConnection.setReadTimeout(REQUEST_TIMEOUT);
            httpConnection.setDoOutput(true) ;

            boolean first = true ;
            OutputStream out = httpConnection.getOutputStream();
            String s = p.httpString();
            out.write(s.getBytes());
            out.flush() ;
            httpConnection.connect() ;
            return execCommon() ;
        }
        catch (java.net.ConnectException connEx)
        { throw new QueryExceptionHTTP(-1, "Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new QueryExceptionHTTP(ioEx); }
    }

    private InputStream execCommon() throws QueryExceptionHTTP
    {
        try {
            int responseCode = httpConnection.getResponseCode() ;
            String responseMessage = Convert.decWWWForm(httpConnection.getResponseMessage()) ;

            // 1xx: Informational
            // 2xx: Success
            // 3xx: Redirection
            // 4xx: Client Error
            // 5xx: Server Error

            if ( 300 <= responseCode && responseCode < 400 )
            {
                throw new QueryExceptionHTTP(responseCode, responseMessage) ;
            }

            // Other 400 and 500 - errors

            if ( responseCode >= 400 )
            {
                throw new QueryExceptionHTTP(responseCode, responseMessage) ;
            }

            // Request suceeded
            InputStream in = httpConnection.getInputStream() ;

            if ( false )
            {
                // Dump the reply
                Map<String,List<String>> map = httpConnection.getHeaderFields() ;
                for ( Iterator<String> iter = map.keySet().iterator() ; iter.hasNext() ; )
                {
                    String k = iter.next();
                    List<String> v = map.get(k) ;
                    System.out.println(k+" = "+v) ;
                }

                // Dump response body
                StringBuffer b = new StringBuffer(1000) ;
                byte[] chars = new byte[1000] ;
                while(true)
                {
                    int x = in.read(chars) ;
                    if ( x < 0 ) break ;
                    b.append(new String(chars, 0, x, FileUtils.encodingUTF8)) ;
                }
                System.out.println(b.toString()) ;
                System.out.flush() ;
                // Reset
                in = new ByteArrayInputStream(b.toString().getBytes(FileUtils.encodingUTF8)) ;
            }
            return in ;
        }
        catch (IOException ioEx)
        {
            throw new QueryExceptionHTTP(ioEx) ;
        }
        catch (JenaException rdfEx)
        {
            throw new QueryExceptionHTTP(rdfEx) ;
        }
    }
}
