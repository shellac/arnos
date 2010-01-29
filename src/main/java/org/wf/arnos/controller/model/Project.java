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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a collection of endpoints.
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class Project
{

    /**
     * The list of endpoints.
     */
    private List<Endpoint> endpoints;

    /**
     * Thie project name.
     */
    private String name;

    /**
     * @return the endpoints
     */
    public final List<Endpoint> getEndpoints()
    {
        return endpoints;
    }

    /**
     * @param paramEndpoints the endpoints to set
     */
    public final void setEndpoints(final List<Endpoint> paramEndpoints)
    {
        this.endpoints = paramEndpoints;
    }

    /**
     * @return the name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * @param paramName the name to set
     */
    public final void setName(final String paramName)
    {
        this.name = paramName;
    }

    /**
     * Default constructor which takes a project name.
     * @param paramName Name of project (should be unique)
     */
    public Project(final String paramName)
    {
        this.name = paramName;
        endpoints = new ArrayList<Endpoint>();
    }

    /**
     * Adds a endpoint to this project.
     * @param uri Endpoint URI
     */
    public final void addEndpoint(final String uri)
    {
        Endpoint e = new Endpoint(uri);
        if (!endpoints.contains(e))
        {
            endpoints.add(e);
        }
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj) return false;

        if (!(obj instanceof Project)) return false;

        Project other = (Project) obj;

        if (this.name.equals(other.getName())) return true;
        return false;
    }

    @Override
    public final int hashCode()
    {
        int hash = 0;
        if (name != null) hash += name.hashCode();
        return hash;
    }
}
