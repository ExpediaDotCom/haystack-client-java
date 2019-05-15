# Benchmarks


One of the deciding factor for performance of haystack client is the Id Generation Process.There are more than one way to implement the Id generator for traces and spans. We have added three implementations : LongId Generator(Default), RandomUUID Generator, TimeBasedUUID Generator. The Id Generators were implemented based on uniqueness and performance.For eg:  Long ID Generator will give you less assurance for uniqueness as compared to UUID implementations, while the performance of Long ID generator will better. You are also free to add your own ID generator implementations.
We have benchmark the performance of client with each Id Generator Implementations:

### Using Long-ID Implementation
![Long-ID Implementation](longIDjmhvisulaizer.png)

### Using Random-UUID-ID Implementation
![Long-ID Implementation](randomuuid.png)

### Using TimeBased-UUID-ID Implementation
![Long-ID Implementation](timebaseduuid.png)


## To Measure Performance of client

To measure the performance of the client, you would need to clone: https://github.com/gsoria/opentracing-java-benchmark/tree/master/opentracing-benchmark-simple-java. Once this is complete, update haystack.version in pom.xml with the current version of your haystack client.
Run the following commands (inside on this specific project)

```bash
mvn clean install
java -jar target/benchmarks.jar
```

Once you are done. Use [JMH-visualizer(Online version)](http://jmh.morethan.io/) to present the benchmark results.
