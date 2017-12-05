package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.expedia.www.haystack.client.dispatchers.formats.ProtoBufFormat;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("protobuf")
public class ProtoBufFormatFactory implements FormatFactory {
    public Format<?> build() {
        return new ProtoBufFormat();
    }
}


