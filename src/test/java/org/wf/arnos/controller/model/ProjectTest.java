/*
 *  © University of Bristol
 */

package org.wf.arnos.controller.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class ProjectTest {

    String endpoint1 = "www.somewhere.com";
    String endpoint2 ="http://otherplace.org";
    String projectName1 = "TestProject";
    String projectName2 = "Another Project";
    Project p1;
    Project p2;

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
    
    @Test
    public void testSetEndpoints()
    {
        assertEquals(1,p2.getEndpoints().size());

        List <Endpoint> endpoints = new ArrayList<Endpoint>();
        endpoints.add(new Endpoint(endpoint1));
        endpoints.add(new Endpoint(endpoint2));
        p2.setEndpoints(endpoints);
        assertEquals(2,p2.getEndpoints().size());
    }

    @Test
    public void testMisc()
    {
        assertEquals(projectName1,p1.getName());
        p1.setName(projectName2);

        assertEquals(projectName2,p1.getName());

        // test duplicate endpoints
        assertEquals(1,p2.getEndpoints().size());
        p2.addEndpoint(endpoint1);
        assertEquals(1,p2.getEndpoints().size());

        HashMap<Project,Integer> projects = new HashMap<Project,Integer>();
        projects.put(p1, 1);
        projects.put(p2, 1);

        assertEquals("Clash didn't occur in hashcode",1,projects.size());
        p1.setName(projectName1);
        projects.put(p2, 1);
        assertEquals("Hash code generated unique project nae",2,projects.size());

        assertEquals(true,p2.toString().contains(projectName2));

        assertEquals(false,p1.equals("hello"));
    }

    @Test
    public void testRemoveEndpoints()
    {
        assertEquals(1,p2.getEndpoints().size());
        p2.removeEndpoint("blablabla");
        assertEquals(1,p2.getEndpoints().size());
        p2.removeEndpoint(endpoint1);
        assertEquals(0,p2.getEndpoints().size());
    }
}