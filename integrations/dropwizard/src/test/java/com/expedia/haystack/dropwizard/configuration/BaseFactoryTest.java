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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

public abstract class BaseFactoryTest<T> {
    protected final ObjectMapper objectMapper = Jackson.newObjectMapper();
    protected final Validator validator = Validators.newValidator();

    protected void isDiscoverable(Iterable<? extends Class<?>> factories) throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .containsAll(factories);
    }

    protected T testFactory(YamlConfigurationFactory<T> factory, String yamlFile, Class clazz) throws Exception {
        final File yml = new File(Resources.getResource(yamlFile).toURI());
        T t = factory.build(yml);
        assertThat(t).isInstanceOf(clazz);
        return t;
    }
}
