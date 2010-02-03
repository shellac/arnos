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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.Project;
import org.wf.arnos.controller.model.ProjectsManager;
import org.wf.arnos.logger.Logger;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
@Controller
@RequestMapping(value = "/projects/{projectName}")
public class EndpointController
{
    /**
     * Logger.
     */
    @Logger
    private Log logger;

    /**
     * The projects manager, autowired in.
     */
    @Autowired
    private transient ProjectsManager manager;

    /**
     * List all the endpoints associated with a project.
     * @param projectName Name of project
     * @param model Supplied model to return data
     * @return View name
     */
    @RequestMapping(value = "/endpoints")
    public final String listEndpoints(@PathVariable final String projectName, Model model)
    {
        if (StringUtils.isNotEmpty(projectName))
        {
            logger.info("Being called with " + projectName);

            ArrayList <String> endpoints = new ArrayList<String>();

            Project p = manager.getProject(projectName);

            if (p != null)
            {
                List<Endpoint> endpointList = p.getEndpoints();
                for (Endpoint ep : endpointList)
                {
                    endpoints.add(ep.getLocation());
                }
            }

            model.addAttribute("endpoints", endpoints);
        }
        return "";
    }

}
