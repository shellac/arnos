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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;


/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class LocalServer {
    
    // Jetty server configuration
    private static Server server;
    private static final int PORT_NUMBER = 9090;
    public static final String SERVER_URL = "http://localhost:"+PORT_NUMBER;
    
    static Handler fakeEndpointHandler =new AbstractHandler()
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
                result = Sparql.getConstructResult(thisEndpoint);
            }
            else if (query.indexOf("DESCRIBE ") > 0)
            {
                queryType = "DESCRIBE";
                result = Sparql.getDescribeResult(thisEndpoint);
            }
            else if (query.indexOf("SELECT ") > 0)
            {
                queryType = "SELECT";
                result = Sparql.getSelectResult(thisEndpoint);
            }
            else
            {
                queryType = "ASK";
                result = Sparql.getAskResult(thisEndpoint);
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

    /**
     * Starts the webserver with our custom handler.
     */
    public static void start()
    {
        try
        {
        server = new Server(PORT_NUMBER);
        server.setHandler(fakeEndpointHandler);
        server.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops the webserver.
     */
    public static void stop()
    {
        try
        {
            server.stop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
