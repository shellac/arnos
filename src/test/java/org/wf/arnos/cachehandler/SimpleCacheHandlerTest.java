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
package org.wf.arnos.cachehandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import net.sf.ehcache.CacheException;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.exception.ArnosRuntimeException;
import static org.junit.Assert.*;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class SimpleCacheHandlerTest
{
    SimpleCacheHandler cache;
    String key = "abc";
    String value = "simple text content";
    String projectName1 = "testProject";
    String projectName2 = "anotherTestProject";
    public static final String CACHE_SETTINGS = "./src/main/webapp/WEB-INF/ehcache.xml";
    Endpoint ep1;
    Endpoint ep2;
    List<Endpoint> endpoints;

    public SimpleCacheHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass()
    {
        System.setProperty("net.sf.ehcache.enableShutdownHook","true");
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
    }

    @Before
    public void setUp()
    {
        try
        {
            ep1 = new Endpoint("http://www.somewhere.com/test1");
            ep2 = new Endpoint("http://www.somewhere.com/test2");
            endpoints = new ArrayList<Endpoint>();
            endpoints.add(ep1);
            endpoints.add(ep2);
            cache = new SimpleCacheHandler(new File(CACHE_SETTINGS));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown()
    {
        try
        {
            cache.close();
        }
        catch (Throwable e)
        {

        }
    }

    @Test
    public void testInitWrongCacheFile()
    {
        try
        {
            cache.close();
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }
        
        String filename = CACHE_SETTINGS+"wrong";
        try
        {
            cache = new SimpleCacheHandler(new File(filename));
            fail("Should have produced a CacheException accessing file "+filename);
        }
        catch (CacheException e)
        {
            // this is the expected result
        }

        filename = CACHE_SETTINGS;
        filename = filename.replace("ehcache", "log4j");
        Resource res = new FileSystemResource(new File(filename));

        try
        {
            cache = new SimpleCacheHandler(res);
            fail("Should have produced a CacheException accessing file "+filename);
        }
        catch (CacheException e)
        {
            // this is the expected result
        }
        catch (IOException ioe)
        {
            fail("CacheHandler should not throw IOException accessing "+filename);
        }
    }

    @Test
    public void testMissingCacheInCacheFile()
    {
        try
        {
            cache.close();
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }
        
        // create a copy of the file without the specific cache handler

        String contents = readFileAsString(CACHE_SETTINGS);
        File f = null;
        try
        {
            f = File.createTempFile("junit_cache", "test");
            f.deleteOnExit();

            // write to file
            Writer writer = new FileWriter(f);
            writer.append(contents);
            writer.close();

        }
        catch (IOException ioe)
        {
            fail("Unable to create/write to temp file");
        }

        // check we can init this cache

        try
        {
            cache = new SimpleCacheHandler(f);
            cache.close();
        }
        catch (ArnosRuntimeException e)
        {
            fail("Should now have produced a ArnosRuntimeException accessing file "+f.getAbsolutePath());
        }
        catch (CacheException ce)
        {
            fail("CacheHandler should not throw CacheException accessing "+f.getAbsolutePath());
        }

        // now remove the resultCache entry
        try
        {
            int tagStart = contents.indexOf("<cache name=\"resultsCache\"");
            // write to file
            Writer writer = new FileWriter(f);
            writer.append(contents.substring(0,tagStart));
            writer.append(contents.substring(contents.indexOf("/>", tagStart)+2));
            writer.close();
        }
        catch (IOException ioe)
        {
            fail("Unable to write to temp file");
        }


        // now check the cache is null

        try
        {
            cache = new SimpleCacheHandler(f);
            fail("Should now have produced a ArnosRuntimeException accessing file "+f.getAbsolutePath());
        }
        catch (ArnosRuntimeException e)
        {
            // expected
            cache.close();
        }
        catch (CacheException ce)
        {
            ce.printStackTrace();
            fail("CacheHandler should not throw CacheException accessing "+f.getAbsolutePath());
        }

    }
    
    /** @param filePath the name of the file to open. Not sure if it can accept URLs or just filenames. Path handling could be better, and buffer sizes are hardcoded
    */
    private static String readFileAsString(String filePath) {
        StringBuffer fileData = new StringBuffer(1000);
        try
        {
            FileReader fr = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(fr);
            char[] buf = new char[1024];
            int numRead=0;
            while((numRead=reader.read(buf)) != -1){
                fileData.append(buf, 0, numRead);
            }
            reader.close();
            fr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return fileData.toString();
    }


    @Test
    public void testPut() throws Throwable
    {
        assertNull(cache.get(projectName1,key));
        assertNull(cache.get(projectName2,key));
        cache.put(projectName1, endpoints, key,value);
        assertEquals(value,cache.get(projectName1,key));
        assertNull(cache.get(projectName2,key));
        cache.put(projectName1, endpoints, key,value+"more text");
        assertEquals(value+"more text",cache.get(projectName1,key));
        assertEquals(null,cache.get(projectName2,key));

        try
        {
            cache.put(projectName1, null, key,value);
            fail("Except npe to be raised");
        }
        catch (Exception e)
        {

        }

        cache.put(projectName1, new ArrayList<Endpoint>(), key,value);
    }

    @Test
    public void testContains() throws Throwable
    {
        assertFalse(cache.contains(projectName1,key));
        cache.put(projectName1, endpoints, key,value);
        assertTrue(cache.contains(projectName1,key));
        cache.put(projectName1 ,endpoints, key,value+"more text");
        assertTrue(cache.contains(projectName1,key));
        cache.flush(projectName1,key);
        assertFalse(cache.contains(projectName1,key));
    }

    @Test
    public void testFlush()
    {
        assertNull(cache.get(projectName1,key));
        assertNull(cache.get(projectName2,key));
        cache.put(projectName1, endpoints, key,value);
        cache.put(projectName2, endpoints, key,value);
        assertEquals(value,cache.get(projectName1,key));
        assertEquals(value,cache.get(projectName2,key));
        cache.flush(projectName1,key);
        assertNull(cache.get(projectName1,key));
        assertEquals(value,cache.get(projectName2,key));
    }

    @Test
    public void testFlushAll()
    {
        assertNull(cache.get(projectName1,key));
        assertNull(cache.get(projectName2,key));
        cache.put(projectName1, endpoints, key,value);
        cache.put(projectName1, endpoints, key+"22",value);
        cache.put(projectName2, endpoints, key,value);
        assertEquals(value,cache.get(projectName1,key));
        assertEquals(value,cache.get(projectName1,key+"22"));
        assertEquals(value,cache.get(projectName2,key));
        cache.flushAll(projectName1);
        assertNull(cache.get(projectName1,key));
        assertNull(cache.get(projectName1,key+"22"));
        assertEquals(value,cache.get(projectName2,key));
    }


    @Test
    public void testFlushEndpointSpecificData()
    {
        assertNull(cache.get(projectName1,key));
        assertNull(cache.get(projectName2,key));

        // test associating a cache with a specific endpoint, then clearing the cache for that endpoint
        endpoints = new ArrayList<Endpoint>();
        endpoints.add(ep1);
        cache.put(projectName1, endpoints, key,value);
        cache.put(projectName2, endpoints, key,value);

        endpoints.add(ep2);
        cache.put(projectName1, endpoints, key+"22",value);

        assertEquals(value,cache.get(projectName1,key));
        assertEquals(value,cache.get(projectName1,key+"22"));
        assertEquals(value,cache.get(projectName2,key));
        cache.flush(projectName1, ep1);

        assertNull(cache.get(projectName1,key));
        assertNull(cache.get(projectName1,key+"22"));

        // as project 2 had a cache with endpoint 1, this value should also be cleared when we flushed
        // all associated caches with endpoint 1
        assertNull(cache.get(projectName2,key));

        endpoints = new ArrayList<Endpoint>();
        endpoints.add(ep1);

        cache.put(projectName1, endpoints, key,value);

        endpoints = new ArrayList<Endpoint>();
        endpoints.add(ep2);

        cache.put(projectName1, endpoints, key,value);
        cache.put(projectName1, endpoints, key+"22",value);

        assertEquals(value,cache.get(projectName1,key));
        cache.flush(projectName1, ep1);

        // as  endpoint1 was associated with the key, the key will be removed
        // even though endpoint 2 also had a pointer to the same key
        assertNull(cache.get(projectName1,key));
        assertEquals(value,cache.get(projectName1,key+"22"));
    }

    @Test
    public void testRepeatedlySetupCache()
    {
        DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");

        try
        {
            cache.close();
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }

        int i = 0;
        for (i=0; i < 1000; i++)
        {
            try
            {
                cache = new SimpleCacheHandler(new File(CACHE_SETTINGS));

                cache.put(projectName1, endpoints, "test", "value");

                assertEquals("value",cache.get(projectName1,"test"));

                cache.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                fail(i + " "+ e.getMessage());
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                fail(i + " "+ e.getMessage());
            }
        }
    }
}