[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-client-java.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-client-java)
[![codecov](https://codecov.io/gh/ExpediaDotCom/haystack-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/ExpediaDotCom/haystack-client-java)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-client-java

Haystack bindings for the OpenTracing API. Clients use this library to send trace data to a Haystack server.

Opentracing API usage has been documented at [https://github.com/opentracing/opentracing-java](https://github.com/opentracing/opentracing-java). 

`haystack-client-core` library provides an implementation of `io.opentracing.Tracer` that sends spans to `Haystack` server. 

Integrations for common frameworks are available @ 

* [Dropwizard](integrations/dropwizard/README.md)
* [Spring Boot and Web](integrations/opentracing-spring-haystack-web-starter/README.md)
* [Opencencus](integrations/opencensus/README.md)


## How to build the code

### Prerequisites

* Make sure you have Java 1.8

### Building

For a full build, including unit tests you can run the following

```
./mvnw clean package
```

### Releases

Latest releases of this library are available in [Maven central](https://mvnrepository.com/search?q=Haystack&d=com.expedia)

1. Create a pull request with your changes.
2. Ask for a review of the pull request; when it is approved, the Travis CI build will upload the resulting SNAPSHOT jar file
to the [SonaType Staging Repository](https://oss.sonatype.org/#stagingRepositories)
3. Once merged to master, one can use github https://github.com/ExpediaDotCom/haystack-client-java/releases or manual tagging to the next semantic version
4. This will cause the build to run for the new tagged version. This will cause the jar files to be released to the 
[SonaType Release Repository](https://oss.sonatype.org/#nexus-search;quick~haystack-client-java).
5. Now, update the POM version to next snapshot version for development
6. Create another pull request with the change from step 5 and get it merged.
