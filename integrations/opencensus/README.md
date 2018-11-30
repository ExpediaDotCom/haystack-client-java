# Opencensus Trace Exporter for Haystack

The OpenCensus Haystack Trace Exporter is a trace exporter that exports data to haystack.

To know about haystack checkout [here](https://expediadotcom.github.io/haystack/)

## Quickstart

#### Add the dependencies to your project
For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.17.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-haystack</artifactId>
    <version>[0.2.1,)</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.17.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.17.0'
compile 'io.opencensus:opencensus-exporter-trace-haystack:0.2.1'
runtime 'io.opencensus:opencensus-impl:0.17.0'
```

#### Register the exporter

This will export traces to the haystack:

```java
public class MainClass {
  public static void main(String[] args) throws Exception {
    com.www.expedia.opencensus.exporter.trace.HaystackTraceExporter.createAndRegister(new GrpcAgentDispatcherConfig("haystack-agent", 35000), "my-service");
    // ...
  }
}
```

You can look into the integration test [here](src/test/scala/com/www/expedia/opencensus/exporter/trace/HaystackExporterIntegrationSpec.scala)