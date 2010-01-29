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

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class Endpoint
{
    /**
     * A SPARQL endpoint URI.
     */
    private String sURI = "";

    /**
     * @return the URI
     */
    public final String getURI()
    {
        return sURI;
    }

    /**
     * @param paramURI the URI to set
     */
    public final void setURI(final String paramURI)
    {
        this.sURI = paramURI;
    }

    /**
     * Default constructor.
     * @param uri URI for this endpoint
     */
    public Endpoint(final String uri)
    {
        this.sURI = uri;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj) return false;

        if (!(obj instanceof Endpoint)) return false;

        Endpoint other = (Endpoint) obj;

        if (this.sURI.equals(other.getURI())) return true;
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 0;
        if (sURI != null) hash += sURI.hashCode();
        return hash;
    }
}
