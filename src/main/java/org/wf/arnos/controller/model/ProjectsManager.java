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
package org.wf.arnos.controller.model;

import org.apache.commons.io.FileUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This class manages all interactions with projects.
 * It also includes a persistant storage capability
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
@ManagedResource
public class ProjectsManager
{
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ProjectsManager.class);

    /**
     * The list of managed projects.
     */
    private List<Project> projects;


    /**
     * Returns all projects managed.
     * @return the projects
     */
    public final List<Project> getProjects()
    {
        return projects;
    }

    /**
     * Returns a list of projects to aid management via JMX
     * @return
     */
    @ManagedOperation
    public final List<String> listAllProjects()
    {
        ArrayList<String> projectNames = new ArrayList<String>();
        for (Project p : projects)
        {
            projectNames.add(p.getName());
        }

        Collections.sort(projectNames);
        return projectNames;
    }
    /**
     * Sets list of managed projects.
     * @param projectsList the projects to set
     */
    public final void setProjects(final List<Project> projectsList)
    {
        this.projects = projectsList;
    }

    /**
     * File name for persistant storage.
     */
    private String fileName = null;

    /**
     * Set the filename for storing the model.
     * @param s File name
     */
    public final void setFileName(final String s)
    {
        fileName = s;
        load();
    }

    /**
     * Retrieve file name.
     * @return name of the file
     */
    public final String getFileName()
    {
        return fileName;
    }

    /**
     * Simple constructor.
     */
    ProjectsManager()
    {
        projects = new ArrayList<Project>();
    }

    /**
     * Simple constructor with persistant storage.
     * @param storageLocation File name of persistant storage to use
     */
    ProjectsManager(final String storageLocation)
    {
        projects = new ArrayList<Project>();
        setFileName(storageLocation);
    }

    /**
     * Add a new project to the manager.
     * @param p Project to add
     * @return boolean indicating success
     */
    public final boolean addProject(final Project p)
    {
        if (p == null) return false;

        if (getProjects().contains(p)) return false;

        getProjects().add(p);

        boolean b = true;

        if (StringUtils.isNotEmpty(fileName))
        {
            // update storage
            b = save();
        }
        return b;
    }

    /**
     * Add a new project using a project name.
     * This method is exposed as a JMX operation.
     * @param projectName Name of project
     * @return indicating success
     */
    @ManagedOperation
    public final boolean addProject(final String projectName)
    {
        if (StringUtils.isEmpty(projectName)) return false;

        Project p = new Project(projectName);

        return addProject(p);
    }

    /**
     * Removes a managed project.
     * @param p Project to remove
     * @return boolean indicating success
     */
    public final boolean removeProject(final Project p)
    {
        if (projects.contains(p))
        {
            projects.remove(p);

            boolean b = true;

            if (StringUtils.isNotEmpty(fileName))
            {
                // update storage
                b = save();
            }
            return b;
        }

        return false;
    }

    /**
     * Removes a managed project using the project name as a lookup.
     * @param projectName Name of project to remove
     * @return boolean indicating success
     */
    @ManagedOperation
    public final boolean removeProject(final String projectName)
    {
        if (StringUtils.isEmpty(projectName)) return false;

        for (Project p : projects)
        {
            if (p.getName().equalsIgnoreCase(projectName))
            {
                projects.remove(p);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a managed project.
     * @param projectName Name of project to search for
     * @return Managed project if found, <code>null</code> otherwise
     */
    public final Project getProject(final String projectName)
    {
        if (StringUtils.isEmpty(projectName)) return null;

        for (Project p : projects)
        {
            if (p.getName().equalsIgnoreCase(projectName)) return p;
        }
        return null;
    }

    /**
     * This method will push internal model to a persistant storage layer.
     * @return boolean value indicating if save was successful
     */
    private boolean save()
    {
        XStream xstream = new XStream();
        String xmlString = xstream.toXML(getProjects());

        try
        {
            File file = new File(fileName);

            FileUtils.writeStringToFile(file, xmlString);
        }
        catch (IOException ioe)
        {
            LOG.error("Unable to save model to persistant storage layer ("
                    + fileName + ")", ioe);
            return false;
        }
        return true;
    }

    /**
     * Loads the model from the persistant storage layer.
     * @return true if the model was loaded successfully
     */
    private boolean load()
    {
        File file = new File(fileName);

        try
        {
            String xmlString = FileUtils.readFileToString(file);

            XStream xstream = new XStream();
            try
            {
                setProjects((List<Project>) xstream.fromXML(xmlString));
                if (LOG.isDebugEnabled()) LOG.debug("Projects model loaded");
                return true;
            }
            catch (XStreamException xse)
            {
                if (LOG.isInfoEnabled()) LOG.info(xmlString);
                LOG.error("Unable to parse xml string", xse);
                return false;
            }

        }
        catch (IOException ioe)
        {
            LOG.warn("Unable to load data from persistant storage layer ("
                    + file.getAbsolutePath() + ")\n" + ioe.getMessage());
            return false;
        }
    }
}
