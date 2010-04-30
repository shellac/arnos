/*
 *  © University of Bristol
 */

package org.wf.arnos.logger;

import java.io.File;
import java.lang.System;
import org.apache.commons.logging.Log;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
@ContextConfiguration(locations={"file:./src/test/java/org/wf/arnos/logger/LoggerPostProcessorTest-context.xml"})
public class LoggerPostProcessorTest extends AbstractJUnit38SpringContextTests {

    @Autowired
    LoggerPostProcessor postPro;

    @Logger
    public static transient Log logger;

    @BeforeClass
    public static void setUpClass() throws Exception {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");

    }

    @Test
    public void testPostProcessAfterInitialization() {
        System.out.println("File "+(new File("tset").getAbsolutePath()));
        Object obj = System.console();
        Object result = postPro.postProcessAfterInitialization(obj,"Console");
        assertEquals(obj,result);
    }

    @Test
    public void testPostProcessBeforeInitialization() {
        System.out.println(LoggerPostProcessorTest.logger);

        Object obj = this;
        Object result = postPro.postProcessBeforeInitialization(obj,"Console");
        assertEquals(obj,result);
        assertNotNull(logger);
    }

}