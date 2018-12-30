Table of Contents
=================

* [Instrumenting Spring Boot or Spring Web applications](#instrumenting-spring-boot-or-spring-web-applications)
* [Usage](#usage)
* [Defaults](#defaults)
* [Configuration](#configuration)
   * [Disabling Tracer](#disabling-tracer)
   * [Configuring Dispatcher(s)](#configuring-dispatchers)
      * [Logger Dispatcher](#logger-dispatcher)
      * [Grpc Agent Dispatcher](#grpc-agent-dispatcher)
   * [Configuring Metrics](#configuring-metrics)
  * [Configuring Tracer](#configuring-tracer)


## Instrumenting Spring Boot or Spring Web applications

One can use this library to instrument spring web applications to send tracing information to Opentracing complicant [Haystack](https://expediadotcom.github.io/haystack/) distributed tracing platform. 

This library actually uses [io.opentracing.contrib:opentracing-spring-web-starter](https://github.com/opentracing-contrib/java-spring-web) to instrument a spring application. Implementation of `io.opentracing.Tracer` required by `opentracing-spring-web-starter` is provided by [com.expedia.www:opentracing-spring-haystack-starter](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/integrations/opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/TracerConfigurer.java).

This library is purely a convenience library that in turn depends on `io.opentracing.contrib:opentracing-spring-web-starter` and `com.expedia.www:opentracing-spring-haystack-starter` to reduce the dependency configuration required to instrument a spring stack using Opentracing and Haystack.

## Usage

Check maven for latest versions of this library. At present, this library has been built and tested with Spring Boot 2.x

```xml
        <dependency>
            <groupId>com.expedia.www</groupId>
            <artifactId>opentracing-spring-haystack-web-starter</artifactId>
            <version>${opentracing-spring-haystack-web-starter.version}</version>
        </dependency>
```

## Defaults

Adding this library autoconfigures an instance of [Haystack Tracer](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/core/src/main/java/com/expedia/www/haystack/client/Tracer.java) using defaults mentioned below. 

* `service-name`: Is read from configuration property `spring.application.name`. If it is not provided then the value will be set to `unnamed-application`
* `dispatcher`: Spans are dispatched to Haystack using one or more [Dispatcher](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/core/src/main/java/com/expedia/www/haystack/client/dispatchers/Dispatcher.java) instances. If none is configured, then a [LoggerDispatcher](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/core/src/main/java/com/expedia/www/haystack/client/dispatchers/LoggerDispatcher.java) is configured with "haystack" as the logger name
* `metrics`: This library depends on Micrometer's MeterRegistry to instrument the library itself. If no instance of `MeterRegistry` is present in the [spring application](https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector#what-do-i-get-out-of-the-box), then it uses a built-in No-op implementation. Which means no metrics are recorded

## Configuration

One can also configure the tracer created by the library using few configuration properties and optional beans.

### Disabling Tracer

One can completely disable tracing with configuration property `opentracing.haystack.enabled`. If the property is missing (default), this property value is assumed as `true`.

```yaml
opentracing:
  haystack:
    enabled: false
```

### Configuring Dispatcher(s)

One can configure `Dispatcher` in two ways: using configuration properties or with a spring bean.

Using configuration properties one can configure one or more of the following dispatchers. Configuring more than one dispatcher, creates a `ChainedDispatcher` and sends a span to all of the configured dispatcher instances.

#### Logger Dispatcher

One can configure the name of the logger to use in a LoggerDispatcher by setting the following property in Spring Boot yaml or properties file

```yaml
opentracing:
  haystack:
    dispatchers:
      logger:
        name: span-logger
```

#### Grpc Agent Dispatcher

Haystack provides a [GRPC agent](https://github.com/ExpediaDotCom/haystack-agent) as a convenience to send protobuf spans to Haystack's kafka. One can configure grpc agent by simply enabling it in the configuration. 

```yaml
opentracing:
  haystack:
    dispatchers:
      agent:
        enabled: true
```

There are other properties available to further configure the grpc agent dispatcher

```
opentracing.haystack.dispatchers.agent.host=haystack-agent
opentracing.haystack.dispatchers.agent.keep-alive-time-m-s=30
opentracing.haystack.dispatchers.agent.keep-alive-timeout-m-s=30
opentracing.haystack.dispatchers.agent.keep-alive-without-calls=true
opentracing.haystack.dispatchers.agent.negotiation-type=PLAINTEXT
```

Alternately, one can create a bean of type [GrpcAgentFactory](https://github.com/ExpediaDotCom/haystack-client-java/blob/opentracing-spring-haystack-starter/integrations/opentracing-spring-haystack-starter/src/main/java/com/expedia/haystack/opentracing/spring/starter/support/GrpcDispatcherFactory.java). This bean will be invoked with configuration properties defined to build a RemoteDispatcher with GrpcAgentClient.

```java
public interface GrpcDispatcherFactory {
    Dispatcher create(MetricsRegistry metricsRegistry, 
                      TracerSettings.AgentConfiguration agentConfiguration);
}
```

```java
@Bean
public GrpcDispatcherFactory grpcDispatcherFactory() {
    return (metricsRegistry, config) ->
            new RemoteDispatcher.Builder(metricsRegistry, 
                                         config.builder(metricsRegistry).build()).build();
}
```

### Configuring Metrics

### Configuring Tracer
