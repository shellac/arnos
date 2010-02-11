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
package org.wf.arnos.controller.model.sparql;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.Iterator;

/**
 * A single SPARQL SELECT query result.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class Result
{
    /**
     * Internal representation of this object.
     */
    private final transient String s;

    /**
     * Default length for results stringbuffer constructor.
     */
    private static final int DEFAULT_SB_LENGTH = 52;

    /**
     * Constructor which uses the provided query solution to form an internal model of this result.
     * @param sol Result to process
     */
    public Result(final QuerySolution sol)
    {
        StringBuffer representation = new StringBuffer(DEFAULT_SB_LENGTH);

        representation.append("<result>");

        Iterator vars = sol.varNames();
        while (vars.hasNext())
        {
            String var = vars.next().toString();
            RDFNode n = sol.get(var);

            representation.append("<binding name=\"");
            representation.append(var);
            representation.append("\">");

            if (n.isLiteral())
            {
                representation.append("<literal>");
                representation.append(((Literal) n).getLexicalForm());
                representation.append("</literal>");
            }
            else
            {
                representation.append("<uri>");
                representation.append(((Resource) n).getURI());
                representation.append("</uri>");
            }

             representation.append("</binding>");
        }

        representation.append("</result>");

        s = representation.toString();
    }

    /**
     * Converts internal representation to an xml string.
     * @return XML string
     */
    public final String toXML()
    {
        return s;
    }
}
