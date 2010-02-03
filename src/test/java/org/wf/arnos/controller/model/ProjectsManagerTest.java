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
    public void testAddAndRemoveProject() {
        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjects().size());

        manager.addProject(null);
        
        assertEquals("Null no added",0, manager.getProjects().size());

        manager.addProject(p1);

        assertEquals("First project added",1, manager.getProjects().size());

        manager.addProject(p2);

        assertEquals("Second project added",2, manager.getProjects().size());

        manager.removeProject(p1);

        assertEquals("First project removed",1, manager.getProjects().size());

        manager.removeProject(p1);

        assertEquals("Deplicate removal",1, manager.getProjects().size());

        manager.removeProject(null);

        assertEquals("Removing null",1, manager.getProjects().size());

        manager.removeProject(p2);

        assertEquals("Second project removed",0, manager.getProjects().size());
    }

    @Test
    public void testGetProject()
    {
        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjects().size());

        Project result = manager.getProject(null);

        assertNull("Testing null getProject value", result);

        result = manager.getProject(projectName1);

        assertNull("Testing get missing project", result);

        manager.addProject(p1);
        manager.addProject(p2);

        result = manager.getProject(projectName1);

        assertEquals("Obtained project 1", p1, result);

        result = manager.getProject(projectName2);

        assertEquals("Obtained project 2", p2, result);
    }

    @Test
    public void testProjectManagerPersistance()
    {
        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjects().size());

        manager.addProject(p1);
        manager.addProject(p2);

        assertEquals("Projects added",2, manager.getProjects().size());

        manager = new ProjectsManager();

        assertEquals("Persistance still not enabled",0, manager.getProjects().size());

        // now enable persistance
        manager = new ProjectsManager(fileName);

        assertEquals("Persistant storage is empty",0, manager.getProjects().size());

        manager.addProject(p1);
        manager.addProject(p2);

        manager = new ProjectsManager();

        assertEquals("Persistance not enabled",0, manager.getProjects().size());

        manager.setFileName(fileName);

        assertEquals("Persistance enabled",2, manager.getProjects().size());

        manager.removeProject(p1);

        assertEquals("Project removed",1, manager.getProjects().size());

        manager = new ProjectsManager(fileName);
        
        assertEquals("Only one project remaining",1, manager.getProjects().size());


    }

}