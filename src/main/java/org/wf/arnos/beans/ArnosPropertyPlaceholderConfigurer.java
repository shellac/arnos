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

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;

/**
 * An extension to PropertyPlaceholderConfigurer which simply handles missing
 * resources without throwing BeanInitializationException on startup
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ArnosPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
{
    @Override
    public void setLocations(Resource[] locations)
    {
        for (Resource location : locations)
        {
            setLocation(location);
        }
    }

    @Override
    public void setLocation(Resource location)
    {
        if (!location.exists()) return;
        super.setLocation(location);
    }

    public Properties mergeProperties()
    {
        try
        {
        return super.mergeProperties();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return new Properties();
        }
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj) return true;

        if (!(obj instanceof PropertyPlaceholderConfigurer)) return false;

        ArnosPropertyPlaceholderConfigurer other = (ArnosPropertyPlaceholderConfigurer) obj;

        return this.mergeProperties().equals(other.mergeProperties());
    }
}
