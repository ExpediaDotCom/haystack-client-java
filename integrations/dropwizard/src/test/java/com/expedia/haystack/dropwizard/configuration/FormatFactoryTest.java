package com.expedia.haystack.dropwizard.configuration;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.dropwizard.configuration.YamlConfigurationFactory;

public class FormatFactoryTest extends BaseFactoryTest<FormatFactory> {
    private final YamlConfigurationFactory<FormatFactory> factory =
        new YamlConfigurationFactory<>(FormatFactory.class, validator, objectMapper, "dw");

    @Test
    public void isDiscoverable() throws Exception {
        // Make sure the types we specified in META-INF gets picked up
        isDiscoverable(ImmutableList.of(StringFormatFactory.class, ProtoBufFormatFactory.class));
    }

    @Test
    public void testBuildStringFormat() throws Exception {
        testFactory(factory, "yaml/format/string.yml", StringFormatFactory.class);
    }

    @Test
    public void testBuildProtobufFormat() throws Exception {
        testFactory(factory, "yaml/format/protobuf.yml", ProtoBufFormatFactory.class);
    }

}
