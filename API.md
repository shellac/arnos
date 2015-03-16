API Methods

  * [SPARQL Quering](#Querying.md)
  * [List Endpoints](#List_Endpoints.md)
  * [Add Endpoint](#Add_Endpoint.md)
  * [Remove Endpoint](#Remove_Endpoint.md)
  * [Add/List/Remove projects](#Project_management.md)


---


# Querying #

## URL ##
```
GET  /projects/:project_id/query   (For SELECT,CONSTRUCT,ASK & DESCRIBE)
POST /projects/:project_id/query   (For UPDATE only)
```

Use the following to issue a [SPARQL query](http://www.w3.org/TR/rdf-sparql-query/) to all associated endpoints and return a single aggregated response.

Returned results are in the [SPARQL Query Results XML Format](http://www.w3.org/TR/rdf-sparql-XMLres/)
For more details about SPARQL support, see [SPARQLSupport](SPARQLSupport.md)


### Parameters ###
`query` = Required. The SPARQL query (SELECT,CONSTRUCT,ASK, DESCRIBE & UPDATE)

### Response ###
The result of the SPARQL query
```
<xml query result>
```

Note: If you want to query over specific endpoints only, use the Endpoint identifiers following form:
```
[POST] /projects/:project_id/:ep1_id+:ep1_id...+:epn_id/query
```


---


# List Endpoints #

## URL ##
```
GET /projects/:project_id/endpoints
```
Returns a list of the endpoints associated with this project

### Response ###
An XML list of endpoints

e.g.
```
<list>
  <endpoint>
    <location>http://endpoint/url</location>
    <id>endpoint identifier</id>
  </endpoint>
  <endpoint> ... </endpoint>
</list>
```


---


# Add Endpoint #

## URL ##
```
POST /projects/:project_id/endpoints/add
```
Associates a new endpoint for the given project (if the endpoint doesn't already exist)

### Parameters ###
`url` = Required. An endpoint url

### Response ###
The endpoint digest
e.g.
```
  <string>bafdaf6ba442a4164482...fa3</string>
```


---


# Remove Endpoint #

## URL ##
```
DELETE /projects/:project_id/endpoints/remove
```
Removes a given endpoint from the list. Clients who can not issue DELETE requests can POST with the added parameter `_method=DELETE`

### Parameters ###
`url` = Required. An endpoint url

### Response ###
A message indicating that the operation has been successful
```
<string>endpoint 'endpoint url' removed</string>
```


---


# Flush Endpoint caches #

## URL ##
```
GET /projects/:project_id/endpoints/flush
```
Removes all cache objects associated with a given endpoint.

### Parameters ###
`url` = Required. An endpoint url

### Response ###
A message indicating that the operation has been successful
```
<string>Cache flushed for 'endpoint url'</string>
```

---


# Project management #

For security reasons, adding, listing and removing projects is done via JMX.