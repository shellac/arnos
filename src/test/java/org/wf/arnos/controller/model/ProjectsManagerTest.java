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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ProjectsManagerTest {
    ProjectsManager manager;
    String endpoint1 = "www.somewhere.com";
    String endpoint2 ="http://otherplace.org";
    String projectName1 = "TestProject";
    String projectName2 = "Another Project";
    Project p1;
    Project p2;
    static String fileName = "./persistance.xml";

    @Before
    public void setUp()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");

        p1 = new Project(projectName1);
        p1.addEndpoint(endpoint1);
        p1.addEndpoint(endpoint2);

        p2 = new Project(projectName2);
        p2.addEndpoint(endpoint1);
    }

    @AfterClass
    public static void tearDownClass()
    {
        // remove persistant storage class if needed
        File f = new File(fileName);
        f.delete();
    }


    @Test
    public void testMisc()
    {
        manager = new ProjectsManager();
        assertEquals(null,manager.getFileName());
        manager.setFileName(fileName);
        assertEquals(fileName,manager.getFileName());
    }

    @Test
    public void testAddAndRemoveProject() {
        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjectCount());

        Project p = null;
        manager.addProject(p);
        
        assertEquals("Null no added",0, manager.getProjectCount());

        manager.addProject(p1);

        assertEquals("First project added",1, manager.getProjectCount());

        manager.addProject(p2);

        assertEquals("Second project added",2, manager.getProjectCount());

        assertEquals(false,manager.addProject((String)null));
        assertEquals(false,manager.addProject(""));

        assertEquals(2,manager.getEndpoints(projectName1).size());
        manager.removeEndpoint(projectName1+"other", endpoint1);
        assertEquals(2,manager.getEndpoints(projectName1).size());
        manager.removeEndpoint(projectName1, endpoint1);
        assertEquals(1,manager.getEndpoints(projectName1).size());
        assertEquals(null,manager.getEndpoints(projectName1+"other"));

        manager.removeProject(p1);

        assertEquals("First project removed",1, manager.getProjectCount());

        manager.removeProject(p1);

        assertEquals("Deplicate removal",1, manager.getProjectCount());

        manager.removeProject(p);

        assertEquals("Removing null",1, manager.getProjectCount());

        manager.removeProject(p2);

        assertEquals("Second project removed",0, manager.getProjectCount());
    }

    @Test
    public void testGetProject()
    {
        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjectCount());

        boolean result = manager.hasProject(null);

        assertFalse("Testing null getProject value", result);

        result = manager.hasProject(projectName1);

        assertFalse("Testing get missing project", result);

        manager.addProject(p1);
        manager.addProject(p2);

        result = manager.hasProject(projectName1);

        assertTrue("Obtained project 1", result);

        result = manager.hasProject(projectName2);

        assertTrue("Obtained project 2",  result);
    }

    @Test
    public void testProjectManagerPersistance()
    {
        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjectCount());

        manager.addProject(p1);
        manager.addProject(p2);

        assertEquals("Projects added",2, manager.getProjectCount());

        manager = new ProjectsManager();

        assertEquals("Persistance still not enabled",0, manager.getProjectCount());

        // now enable persistance
        manager = new ProjectsManager(fileName);

        assertEquals("Persistant storage is empty",0, manager.getProjectCount());

        manager.addProject(p1);
        manager.addProject(p2);

        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjectCount());

        manager.setFileName(fileName);

        assertEquals("Persistance enabled",2, manager.getProjectCount());

        manager.removeProject(p1);

        assertEquals("Project removed",1, manager.getProjectCount());

        manager = new ProjectsManager(fileName);
        
        assertEquals("Only one project remaining",1, manager.getProjectCount());


    }

    @Test
    public void testManagementViaProjectNames()
    {
        manager = new ProjectsManager();
        manager.addProject(p1);
        manager.addProject(p2);

        assertEquals("Two project added",2, manager.getProjectCount());

        String uniqueName = "project_"+new Date().getTime();
        boolean result = manager.addProject(uniqueName);

        assertTrue("Successfully added third project", result);
        assertEquals("Unique third project added",3, manager.getProjectCount());

        result = manager.addProject(projectName1);

        assertFalse("Duplicate project name not added", result);

        result = manager.removeProject("x"+uniqueName);

        assertFalse("Unknown project not removed", result);

        assertTrue(manager.hasProject(projectName2));
        
        result = manager.removeProject(projectName2);

        assertTrue("Project 2 removed", result);

        assertFalse(manager.hasProject(projectName2));

        result = manager.removeProject(uniqueName);
        assertTrue("Unique project name removed", result);

        assertEquals("One project remaining",1, manager.getProjectCount());
        assertTrue(manager.hasProject(projectName1));
    }

    @Test
    public void testListAllProjects()
    {
        manager = new ProjectsManager();
        assertEquals(0,manager.listAllProjects().size());

        manager.addProject(p1);
        manager.addProject(p2);

        assertEquals(2,manager.listAllProjects().size());
    }
}