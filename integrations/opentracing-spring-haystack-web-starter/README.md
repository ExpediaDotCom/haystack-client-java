## Instrumenting Spring Boot or Spring Web applications

One can use this library to instrument spring web applications to send tracing information to Opentracing complicant [Haystack](https://expediadotcom.github.io/haystack/) distributed tracing platform. 

This library actually uses [io.opentracing.contrib:opentracing-spring-web-starter](https://github.com/opentracing-contrib/java-spring-web) to instrument a spring application. Implementation of `io.opentracing.Tracer` required by `opentracing-spring-web-starter` is provided by [com.expedia.www:opentracing-spring-haystack-starter](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/integrations/opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/TracerConfigurer.java).

This library is purely a convenience library that in turn depends on `io.opentracing.contrib:opentracing-spring-web-starter` and `com.expedia.www:opentracing-spring-haystack-starter` to reduce the dependency configuration required to instrument a spring stack using Opentracing and Haystack.

### Configuration

Check maven for latest versions of this library. At present, this library has been built and tested with Spring Boot 2.x

```xml
        <dependency>
            <groupId>com.expedia.www</groupId>
            <artifactId>opentracing-spring-haystack-web-starter</artifactId>
            <version>${opentracing-spring-haystack-web-starter.version}</version>
        </dependency>
```


