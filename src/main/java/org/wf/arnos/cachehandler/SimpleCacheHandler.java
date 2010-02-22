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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.core.io.Resource;
import org.wf.arnos.exception.ArnosRuntimeException;

/**
 * A nieve implementation of cache support using ehcache.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class SimpleCacheHandler implements CacheHandlerInterface
{
    /**
     * Name of ehcache.
     */
    private static final String CACHE_NAME = "resultsCache";

    /**
     * MD5 digest.
     */
    private transient MessageDigest digest;

    /**
     * The ehcache.
     */
    private transient Cache cache;

    /**
     * Creates a singleton instance of the cachemanager for spring-configured frameworks.
     */
    public SimpleCacheHandler(final Resource res) throws FileNotFoundException, IOException
    {
        init(res.getFile());
    }

    /**
     * Creates a singleton instance of the cachemanager using supplied file descriptor as config.
     * This constructor can be used in a unit testing environment.
     * @param configFile Path to ehcache.xml
     */
    public SimpleCacheHandler(final File  file) throws FileNotFoundException, IOException
    {
        init(file);
    }


    /**
     * Constructor-independent initlization script.
     * @param file Cache file
     * @throws IOException Thrown if unable to read cache file
     */
    private void init(final File file) throws IOException
    {
        CacheManager.create(file.getAbsolutePath());

        cache = CacheManager.getInstance().getCache(CACHE_NAME);

        if (cache == null) throw new ArnosRuntimeException("Cache '" + CACHE_NAME + "'missing");

        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException nsae)
        {
            try
            {
                digest = MessageDigest.getInstance("SHA-1");
            }
            catch (NoSuchAlgorithmException anothernsae)
            {
                throw new ArnosRuntimeException("Unable to setup digest algorithm", anothernsae);
            }
        }
    }

    /**
     * Adds an entry to the cache.
     * @param key Identifier for cache
     * @param value Response to cache
     */
    public final void put(final String key, final String value)
    {
        Element element = new Element(generateKey(key), value);
        cache.put(element);
    }

    /**
     * Gets a response from the cache.
     * @param key Lookup key
     * @return Cached value, or null if not in cache
     */
    public final String get(final String key)
    {
        Element element = cache.get(generateKey(key));

        if (element == null) return null;

        Serializable value = element.getValue();
        return value.toString();
    }

    /**
     * Checks for the existance of a cache object.
     * @param key Lookup key
     * @return <code>true</code> if cache exists, <code>false</code> otherwise
     */
    public final boolean contains(final String key)
    {
        Element element = cache.get(generateKey(key));

        if (element == null) return false;

        return true;
    }

    /**
     * Remove a specific key from the cache.
     * @param key Cache key
     */
    public final void flush(final String key)
    {
        cache.remove(generateKey(key));
    }

    /**
     * Remove all keys from the cache.
     */
    public final void flushAll()
    {
        cache.removeAll();
    }

    /**
     * Cleanly shuts down the cache mangager.
     * @throws Throwable
     */
    @Override
    protected final void finalize() throws Throwable
    {
        CacheManager.getInstance().shutdown();
        super.finalize();
    }

    /**
     * Create a digest of the original string to use as a hash key.
     * @param s Full string representation of the key
     * @return Digest version of the key
     */
    private String generateKey(final String s)
    {
        digest.reset();
        digest.update(s.getBytes());
        return convertToHex(digest.digest());
    }

    /**
     * Method to convert byte array to a HEX-encoded string.
     * See: http://www.anyexample.com/programming/java/java_simple_class_to_compute_md5_hash.xml
     * @param data Data to digest
     * @return a hexadecimal string representation for the hash
     */
    private static String convertToHex(final byte[] data)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++)
        {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int twoHalfs = 0;
            do
            {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return buf.toString();
    }
}
