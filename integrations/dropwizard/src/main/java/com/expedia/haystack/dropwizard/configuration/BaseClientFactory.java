package com.expedia.haystack.dropwizard.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseClientFactory implements ClientFactory {
    
    @NotNull
    protected FormatFactory format;
    
    /**
     * @return the format
     */
    @JsonProperty
    public FormatFactory getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    @JsonProperty
    public void setFormat(FormatFactory format) {
        this.format = format;
    }
}
