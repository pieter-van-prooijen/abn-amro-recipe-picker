# Recipe Picker Service

## Building and Running

This service requires a Java 17 SDK and Maven for building.

Creating the uberjar and running the tests:
```shell
mvnw clean package
```

Run the jar with:
```shell
java -jar recipepicker-0.0.1-SNAPSHOT.jar
```
This exposes the following endpoints:

- (http://localhost:8080/v1/recipes)  The recipe API
- (http://localhost:8080/v1/ingredients) The ingredient API
- (http://localhost:8080/swagger-ui.html) The documentation for the APIs as a swagger-ui html page.
- (http://localhost:8080/v3/api-docs) The documentation for the APIs in OpenAPI json format.
- (http://localhost:8080/actuator/health) Endpoint for the status of the service.

The service loads a number of sample recipes upon startup, note that the database is not persisted between restarts.
Logging is on stdout.

The API endpoints have basic authentication on them, use *abn/secret* as a username/password to authenticate.

The "Try it out" function on the swagger-ui page can be used to invoke the endpoints.

## Overview
The service uses Spring Boot as its framework, with Spring technologies like Spring Rest MVC and Spring Data for web
and database access and Spring security for authentication/authorization.

## API
The API has two parts, one for managing the recipes and another one for the ingredients. Keeping the ingredients as
a separate entity enforces consistent naming and use, so no two versions of the same ingredient can exist in the
database (say with slightly different names, like "Minced Beef" or "Ground Beef").

Searching for recipes with included / excluded ingredients uses the ingredient ids only, the API assumes that the
front-end application will let the user pick ingredients from a list (fetched from /v1/ingredients), so it has the ids
available for use.

## Code
The code is structured following the "hexagonal architecture" principle and has the following packages:

- _nl.lambdatree.recipepicker.domain_ The core of the service containing the domain logic as domain models, services
  and repositories. It has no dependencies on the infrastructure package.
- _nl.lambdatree.recipepicker.infrastructure_ The infrastructure surrounding the domain, with the API definition and
  implementation and the spring wiring, these invoke services and repositories of the domain.

The tests contain mostly integration tests, unit tests are in the 'domain' package.

Transaction boundaries are set at the controller level, as an API call is the atomic unit here, it can either
succeed or fail as one unit.

The service uses Lombok to reduce the boilerplate for generating accessors, constructors and logging.

## Changes needed for Production

### Network
The service should be fronted by a reverse proxy and/or load balancer with https capability, as it only implements
unencrypted http endpoints.

### Database and Search
Replace the in-memory h2 database with a database server connection, with its credentials set outside the
application (see below). Hikari connection pooling is already provided by spring.

If the number of recipes becomes very large, replace the current instruction search implementation (which uses SQL wild
card 'like' search) with a full text search (like Postgresql's _tsvector_ data type or Sqlite's fts5 extension).
Note that this makes the query non-portable, as every database has its own syntax for full text queries, table
definitions etc.

### Security
The service secures the endpoint using basic authentication and a username / password in plaintext in the spring
configuration:
1. Fetch critical attributes from the process environment (set by something like Kubernetes Secrets) or use a secrets
   manager like Vault (which has Spring integration available).
2. Replace the basic authentication with a JWT/Oauth approach if more control is needed over access to the API.

### Improvements
A number of other possible improvements:

- Friendlier error responses.
- Increase unit and integration test coverage.
- Pagination of the search results.
