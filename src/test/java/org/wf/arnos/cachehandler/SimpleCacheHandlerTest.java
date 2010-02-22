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

import java.io.File;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
    public static final String CACHE_SETTINGS = "./src/main/webapp/WEB-INF/ehcache.xml";

    public SimpleCacheHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp()
    {
        try
        {
            DOMConfigurator.configure("./src/main/webapp/WEB-INF/log4j.xml");
            cache = new SimpleCacheHandler(new File(CACHE_SETTINGS));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown()
    {
        try
        {
            cache.finalize();
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPut() throws Throwable
    {
        assertNull(cache.get(key));
        cache.put(key,value);
        assertEquals(value,cache.get(key));
        cache.put(key,value+"more text");
        assertEquals(value+"more text",cache.get(key));
    }

    @Test
    public void testContains() throws Throwable
    {
        assertFalse(cache.contains(key));
        cache.put(key,value);
        assertTrue(cache.contains(key));
        cache.put(key,value+"more text");
        assertTrue(cache.contains(key));
        cache.flush(key);
        assertFalse(cache.contains(key));
    }

    @Test
    public void testFlushAll()
    {
        assertNull(cache.get(key));
        cache.put(key,value);
        cache.put(key+"22",value);
        cache.flushAll();
        assertNull(cache.get(key));
        assertNull(cache.get(key+"22"));
    }

    @Test
    public void testFlush()
    {
        assertNull(cache.get(key));
        cache.put(key,value);
        assertEquals(value,cache.get(key));
        cache.flush(key);
        assertNull(cache.get(key));
    }

}