package com.expedia.www.haystack.client;

import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import org.junit.Assert;
import org.junit.Test;



public class TracerBuildTest {


    @Test
    public void testTracerBuildLongIdGenerator(){
        IdGenerator idGenerator = new LongIdGenerator();
        Tracer.Builder tracerBuild = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher());
        tracerBuild.withIdGenerator(idGenerator);
        Tracer tracer = tracerBuild.build();
        Assert.assertNotNull(tracer);
    }

    @Test
    public void testTracerBuildWithoutIdGenerator(){
        Tracer.Builder tracerBuild = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher());
        Tracer tracer = tracerBuild.build();
        Assert.assertNotNull(tracer);
    }

    @Test
    public void testTracerBuildUUIDv3IdGenerator(){
        IdGenerator idGenerator = new UUIDv3Generator("time");
        Tracer.Builder tracerBuild = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher());
        tracerBuild.withIdGenerator(idGenerator);
        Tracer tracer = tracerBuild.build();
        Assert.assertNotNull(tracer);
    }

    @Test
    public void testTracerBuildUUIDv4IdGenerator(){
        IdGenerator idGenerator = new UUIDv4Generator();
        Tracer.Builder tracerBuild = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher());
        tracerBuild.withIdGenerator(idGenerator);
        Tracer tracer = tracerBuild.build();
        Assert.assertNotNull(tracer);
    }


}