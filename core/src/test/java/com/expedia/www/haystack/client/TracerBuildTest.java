/*
 * Copyright 2019 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client;

import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.idgenerators.IdGenerator;
import com.expedia.www.haystack.client.idgenerators.LongIdGenerator;
import com.expedia.www.haystack.client.idgenerators.RandomUUIDGenerator;
import com.expedia.www.haystack.client.idgenerators.TimeBasedUUIDGenerator;
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
        IdGenerator idGenerator = new TimeBasedUUIDGenerator();
        Tracer.Builder tracerBuild = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher());
        tracerBuild.withIdGenerator(idGenerator);
        Tracer tracer = tracerBuild.build();
        Assert.assertNotNull(tracer);
    }

    @Test
    public void testTracerBuildUUIDv4IdGenerator(){
        IdGenerator idGenerator = new RandomUUIDGenerator();
        Tracer.Builder tracerBuild = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher());
        tracerBuild.withIdGenerator(idGenerator);
        Tracer tracer = tracerBuild.build();
        Assert.assertNotNull(tracer);
    }


}