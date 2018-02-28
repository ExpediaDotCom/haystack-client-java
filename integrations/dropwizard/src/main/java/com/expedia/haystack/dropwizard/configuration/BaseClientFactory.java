package com.expedia.haystack.dropwizard.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseClientFactory implements ClientFactory {

    @NotNull
    protected MetricsFactory metrics = new NoopMetricsFactory();

    @NotNull
    protected FormatFactory format;

    @JsonProperty
    public MetricsFactory getMetrics() {
        return metrics;
    }

    @JsonProperty
    public void setMetrics(MetricsFactory metrics) {
        this.metrics = metrics;
    }

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
