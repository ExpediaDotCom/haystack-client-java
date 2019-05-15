[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-client-java.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-client-java)
[![codecov](https://codecov.io/gh/ExpediaDotCom/haystack-client-java/branch/master/graph/badge.svg)](https://codecov.io/gh/ExpediaDotCom/haystack-client-java)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# Haystack Client Java 

Haystack client java is an [OpenTracing](https://github.com/opentracing/opentracing-java) compatible library which is used to capture information about distributed operations and report them to [haystack](https://expediadotcom.github.io/haystack)


Opentracing API usage has been documented at [https://github.com/opentracing/opentracing-java](https://github.com/opentracing/opentracing-java). 


You can look at our sample projects for how to trace a simple web application

* [SpringBoot Example](https://github.com/ExpediaDotCom/opentracing-spring-haystack-example)
* [Dropwizard Example](https://github.com/ExpediaDotCom/haystack-dropwizard-example) 

## Contributing and Developing
Please see [CONTRIBUTING.md](CONTRIBUTING.md)


## Core Modules

[haystack-client-core](./core) -  library provides an implementation of `io.opentracing.Tracer` that sends spans to `Haystack` server. 

## Addon Modules

[haystack-client-metrics-micrometer](./metrics/micrometer): metrics provider backed by [micrometer](https://micrometer.io/), to report internal Haystack Client metrics to third-party backends, such as Prometheus

[haystack-client-metrics-dropwizard](./metrics/dropwizard-metrics): metrics provider backed by [dropwizard-metrics](https://metrics.dropwizard.io/4.0.0/), to report internal Haystack Client metrics to third-party backends, such as Graphite

[haystack-client-metrics-api](./metrics/api): metrics api consumed by haystack-client-core to report internal Haystack Client metrics. By default its bundled with a NoOp metrics dispatcher, add any of the obove dependencies to push the client metrics.


## Importing Dependencies

All artifacts are published to Maven Central. Snapshot artifacts are also published to [Sonatype][sonatype].
Follow these [instructions][sonatype-snapshot-instructions] to add the snapshot repository to your build system.

**Please use the latest version:** [![Released Version][maven-img]][maven]

In the usual case, you just need to include the following dependency to your project:
```xml
<dependency>
  <groupId>com.expedia.www</groupId>
  <artifactId>haystack-client-java-core</artifactId>
  <version>$latestClientVersion</version>
  <type>pom</type>
</dependency>
``` 

## Integrations 

In case your apps are already wired in with some kind of instrumentation library other than OpenTracing you could refer to the below links based on your usecase

* [Opencencus](https://github.com/ExpediaDotCom/haystack-opencensus-exporter-java)
* [Brave-Zipkin](https://github.com/HotelsDotCom/pitchfork)


## License
This project is licensed under the Apache License v2.0 - see the LICENSE.txt file for details.

## Benchmarks
We have published our benchmarks [here](benchmark/README.md)
