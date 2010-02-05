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
package org.wf.arnos.controller;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.ProjectsManager;
import org.wf.arnos.exception.ResourceNotFoundException;
import org.wf.arnos.logger.Logger;
import org.wf.arnos.queryhandler.QueryHandlerInterface;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
@Controller
public class QueryController
{
    /**
     * Logger.
     */
    @Logger
    private static transient Log logger;

    /**
     * The projects manager, autowired in.
     */
    @Autowired
    private transient ProjectsManager manager;

    /**
     * The query handler.
     */
    @Autowired
    private transient QueryHandlerInterface queryHandler;

    /**
     * Primary SPARQL Endpoint of the arnos service. Runs the provided
     * query over all defined endpoints for that project
     * @param projectName
     * @param query
     * @param model
     * @return
     */
    @RequestMapping(value = "/projects/{projectName}/query")
    public final void executeQuery(@PathVariable final String projectName,
                                                 @RequestParam("query") final String query,
                                                 java.io.Writer writer)
    {
        checkProject(projectName);

        logger.info("Passing to " + queryHandler.getClass() );

        List<Endpoint> endpoints = manager.getEndpoints(projectName);
        String s = queryHandler.handleQuery(query, endpoints);

        try
        {
        writer.append(s);
        writer.flush();
        }
        catch (Exception e){e.printStackTrace();}
    }


    /**
     * Checks for the existance of the project, throwing a runtime exception
     * if not found.
     * @param projectName Name of project, provided from request
     */
    private void checkProject(final String projectName)
    {
        if (StringUtils.isEmpty(projectName))
        {
            if (logger.isDebugEnabled()) logger.debug("Missing project name");
            throw new ResourceNotFoundException();
        }

        if (manager.hasProject(projectName))
        {
            return;
        }

        if (logger.isDebugEnabled()) logger.debug("Listing failed, no project named '" + projectName + "'");
        throw new ResourceNotFoundException();
    }
}
