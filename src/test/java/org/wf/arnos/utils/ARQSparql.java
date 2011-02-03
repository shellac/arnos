    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wf.arnos.utils;

import com.hp.hpl.jena.query.Syntax;
import java.util.HashMap;

/**
 *
 * @author cmcpb
 */
public class ARQSparql extends Sparql
{

    /*** ARQ Extensions ***/
    /*================*/

    public static final String ARQ_SELECT_COUNT = "SELECT  (count(*) as ?count)"+
        "WHERE"+
        "{ GRAPH ?v13"+
        "  { ?s  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "+ "<http://vocab.ouls.ox.ac.uk/projectfunding/projectfunding#Grant> .} "+
        "    { GRAPH ?v14 "+
        "      { ?s  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "+ "<http://vocab.ouls.ox.ac.uk/projectfunding/projectfunding#Grant> .}"+
        "    }"+
        "}";

    private static final String ARQ_SELECT_COUNT_RESULT_1 = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head><variable name=\"count\"/></head>\n"
        + "  <results>\n"
        + "      <result>\n"
        + "        <binding name=\"count\">\n"
        + "          <literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">457</literal>\n"
        + "        </binding>\n"
        + "      </result>\n"
        + "  </results>\n"
        + "</sparql>";
    private static final String ARQ_SELECT_COUNT_RESULT_2 = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head><variable name=\"count\"/></head>\n"
        + "  <results>\n"
        + "      <result>\n"
        + "        <binding name=\"count\">\n"
        + "          <literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">10</literal>\n"
        + "        </binding>\n"
        + "      </result>\n"
        + "  </results>\n"
        + "</sparql>";
    private static final String ARQ_SELECT_COUNT_RESULT_0 = "<?xml version=\"1.0\"?>\n"
        + "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n"
        + "  <head><variable name=\"count\"/></head>\n"
        + "  <results>\n"
        + "      <result>\n"
        + "        <binding name=\"count\">\n"
        + "          <literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">0</literal>\n"
        + "        </binding>\n"
        + "      </result>\n"
        + "  </results>\n"
        + "</sparql>";


        public ARQSparql()
        {
            endpoint1Mapping = new QueryMap(Syntax.syntaxARQ);
            endpoint2Mapping = new QueryMap(Syntax.syntaxARQ);
            endpoint3Mapping = new QueryMap(Syntax.syntaxARQ);

            endpoint1Mapping.put(ARQ_SELECT_COUNT, ARQ_SELECT_COUNT_RESULT_1);
            endpoint2Mapping.put(ARQ_SELECT_COUNT, ARQ_SELECT_COUNT_RESULT_2);
            endpoint3Mapping.put(ARQ_SELECT_COUNT, ARQ_SELECT_COUNT_RESULT_0);

            init();
        }
}
