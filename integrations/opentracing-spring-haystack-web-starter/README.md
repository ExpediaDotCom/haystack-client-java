Table of Contents
=================

* [Table of Contents](#table-of-contents)
   * [Instrumenting Spring Boot or Spring Web applications](#instrumenting-spring-boot-or-spring-web-applications)
   * [Quick Start](#quick-start)
      * [Spring Boot or Spring Web dependency](#spring-boot-or-spring-web-dependency)
      * [Spring Application dependency](#spring-application-dependency)
      * [Other dependencies](#other-dependencies)
      * [Sample yaml/properties file](#sample-yamlproperties-file)
   * [Details](#details)
      * [Using this library](#using-this-library)
      * [Defaults](#defaults)
      * [Configuration](#configuration)
         * [Disabling Tracer](#disabling-tracer)
         * [Dispatcher(s)](#dispatchers)
            * [Logger Dispatcher](#logger-dispatcher)
            * [Grpc Agent Dispatcher](#grpc-agent-dispatcher)
            * [Http Dispatcher](#http-dispatcher)
            * [Dispatcher Bean](#dispatcher-bean)
         * [Metrics](#metrics)
         * [Customizing Tracer](#customizing-tracer)


## Instrumenting Spring Boot or Spring Web applications

One can use this library to instrument spring web applications to send tracing information to Opentracing complicant [Haystack](https://expediadotcom.github.io/haystack/) distributed tracing platform. 

This library actually uses [io.opentracing.contrib:opentracing-spring-web-starter](https://github.com/opentracing-contrib/java-spring-web) to instrument a spring application. Implementation of `io.opentracing.Tracer` required by `opentracing-spring-web-starter` is provided by [com.expedia.www:opentracing-spring-haystack-starter](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/integrations/opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/TracerConfigurer.java).

This library is purely a convenience library that in turn depends on `io.opentracing.contrib:opentracing-spring-web-starter` and `com.expedia.www:opentracing-spring-haystack-starter` to reduce the dependency configuration required to instrument a spring stack using Opentracing and Haystack.

## Quick Start

This section provides steps required to quickly configure your spring application to be wired using Opentracing's spring integration to Haystack. If you need additional information, please read the subsequent sections in this documentation

### Spring Boot or Spring Web dependency

Add the following dependency to your application

```xml
<dependency>
    <groupId>com.expedia.www</groupId>
    <artifactId>opentracing-spring-haystack-web-starter</artifactId>
    <version>${opentracing-spring-haystack-web-starter.version}</version>
</dependency>
```

Alternately, one can use the following 

### Spring Application dependency

To access an instance of `opentracing.io.Tracer` to instrument a Spring application, one can add the following dependencies

```xml
<dependency>
    <groupId>com.expedia.www</groupId>
    <artifactId>opentracing-spring-haystack-starter</artifactId>
    <version>${opentracing-spring-haystack-starter.version}</version>
</dependency>
```

Enable `@ComponentScan` on the package `com.expedia.haystack.opentracing.spring.starter` to configure the `Tracer` bean

### Other dependencies

Optionally, add the following to get metrics recorded in JMX

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-jmx</artifactId>
    <version>${io-micrometer.version}</version>
</dependency>
```

### Sample yaml/properties file

Add the following to the properties or yaml file of the application being instrumented  (this is just a sample. change the name of the application, host name/port of the agent etc)

```yaml
spring:
  application:
    name: springbootsample
    
opentracing:
  haystack:
    dispatchers:
      logger:
        name: span-logger
      agent:
        enabled: true
        host: haystack-agent
        port: 3400
```

## Details

### Using this library

Check maven for latest versions of this library. At present, this library has been built and tested with Spring Boot 2.x

```xml
<dependency>
    <groupId>com.expedia.www</groupId>
    <artifactId>opentracing-spring-haystack-web-starter</artifactId>
    <version>${opentracing-spring-haystack-web-starter.version}</version>
</dependency>
```

### Defaults

Adding this library autoconfigures an instance of [Haystack Tracer](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/core/src/main/java/com/expedia/www/haystack/client/Tracer.java) using defaults mentioned below. 

* `service-name`: Is read from configuration property `spring.application.name`. If it is not provided then the value will be set to `unnamed-application`
* `dispatcher`: Spans are dispatched to Haystack using one or more [Dispatcher](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/core/src/main/java/com/expedia/www/haystack/client/dispatchers/Dispatcher.java) instances. If none is configured or created, then a [LoggerDispatcher](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/core/src/main/java/com/expedia/www/haystack/client/dispatchers/LoggerDispatcher.java) is configured with "haystack" as the logger name
* `metrics`: This library depends on Micrometer's MeterRegistry to instrument the library itself. If no instance of `MeterRegistry` is present in the [spring application](https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector#what-do-i-get-out-of-the-box), then it uses a built-in No-op implementation. Which means no metrics are recorded

### Configuration

One can also configure the tracer created by the library using few configuration properties and optional beans.

#### Disabling Tracer

One can completely disable tracing with configuration property `opentracing.haystack.enabled`. If the property is missing (default), this property value is assumed as `true`.

```yaml
opentracing:
  haystack:
    enabled: false
```

#### Dispatcher(s)

One can configure `Dispatcher` in two ways: using configuration properties or by creating a spring bean.

Using configuration properties one can configure one or more of the following dispatchers. Configuring more than one dispatcher, creates a `ChainedDispatcher` and sends a span to all of the configured dispatcher instances.

##### Logger Dispatcher

One can configure the name of the logger to use in a LoggerDispatcher by setting the following property in Spring Boot yaml or properties file

```yaml
opentracing:
  haystack:
    dispatchers:
      logger:
        name: span-logger
```

##### Grpc Agent Dispatcher

Haystack provides a [GRPC agent](https://github.com/ExpediaDotCom/haystack-agent) as a convenience to send protobuf spans to Haystack's kafka. One can configure grpc agent by simply enabling it in the configuration. 

```yaml
opentracing:
  haystack:
    dispatchers:
      agent:
        enabled: true
```

There are other properties available to further configure the grpc agent dispatcher

```properties
opentracing.haystack.dispatchers.agent.host=haystack-agent
opentracing.haystack.dispatchers.agent.port=3400
opentracing.haystack.dispatchers.agent.keep-alive-time-m-s=30
opentracing.haystack.dispatchers.agent.keep-alive-timeout-m-s=30
opentracing.haystack.dispatchers.agent.keep-alive-without-calls=true
opentracing.haystack.dispatchers.agent.negotiation-type=PLAINTEXT
```

Alternately, one can create a bean of type [GrpcDispatcherFactory](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/integrations/opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/support/GrpcDispatcherFactory.java). 

```java
public interface GrpcDispatcherFactory {
    Dispatcher create(MetricsRegistry metricsRegistry, 
                      TracerSettings.AgentConfiguration agentConfiguration);
}
```

If available, this bean will be invoked with configuration properties defined to build a RemoteDispatcher with GrpcAgentClient.

```java
@Bean
public GrpcDispatcherFactory grpcDispatcherFactory() {
    return (metricsRegistry, config) ->
            new RemoteDispatcher.Builder(metricsRegistry, 
                                         config.builder(metricsRegistry).build()).build();
}
```

##### Http Dispatcher

Haystack also provides a [http collector](https://github.com/ExpediaDotCom/haystack-collector/tree/master/http) to ingest Json and Protobuf serialized spans over http.

One can configure a http dispatcher by adding the following endpoint configuration

```yaml
opentracing:
  haystack:
    dispatchers:
      http:
        endpoint: http://localhost:8080/span
        headers: 
          client-id: foo
          client-key: bar
```

`headers` property is optional. All properties defined under 'headers' will be sent as HTTP headers along with the serialized span data. 

As in Grpc Agent, one can create a bean of type [HttpDispatcherFactory](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/integrations/opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/support/GrpcDispatcherFactory.java). If available, that bean will be invoked to create a RemoteDispatcher with HttpClient

##### Dispatcher Bean

Instead of configuring dispatchers through properties, one can create a bean of type `Dispatcher` in the application's spring context. This library will use that bean instead of creating one using configuration or defaults. One can see this in the [integration test example](src/test/java/com/expedia/haystack/opentracing/spring/starter/DispatcherInjectionIntegrationTest.java#L57).

#### Metrics

As mentioned earlier, this library looks for a bean of type [Micrometer's MeterRegistry](https://micromerter.io). If present, it uses that to write all metrics from the library to the configured store. If not, the library will use a no-op implementation and no metrics will be written.

For example, adding the following two dependencies to the application will automatically create a `MeterRegistry` bean and write the metrics to JMX

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-jmx</artifactId>
    <version>${io-micrometer.version}</version>
</dependency>
```

#### Customizing Tracer

Haystack Tracer's [Builder](https://github.com/ExpediaDotCom/haystack-client-java/blob/master/core/src/main/java/com/expedia/www/haystack/client/Tracer.java#L356) class exposes a number of possible configurations that can be used to customize the `Tracer` instance built by this library and used by [Opentracing's Spring integration](https://github.com/opentracing-contrib/java-spring-web/). To customize the Tracer instance, one can create a bean of type [TracerCustomizer](../opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/support/TracerCustomizer.java). This will be invoked when the library attempts to build an instance of Tracer. One can see this in the [integration test](src/test/java/com/expedia/haystack/opentracing/spring/starter/TracerCustomizerIntegrationTest.java#L29).

