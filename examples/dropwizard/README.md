# Dropwizard example integration for Haystack

This is the example hello world application from the [Dropwizard][dropwizard] getting started documentation with Haystack tracing configured.

[dropwizard]: https://www.dropwizard.io

How to start the Dropwizard example integration application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/artifact-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080/hello-world`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`


# Haystack Data #

The shipped configuration (`config.yml`) configures the tracer to log
all spans it recieves in two different locations and formats:
`dispatcher` and `client` loggers.
