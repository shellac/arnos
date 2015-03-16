# Introduction #

Arnos is Java web application with the goal of providing a single unified query interface to a set of SPARQL endpoints. It acts as a middle man, relaying queries from the presentation layer to the data layer and back.
In this manor, it provides a simple [Federated query](http://en.wikipedia.org/wiki/Federated_search) mechanism for your application.

# Details #

A traditional semantic web app might be configured as follows:

![http://wiki.arnos.googlecode.com/hg/images/arnos_architecture_pre.png](http://wiki.arnos.googlecode.com/hg/images/arnos_architecture_pre.png)

In such a setup you might have a single endpoint containing all your triples, or you might store different datasets behind different triple stores as implied in this diagram. In a [linked-data](http://linkeddata.org/) view of the world, this is a high chance that you might not even be using your own datasets but instead relying on other sources.

With Arnos, you would register (via its [REST interface](http://code.google.com/p/arnos/wiki/API)) all the endpoints your app is using. Then your app sends all the SPARQL queries to Arnos directly. Arnos in turn issues the queries over all of the endpoints registered with your project and collates the results before returning them to your application.

![http://wiki.arnos.googlecode.com/hg/images/arnos_architecture_post.png](http://wiki.arnos.googlecode.com/hg/images/arnos_architecture_post.png)

# Why use Arnos? #
  * Abstraction. Arnos provides another layer of abstraction between your application and the data sources your application is using. This allows a level of flexibility to change datasources without needing any modification to your code.

  * Simplification. If your application is relying on data sets in different locations, Arnos will merge the results from all endpoints before returning the data to your app.

  * Consistent communication. Arnos forces all communication to be done via SPARQL over HTTP. Depending on your personal view, this might be a good thing or a headache.

  * Consistent presentation. You could use Arnos to present a set of endpoints used within your organisation via a single server.

  * Caching. Arnos provides a handy caching layer. If your application routinely sends the same SPARQL queries over and over again, Arnos will significantly reduce the communication overheads and processing needed on your backend endpoints.

  * Security. While not an important feature, using Arnos allows you to be more restrictive over access to endpoints.

# Issues #
Arnos is not a solution to all your sparql-querying needs.

  * Authentication. At the moment Arnos doesn't handle authentication or authorisation between your apps and the data. It is up to your application to keep track of these issues and make sure the right people see the right information. There [are plans](http://code.google.com/p/arnos/wiki/ToDo) to integrate something like OAuth into Arnos which can solve some of these issues. Alternatively using named graphs or distinct endpoints in your application can make the process of handling authorisation much easier.

  * Federated queries. Arnos does not provide a true solution for [federated sparql querying](http://www.w3.org/2007/05/SPARQLfed/). It uses a simplistic approach whereby incoming queries are reissued to each endpoint and results are merged together before returning to the client. Please be aware, **joins between endpoints are not supported** and neither are OFFSET or LIMIT always guaranteed to be preserved.

# Development #
Arnos is a Java [Maven](http://maven.apache.org/) project built using [Spring](http://www.springsource.org/), [Jena](http://jena.sourceforge.net/) and supports [SPARQL 1.0](http://code.google.com/p/arnos/wiki/SPARQLSupport) and ARQ's [count](http://jena.sourceforge.net/ARQ/group-by.html) extension.
We have extensive test coverage and use a continuous integration server ([Hudson](https://hudson.dev.java.net/)) for internal deployment.

Current code coverage (v0.2)
  * Classes: 100%
  * Conditionals: 92%
  * Files: 100%
  * Lines: 96%
  * Methods: 100%
  * Packages: 100%
  * Test methods: 94