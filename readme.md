# ![RealWorld Example App](static/openliberty-realworld-logo.png)

> ### OpenLiberty + JPA codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

### [Demo](https://github.com/gothinkster/realworld)&nbsp;&nbsp;&nbsp;&nbsp;[RealWorld](https://github.com/gothinkster/realworld)

This codebase was created to demonstrate a fully fledged fullstack application built with OpenLiberty + MicroProfile +
JPA including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to
the [RealWorld](https://github.com/gothinkster/realworld) repo.

# Getting started

You will need Java as a prerequisite and our current build tool of choice is Maven.

* Clone this repo
* To build, run `mvn clean install`
* To start the server, run `mvn liberty:start`
* To start the server as a foreground process, run `mvn liberty:run`
