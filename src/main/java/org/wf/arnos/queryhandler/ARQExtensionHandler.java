/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wf.arnos.queryhandler;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        if (isCountQuery(query)) return handleCountQuery(projectName, query, endpoints);
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
        
        Map<Var, Agg> aggregators = new HashMap<Var, Agg>();
        for (Var var: query.getProject().getVars()) {
            Expr expr = query.getProject().getExpr(var);
            if (expr != null && expr instanceof E_Aggregator) {
                Aggregator agg = ((E_Aggregator) expr).getAggregator();
                aggregators.put(var, Agg.COUNT); // TODO -- use right thing
            }
        }
        
        Grouper grouper = new Grouper(query.getGroupBy(), aggregators);
        
        for (Result r: selectResultList) {
            grouper.group(r);
        }
        
        selectResultList = new ArrayList<Result>();
        Model solModel = ModelFactory.createDefaultModel();
        for (Binding b: grouper.results.values()) {
            Result r = new Result(new ResultBinding(solModel, b));
            selectResultList.add(r);
        }
        
        
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

        content.append("</results></sparql>");

        selectResultList.clear();

        LOG.debug("Returning string: "+content.toString());
        
        return content.toString();
    }

    /**
     * Checks if given select query uses the count ARQ extension syntax
     * @param s
     * @return <code>true</codisCountQuerye> if query contains a count result, <code>false</code> otherwise
     */
    private boolean isCountQuery(Query q)
    {
        return q.hasAggregators();
    }
    
    static class Grouper {
        
        private final Map<String, Binding> results = new HashMap<String, Binding>();
        private final List<Var> vars;
        private final Map<Var, Agg> aggs;
              
        public Grouper(VarExprList groupExpr, Map<Var, Agg> aggs) {
            this.vars = groupExpr.getVars();
            this.aggs = aggs;
        }
        
        public void group(Result result) {
            Binding binding = result.getBinding();
            String key = makeKey(binding);
            if (!results.containsKey(key)) results.put(key, binding);
            else {
                results.put(key, combine(results.get(key), binding));
            }
        }
        
        private Binding combine(Binding left, Binding right) {
            Binding toReturn = new BindingMap();
            for (Var var: aggs.keySet()) {
                // All aggs are sums at the moment
                Node valLeft = left.get(var);
                Node valRight = right.get(var);
                Integer val = ((Integer) valLeft.getLiteralValue()) + ((Integer) valRight.getLiteralValue());
                toReturn.add(var, Node.createLiteral(val.toString(), null, XSDDatatype.XSDinteger));
            }
            // copy vals from left if not already set
            Iterator<Var> it = left.vars();
            while (it.hasNext()) {
                Var var = it.next();
                if (!toReturn.contains(var)) toReturn.add(var, left.get(var));
            }
            return toReturn;
        }
        
        private String makeKey(Binding binding) {
            if (vars.isEmpty()) return "nogroup";
            StringBuilder key = new StringBuilder();
            for (Var var: vars) {
                key.append(binding.get(var));
                key.append('|');
            }
            return key.toString();
        }
    }
    
    enum Agg { COUNT, SUM }
}
