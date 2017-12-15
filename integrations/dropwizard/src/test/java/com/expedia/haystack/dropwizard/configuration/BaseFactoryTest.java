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
