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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * A single SPARQL SELECT query result.
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class Result
{
    /**
     * Binding representation used for sorting.
     */
    private transient Binding binding = null;

    /**
     * Returns the binding for this Result.
     * @return Binding object
     */
    public final Binding getBinding()
    {
        return binding;
    }
    
    /**
     * Bindings array.
     */
    protected final transient List<String> bindings = new ArrayList<String>();

    /**
     * Value of each binding.
     */
    protected final transient List<String> values = new ArrayList<String>();

    /**
     * Returns the list of values for this result.
     * @return List of values
     */
    public final List<String> getValues()
    {
        return values;
    }

    // records the last integer value in the result (useful for count() queries)
    public int count = 0;

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
        binding = ((ResultBinding) sol).getBinding();

        Iterator vars = sol.varNames();
        while (vars.hasNext())
        {
            String var = vars.next().toString();

            bindings.add(escapeXMLEntities(var));

            RDFNode n = sol.get(var);

            if (n.isLiteral())
            {
                StringBuffer lit = new StringBuffer(DEFAULT_SB_LENGTH);
                lit.append("<literal");
                
                RDFDatatype dt = n.asNode().getLiteralDatatype();
                String lang = n.asNode().getLiteralLanguage();
                
                if (dt != null && StringUtils.isNotEmpty(dt.toString()))
                {
                    lit.append(" datatype=\""+dt.getURI()+"\"");
                    if (dt.getURI().toLowerCase().equals("http://www.w3.org/2001/xmlschema#integer"))
                    {
                        String s = ((Literal) n).getLexicalForm().toString();
                        try
                        {
                            count = Integer.parseInt(s);
                        }
                        catch (NumberFormatException nfe) {}
                    }
                }
                if (StringUtils.isNotEmpty(lang))
                {
                    lit.append(" xml:lang=\""+lang+"\"");
                }
                
                 lit.append(">" + escapeXMLEntities(((Literal) n).getLexicalForm()) + "</literal>");

                 values.add(lit.toString());
            }
            else
            {
                values.add("<uri>" + escapeXMLEntities(((Resource) n).getURI()) + "</uri>");
            }
        }
    }

    /**
     * Converts the internal representation to an xml string.
     * @return XML string
     */
    public final String toXML()
    {
        StringBuffer representation = new StringBuffer(DEFAULT_SB_LENGTH);

        representation.append("<result>");

        for (int i = 0; i < bindings.size(); i++)
        {
            String bind = bindings.get(i).toString();
            String value = values.get(i).toString();

            representation.append("<binding name=\"");
            representation.append(bind);
            representation.append("\">");
            representation.append(value);
            representation.append("</binding>");
        }

        representation.append("</result>");

        return representation.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return false;

        if (!(obj instanceof Result)) return false;

        Result other = (Result) obj;

        if (this.values.equals(other.getValues())) return true;
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 61 * hash + this.bindings.hashCode();
        hash = 61 * hash + this.values.hashCode();
        return hash;
    }


    /**
     * Escapes the xml output using xml entities
     * @param s RAW XML string
     * @return Escaped XML string
     */
    public static String escapeXMLEntities(String s)
    {
        s = s.replace("&", "&amp;");
        s = s.replace("\"", "&quot;");
        s = s.replace("'", "&apos;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }
}
