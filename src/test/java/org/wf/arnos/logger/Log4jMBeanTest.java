/*
 *  © University of Bristol
 */

package org.wf.arnos.logger;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wf.arnos.controller.EndpointController;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class Log4jMBeanTest {
    Log logger;
    String loggerClassString = "org.wf.arnos.controller.EndpointController";
    Class loggerClass = EndpointController.class;
    Log4jMBean instance;

    public Log4jMBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        logger = LogFactory.getLog(loggerClass);

        LogManager.getLogger(loggerClassString).setLevel(Level.ALL);

        instance = new Log4jMBean();
    }


    /**
     * Test of activateInfo method, of class Log4jMBean.
     */
    @Test
    public void testActivateInfo() {
        System.out.println("activateInfo");

        instance.activateInfo(loggerClassString);
        assertEquals(Level.INFO, LogManager.getLogger(loggerClassString).getLevel());
    }

    /**
     * Test of activateDebug method, of class Log4jMBean.
     */
    @Test
    public void testActivateDebug() {
        System.out.println("activateDebug");

        instance.activateDebug(loggerClassString);
        assertEquals(Level.DEBUG, LogManager.getLogger(loggerClassString).getLevel());
    }

    /**
     * Test of activateWarn method, of class Log4jMBean.
     */
    @Test
    public void testActivateWarn() {
        System.out.println("activateWarn");

        instance.activateWarn(loggerClassString);
        assertEquals(Level.WARN, LogManager.getLogger(loggerClassString).getLevel());
    }

    /**
     * Test of activateError method, of class Log4jMBean.
     */
    @Test
    public void testActivateError() {
        System.out.println("activateError");

        instance.activateError(loggerClassString);
        assertEquals(Level.ERROR, LogManager.getLogger(loggerClassString).getLevel());
    }

    /**
     * Test of activateFatal method, of class Log4jMBean.
     */
    @Test
    public void testActivateFatal() {
        System.out.println("activateFatal");
        
        instance.activateFatal(loggerClassString);
        assertEquals(Level.FATAL, LogManager.getLogger(loggerClassString).getLevel());
    }

    /**
     * Test of listAllLoggers method, of class Log4jMBean.
     */
    @Test
    public void testListAllLoggers() {
        System.out.println("listAllLoggers");

        ArrayList<String> result = instance.listAllLoggers();
        assertEquals(true, result.size() >= 1);
        
        boolean found = false;
        for (String s: result)
        {
            if (s.contains(loggerClassString))
            {
                assertEquals(true,s.contains("ALL"));
                found = true;
            }
        }
        assertEquals("Logger not found",true,found);
    }

}