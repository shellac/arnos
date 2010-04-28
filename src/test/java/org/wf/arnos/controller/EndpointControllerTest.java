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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.Assert.*;
import org.springframework.ui.Model;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.Project;
import org.wf.arnos.controller.model.ProjectsManager;
import org.wf.arnos.utils.Sparql;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class EndpointControllerTest {
    EndpointController controller;
    static String PROJECT_NAME = "testproject";
    Project p;
    
    @Before
    public void setUp() throws IOException
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");

        File f = File.createTempFile("jnuit_endpoint", "test");
        f.deleteOnExit();

        ProjectsManager manager = new ProjectsManager();
        manager.setFileName(f.getAbsolutePath());

        // setup default projects
        p = new Project(PROJECT_NAME);
        p.addEndpoint(Sparql.ENDPOINT1_URL);
        p.addEndpoint(Sparql.ENDPOINT2_URL);
        manager.addProject(p);

        controller = new EndpointController();
        controller.manager = manager;
        EndpointController.logger = LogFactory.getLog(QueryController.class);
    }

    @Test
    public void testListEndpoints()
    {
        assertEquals(2,getNumOfEndpoints());
    }

    @Test
    public void testAddEndpoint()
    {
        Model model = new ExtendedModelMap();
        assertEquals(2,getNumOfEndpoints());

        String result = controller.addEndpoint(PROJECT_NAME, Sparql.ENDPOINT3_URL, model);

        Endpoint p = new Endpoint(Sparql.ENDPOINT3_URL);
        String expectedResult = p.getIdentifier();

        // check result
        assertEquals(3,getNumOfEndpoints());
        assertEquals("",result);
    }

    @Test
    public void testRemoveEndpoint()
    {
        Model model = new ExtendedModelMap();
        
        assertEquals(2,getNumOfEndpoints());
        controller.removeEndpoint(PROJECT_NAME, Sparql.ENDPOINT1_URL, model);

        assertEquals(1,getNumOfEndpoints());
    }

    private int getNumOfEndpoints()
    {
        Model model = new ExtendedModelMap();
        assertEquals("",controller.listEndpoints(PROJECT_NAME, (Model)model));
        List<Endpoint> endpointList = (List<Endpoint>) model.asMap().get("endpoints");

        return endpointList.size();
    }
}