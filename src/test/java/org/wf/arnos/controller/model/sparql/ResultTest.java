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
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.wf.arnos.utils.Sparql;
import static org.junit.Assert.*;

/**
 *
 * @author Chris Bailey (c.bailey@bristol.ac.uk)
 */
public class ResultTest {


    @Test
    public void testHashCode()
    {
        List<org.wf.arnos.controller.model.sparql.Result> results = new ArrayList<org.wf.arnos.controller.model.sparql.Result>();

        HashMap <Result,Integer> hash = new HashMap<Result,Integer>();

        ResultSet resultSet = ResultSetFactory.fromXML(Sparql.SELECT_RESULT_7_BOOKS);
        int i = 0 ;
        while (resultSet.hasNext())
        {
            QuerySolution sol = resultSet.next();
            Result r = new Result(sol);
            results.add(r);
            hash.put(r, i++);
        }

        assertEquals("Hashmap is the same size as Set",results.size(),hash.size());

        assertFalse(results.get(0).equals(null));

        for (i =1; i<results.size(); i++)
        {
            Result previous = results.get(i-1);
            Result r = results.get(i);

            boolean compare = r.equals(previous);
            assertTrue(compare == previous.equals(r));

            int hash1 = r.hashCode();
            r.bindings.clear();
            int hash2 = r.hashCode();
            r.values.clear();
            int hash3 = r.hashCode();

            assertEquals(true,hash1 != hash2 && hash1 != hash3 && hash2 != hash3);
        }
    }

}