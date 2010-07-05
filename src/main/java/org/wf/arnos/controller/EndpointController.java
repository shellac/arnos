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

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.ProjectsManager;
import org.wf.arnos.exception.ResourceNotFoundException;
import org.wf.arnos.logger.Logger;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
@Controller
@RequestMapping(value = "/{projectName}/endpoints")
public class EndpointController
{
    /**
     * Logger.
     */
    @Logger
    public static transient Log logger;

    /**
     * The projects manager, autowired in.
     */
    @Autowired
    public transient ProjectsManager manager;

    /**
     * The cache handler, autowired in.
     */
    @Autowired(required = false)
    public transient CacheHandlerInterface cacheHandler;

    /**
     * List all the endpoints associated with a project.
     * @param projectName Name of project
     * @param model Supplied model to return data
     * @return View name
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public final String listEndpoints(@PathVariable final String projectName, final Model model)
    {
        checkProject(projectName);

        if (logger.isDebugEnabled())
        {
            logger.debug("Listing endpoints for '" + projectName + "'");
        }

        List<Endpoint> endpointList = manager.getEndpoints(projectName);

        model.addAttribute("endpoints", endpointList);

        return "";
    }


    /**
     * Method to add endpoint to a given project.
     * @param projectName Name of project
     * @param endpoint uri of endpoint to add
     * @param model Supplied model to return data
     * @return String representing view name
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public final String addEndpoint(@PathVariable final String projectName,
                                                      @RequestParam("url") final String endpoint,
                                                      final Model model)
    {
        String result = "";
        String message = "";

        if (StringUtils.isEmpty(endpoint))
        {
            message = "Missing endpoint";
        }
        else
        {
            checkProject(projectName);

            manager.addEndpoint(projectName, endpoint);

            Endpoint ep = new Endpoint(endpoint);
            result = ep.getIdentifier();
            message = result;
        }

        if (logger.isDebugEnabled()) logger.debug(message);

        model.addAttribute("message", message);
        return "";
    }

    /**
     * Method to remove a specified endpoint from a given project.
     * @param projectName Name of project
     * @param endpoint uri of endpoint to remove
     * @param model Supplied model to return data
     * @return String representing view name
     */
    @RequestMapping(value = "/remove", method = RequestMethod.DELETE)
    public final String removeEndpoint(@PathVariable final String projectName,
                                                       @RequestParam("url") final String endpoint,
                                                      final Model model)
    {
        String message = "";

        if (StringUtils.isEmpty(endpoint))
        {
            message = "Missing endpoint";
        }
        else
        {
            checkProject(projectName);

            manager.removeEndpoint(projectName, endpoint);

            message = "endpoint '" + endpoint + "' removed";
        }

        if (logger.isDebugEnabled()) logger.debug(message);

        model.addAttribute("message", message);
        return "";
    }


    /**
     * Flush caches for a given endpoint
     * @param projectName
     * @param endpointList
     * @param model
     * @return
     */
    @RequestMapping(value = "/flush", method = RequestMethod.GET)
    public final String flushEndpoint(@PathVariable final String projectName,
                                                       @RequestParam("url") final String endpoint,
                                                       final Model model)
    {
        String result = "";
        String message = "";

        if (StringUtils.isEmpty(endpoint))
        {
            message = "Missing endpoint";
        }
        else
        {
            checkProject(projectName);

            if (logger.isDebugEnabled()) logger.debug("Flushing cache for " + endpoint);

            Endpoint ep = new Endpoint(endpoint);
            String epId = ep.getIdentifier();
            if (cacheHandler != null)
            {
                cacheHandler.flush(projectName, epId);
            }

            message = "Cache flushed for " + endpoint;
        }

        model.addAttribute("message", message);
        return "";
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
