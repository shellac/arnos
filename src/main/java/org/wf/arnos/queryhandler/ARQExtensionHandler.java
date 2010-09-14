/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wf.arnos.queryhandler;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wf.arnos.controller.model.Endpoint;
import org.wf.arnos.controller.model.sparql.Result;

/**
 *
 * @author cmcpb
 */
public class ARQExtensionHandler extends ThreadedQueryHandler
{
    private static final Log LOG = LogFactory.getLog(ARQExtensionHandler.class);

    /**
     * Parse the string with the ARQ extensions, returning a Query object
     * @param query
     * @return
     */
    @Override
    public Query parseQuery(String query)
    {
        return QueryFactory.create(query, Syntax.syntaxARQ);
    }

    /**
     * This method overrides the default handleSelect to parse count queries to the handleCountQuery function
     * @param projectName Name of project
     * @param query SPARQL SELECT query
     * @param endpoints List of endpoints to query over
     * @return Response string
     */
    @Override
    public final String handleSelect(final String projectName, final Query query, final List<Endpoint> endpoints)
    {
        if (isCountQuery(query.toString())) return handleCountQuery(projectName, query, endpoints);
        else return super.handleSelect(projectName, query, endpoints);
    }

    /**
     * This method handles the ARQ count extensions. It totals up the results across all endpoints.
     * @param projectName Name of project
     * @param query
     * @param endpoints
     * @return
     */
    public String handleCountQuery(final String projectName, final Query query, final List<Endpoint> endpoints) {
        LOG.info("handling SELECT (count)");

        List<Result> selectResultList = fetchResultSetAndWait(projectName, query, endpoints);

        StringBuffer content = new StringBuffer(DEFAULT_SB_LENGTH);

        content.append("<?xml version=\"1.0\"?><sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"><head>");

        // add head info
        List<String> vars = query.getResultVars();
        for (String var : vars)
        {
            content.append("<variable name=\"");
            content.append(Result.escapeXMLEntities(var));
            content.append("\"/>");
        }
        content.append("</head><results>");

        // collate all responses
        boolean hasLimit = false;
        boolean distinct = false;
        long limit = -1;

        if (query.hasLimit())
        {
            limit = query.getLimit();
            hasLimit = true;
        }

        if (query.isDistinct())
        {
            distinct = true;
        }

        if (query.hasOrderBy())
        {
            sortResults(selectResultList, query.getOrderBy());
        }

        if (query.hasAggregators())
        {
            int count = 0;
            for (int i = 0; i < selectResultList.size(); i++)
            {
                Result r = selectResultList.get(i);
                count += r.count;
            }
            content.append("<result><binding name=\""+ query.getResultVars().get(0).toString()+"\">");
            content.append("<literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">"+count+"</literal>");
            content.append("</binding></result>");
        }
        else
        {
            for (int i = 0; i < selectResultList.size(); i++)
            {
                Result r = selectResultList.get(i);
                boolean add = true;

                if (!hasLimit || limit > 0)
                {
                    if (hasLimit) limit--;
                }
                else
                {
                    add = false;
                }

                if (distinct)
                {
                    // check a duplicate result hasn't already been added
                    boolean match = false;
                    for (int j = 0; j < i; j++)
                    {
                        if (r.equals(selectResultList.get(j))) match = true;
                    }
                    if (match) add = false;
                }

                if (add) content.append(r.toXML());
            }
        }

        content.append("</results></sparql>");

        selectResultList.clear();

        return content.toString();
    }

    /**
     * Checks if given select query uses the count ARQ extension syntax
     * @param s
     * @return <code>true</codisCountQuerye> if query contains a count result, <code>false</code> otherwise
     */
    private boolean isCountQuery(String s)
    {
        if (s.toUpperCase().contains("COUNT")) return true;

        return false;
    }
}
