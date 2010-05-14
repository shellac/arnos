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

import java.security.MessageDigest;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class Endpoint implements Comparable<Endpoint>
{
    /**
     * A SPARQL endpoint URI.
     */
    private transient String location = "";

    /**
     * A unique, rest-safe representation of this endpoint.
     */
    private transient String id = "";

    /**
     * The digest algorithm used to generate the id.
     */
    public static final String DIGEST_ALGORITHM = "SHA-1";

    /**
     * @return the URI
     */
    public final String getLocation()
    {
        return location;
    }

    /**
     * @param paramURI the URI to set
     */
    public final void setLocation(final String paramURI)
    {
        this.location = paramURI;
    }

    /**
     * Default constructor.
     * @param uri URI for this endpoint
     */
    public Endpoint(final String uri)
    {
        this.location = uri;
    }

    /**
     * Returns the rest-safe identifier for this endpoint.
     * @return a string digest representing this location
     */
    public final String getIdentifier()
    {
        if (!"".equals(id)) return id;

        try
        {
            MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);

            byte[] hash = md.digest(location.getBytes());

            id = toHexString(hash);
        }
        catch (Exception e) { }
        return id;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj) return true;

        if (!(obj instanceof Endpoint)) return false;

        Endpoint other = (Endpoint) obj;

        if (this.location.equals(other.getLocation())) return true;
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 0;
        if (location != null) hash += location.hashCode();
        return hash;
    }

    @Override
    public final String toString()
    {
        return "Endpoint:" + this.location;
    }

    /**
     * Implementing compareTo to allow sorting of endpoint lists.
     * @param that Endpoint to compare to
     * @return -1 if that is before this, 0 if they are equal, 1 if this should come after that
     */
    public final int compareTo(final Endpoint that)
    {
        if (that == null) return -1;
        return location.compareTo(that.getLocation());
    }

    public static String toHexString(byte[] v)
    {
        StringBuffer sb = new StringBuffer();
        byte n1, n2;

        for (int c = 0; c < v.length; c++)
        {
            n1 = (byte) ((v[c] & 0xF0) >>> 4); // This line was changed
            n2 = (byte) ((v[c] & 0x0F)); // So was this line

            sb.append(n1 >= 0xA ? (char) (n1 - 0xA + 'a') : (char) (n1 + '0'));
            sb.append(n2 >= 0xA ? (char) (n2 - 0xA + 'a') : (char) (n2 + '0'));
        }

        return sb.toString();
    }
}
