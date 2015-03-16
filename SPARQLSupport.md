# SPARQL Support #

The arnos provides a SPARQL 1.0 http://www.w3.org/TR/rdf-sparql-query/ endpoint for each project.

All query forms (SELECT/CONSTRUCT/ASK/DESCRIBE) are supported.
UPDATE is supported for single endpoints.

## SELECT ##

  * ORDER BY
> > Supported.
  * LIMIT
> > Supported.
  * OFFSET
> > OFFSET will work as expected only if a _single_ endpoint is returning results. If multiple endpoints provide results, OFFSET is unlikely to return the expected result.
  * REDUCED
> > Supported.
  * DISTINCT
> > Supported.

## ASK ##
Will return true if any single endpoint matches.

## DESCRIBE ##
Results of endpoint matches are merged before results are returned.
  * OFFSET & LIMIT
> > OFFSET & LIMIT will work as expected only if a _single_ endpoint is returning results. If multiple endpoints provide results, these results are combined so LIMIT (and OFFSET) are unlikely to be upheld.

## CONSTRUCT ##
Results of endpoint matches are merged before results are returned.

  * OFFSET
> > Supported.
  * LIMIT
> > Supported.

# EXTENSIONS #
Using the `ARQExtensionHandler` provides support for ARQ's  [count aggregation queries](http://jena.sourceforge.net/ARQ/group-by.html).