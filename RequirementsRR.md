# Introduction #

A description of Research Revealed, and what it will require of arnos.

## An Overview of Research Revealed ##

In Research Revealed (henceforth **RR**) all of our data will be accessed using SPARQL over HTTP. Updates currently use SPARQL Update over HTTP, although we may revisit that as the working group approaches CR.

In this diagram the solid arrows represent http GETs. The double arrow head indicates an HTTP POST or PUT. Thus the funding council harvester feeds its store using update, and the completor reads data using queries.

Crew Browser and Caboto UI are html applications running in the browser. In the former case that simple means the browser requests dynamically create html from the crew application. The latter is more complex since it uses two services: caboto, for annotation form submission, and completor, which provides auto-completion for the form.

Crew, Completor, and Caboto are applications which use and (in one case) update the data stores, and present a web front end to user agents. The plan is to write more specialised applications, such as visualisers, which will also talk to the data layer.

Data is fed in from three sources: Caboto, providing user annotations; Harvester, providing data pulled from the web; and DataHub, the universities own systems. (The latter will be a direct query at some point, rather than a feed)

![http://wiki.arnos.googlecode.com/hg/images/rr-structure.svg?.png](http://wiki.arnos.googlecode.com/hg/images/rr-structure.svg?.png)

Arnos is the centre of RR. It provides a unified view of data for the client applications.

## Types of Query ##

RR applications produce most query forms (DESCRIBE, CONSTRUCT and SELECT). The only common feature is that they all use GRAPH. No queries use the default graph currently.

## Authentication and Authorisation ##

We use Bristol single sign on (CAS) to authenticate in the caboto-based REF tool. Once authenticated the user may add annotations to their private or public graphs in the caboto data store. They can read from all public graphs, plus their own private graph.

We would like this to carry on through Arnos, so that the authenticated user may see data from their private graph in other client applications such as Crew.

# Use Cases #

## Private Annotations ##

  * Bill logs in to REF tool using SSO.
  * Bill privately bookmarks an article mentioning a product which licenses a patent generated from her work.
  * Bill moves to RR browser.
  * Bill (vainly) navigates to own page.
  * Article is not mentioned in page.
  * Bill logs in via SSO.
  * Article is now visible.