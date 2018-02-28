package com.expedia.haystack.dropwizard.configuration;

import javax.annotation.Nullable;

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.LoggerClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A factory for configuring and building {@link LoggerClient} instances.
 * 
 * Configaruation for the logger used is possible; by default it uses the logger for the {@link LoggerClient}.
 *
 * See {@link BaseClientFactory} for more options, if any.
 * See {@link ClientFactory} for more options, if any.
 */
@JsonTypeName("logger")
public class LoggerClientFactory extends BaseClientFactory {

    @Nullable
    private String loggerName;

    public LoggerClientFactory() {
        setFormat(new StringFormatFactory());
    }

    @Override
    public Client build() {
        LoggerClient.Builder builder = new LoggerClient.Builder(getMetrics().build(), getFormat().build());
        if (loggerName != null) {
            builder.withLogger(loggerName);
        }
        return builder.build();
    }

	/**
     * @return the loggerName
     */
    @JsonProperty
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * @param loggerName the loggerName to set
     */
    @JsonProperty
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

}
