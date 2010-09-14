/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wf.arnos.queryhandler;

import org.wf.arnos.utils.handler.EndpointHandler;
import com.hp.hpl.jena.query.Syntax;
import org.wf.arnos.utils.ARQSparql;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.wf.arnos.cachehandler.CacheHandlerInterface;
import org.wf.arnos.cachehandler.SimpleCacheHandler;
import org.wf.arnos.cachehandler.SimpleCacheHandlerTest;
import org.wf.arnos.controller.QueryController;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.Project;
import org.wf.arnos.controller.model.ProjectsManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.easymock.EasyMockSupport;
import org.wf.arnos.utils.LocalServer;
import java.io.StringWriter;
import org.wf.arnos.utils.Sparql;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cmcpb
 */
public class ARQExtensionHandlerTest extends EasyMockSupport
{
    Project p;
    Endpoint ep1 = new Endpoint(Sparql.ENDPOINT1_URL); // 7 books
    Endpoint ep2 = new Endpoint(Sparql.ENDPOINT2_URL); // 0 books
    Endpoint ep3 = new Endpoint(Sparql.ENDPOINT3_URL); // 4 books
    private QueryController controller;
    static String PROJECT_NAME = "testproject";
    static String QueryString = ARQSparql.ARQ_SELECT_COUNT;


    @Before
    public void setUp()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
        Logger.getLogger("org.wf.arnos.controller.QueryController").setLevel(Level.ALL);

        ARQExtensionHandler queryHandler = new ARQExtensionHandler();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.initialize();
        queryHandler.setTaskExecutor(executor);

        ProjectsManager manager = new ProjectsManager();

        // setup default projects
        p = new Project(PROJECT_NAME);
        p.addEndpoint(Sparql.ENDPOINT1_URL);
        p.addEndpoint(Sparql.ENDPOINT2_URL);
        p.addEndpoint(Sparql.ENDPOINT3_URL);
        manager.addProject(p);

        controller = new QueryController();
        controller.manager = manager;
        controller.queryHandler = queryHandler;

        CacheHandlerInterface cache = null;
        try
        {
            cache = new SimpleCacheHandler(new File(SimpleCacheHandlerTest.CACHE_SETTINGS));
        }
        catch (Exception ex)
        {
            System.out.println("Throwing error");
            ex.printStackTrace();
            fail("Unable to create cache");
        }

        cache.flushAll(PROJECT_NAME);
        controller.cacheHandler = cache;
        QueryController.logger = LogFactory.getLog(QueryController.class);
    }

    @BeforeClass
    public static void setUpClass()
    {
        LocalServer.handler = new EndpointHandler(new ARQSparql());
        LocalServer.start();
    }

    @AfterClass
    public static void tearDownClass() {
        LocalServer.stop();
    }

    @Test
    public void testARQExtension()
    {
        StringWriter writer = new StringWriter();
        controller.executeQueryAcrossAllEndpoints(PROJECT_NAME, QueryString, writer);
        String result = writer.getBuffer().toString();

        System.out.println("result:"+result);
        assertTrue("Counts tally",result.contains("467"));

        // test output is valid xml
        assertTrue("Is valid xml", Sparql.validateXML(result));
    }


}