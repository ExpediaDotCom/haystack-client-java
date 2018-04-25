/*
 * Copyright 2018 Expedia, Inc.
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
package com.expedia.haystack.dropwizard.configuration;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.dropwizard.configuration.YamlConfigurationFactory;

public class DispatcherFactoryTest extends BaseFactoryTest<DispatcherFactory> {
    private final YamlConfigurationFactory<DispatcherFactory> factory =
        new YamlConfigurationFactory<>(DispatcherFactory.class, validator, objectMapper, "dw");

    @Test
    public void isDiscoverable() throws Exception {
        // Make sure the types we specified in META-INF gets picked up
        isDiscoverable(ImmutableList.of(RemoteDispatcherFactory.class,
                                        LoggerDispatcherFactory.class,
                                        RemoteDispatcherFactory.class));
    }

    @Test
    public void testBuildRemote() throws Exception {
        RemoteDispatcherFactory agent = (RemoteDispatcherFactory) testFactory(factory, "yaml/dispatcher/remote.yml", RemoteDispatcherFactory.class);
    }

    @Test
    public void testBuildLogger() throws Exception {
        testFactory(factory, "yaml/dispatcher/logger.yml", LoggerDispatcherFactory.class);
    }

    @Test
    public void testBuildNoop() throws Exception {
        testFactory(factory, "yaml/dispatcher/noop.yml", NoopDispatcherFactory.class);
    }
}
