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
package org.wf.arnos.beans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import static org.junit.Assert.*;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ArnosPropertyPlaceholderConfigurerTest {
    static Resource existingFile1;
    static Resource existingFile2;
    static Resource missingFile = new FileSystemResource("missingfile.txt");

    @BeforeClass
    public static void setUp() throws IOException
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");

        // Create a temporary properties file
        Properties properties = new Properties();
        properties.setProperty("test.1.a", "aaa");
        properties.setProperty("test.1.b", "aaa");

        File f = File.createTempFile("prop1", "test");
        f.deleteOnExit();

        try { properties.store(new FileOutputStream(f), null); } catch (IOException e) { }
        
        existingFile1 = new FileSystemResource(f.getAbsolutePath());

        properties = new Properties();
        properties.setProperty("test.2.a", "ccc");
        properties.setProperty("test.2.b", "ddd");

        f = File.createTempFile("prop2", "test");
        f.deleteOnExit();

        try { properties.store(new FileOutputStream(f), null); } catch (IOException e) { }

        existingFile2 = new FileSystemResource(f.getAbsolutePath());
    }

    @Test
    public void testSetLocations()
    {
        ArnosPropertyPlaceholderConfigurer expectedBean = new ArnosPropertyPlaceholderConfigurer();

        PropertyPlaceholderConfigurer bean = new ArnosPropertyPlaceholderConfigurer();

        try
        {
            expectedBean.setLocations(new Resource[]{existingFile1});
        }
        catch (Exception e) {}

        try
        {
            bean.setLocations(new Resource[]{existingFile1,missingFile});
        }
        catch (Exception e) {}

        assertTrue(expectedBean.equals(bean));

        expectedBean = new ArnosPropertyPlaceholderConfigurer();

        bean = new ArnosPropertyPlaceholderConfigurer();

        try
        {
        expectedBean.setLocations(new Resource[]{existingFile1,existingFile2});
        }
        catch (Exception e) {}

        try
        {
        bean.setLocations(new Resource[]{existingFile1,missingFile, existingFile2});
        }
        catch (Exception e) {}

        assertTrue(expectedBean.equals(bean));

        // check different files lead to different beans
        expectedBean = new ArnosPropertyPlaceholderConfigurer();

        bean = new ArnosPropertyPlaceholderConfigurer();

        try
        {
        expectedBean.setLocations(new Resource[]{existingFile1});
        }
        catch (Exception e) {}

        try
        {
        bean.setLocations(new Resource[]{existingFile2});
        }
        catch (Exception e) {}

        assertFalse(expectedBean.equals(bean));
    }

    @Test
    public void testSetLocation()
    {
        ArnosPropertyPlaceholderConfigurer expectedBean = new ArnosPropertyPlaceholderConfigurer();

        PropertyPlaceholderConfigurer bean = new ArnosPropertyPlaceholderConfigurer();

        expectedBean.setLocation(existingFile1);

        bean.setLocation(existingFile1);

        assertTrue(expectedBean.equals(bean));

        expectedBean = new ArnosPropertyPlaceholderConfigurer();

        bean = new ArnosPropertyPlaceholderConfigurer();

        expectedBean.setLocation(existingFile1);

        bean.setLocations(new Resource[]{existingFile1,missingFile});

        assertTrue(expectedBean.equals(bean));
    }

    @Test
    public void testEquals()
    {
        PropertyPlaceholderConfigurer expectedBean = new ArnosPropertyPlaceholderConfigurer();
        expectedBean.setLocations(new Resource[]{existingFile1});

        PropertyPlaceholderConfigurer bean = new ArnosPropertyPlaceholderConfigurer();
        bean.setLocations(new Resource[]{existingFile2});

        assertFalse(expectedBean.equals(bean));

        expectedBean = new ArnosPropertyPlaceholderConfigurer();
        expectedBean.setLocations(new Resource[]{existingFile2});

        assertTrue(expectedBean.equals(bean));

        assertTrue(expectedBean.equals(expectedBean));

        assertFalse(expectedBean.equals(1));

        assertFalse(expectedBean.equals(new StringBuffer()));
    }
}