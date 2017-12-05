package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.expedia.www.haystack.client.dispatchers.formats.StringFormat;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("string")
public class StringFormatFactory implements FormatFactory {
    public Format<?> build() {
        return new StringFormat();
    }
}

