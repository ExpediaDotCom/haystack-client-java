package com.expedia.haystack.dropwizard.configuration;

import javax.annotation.Nullable;

import com.expedia.www.haystack.client.dispatchers.LoggerDispatcher;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("logger")
public class LoggerDispatcherFactory implements DispatcherFactory {

    @Nullable
    private String loggerName;

    @Override
    public LoggerDispatcher build() {
        LoggerDispatcher.Builder loggerBuilder = new LoggerDispatcher.Builder();

        if (loggerName != null) {
            loggerBuilder.withLogger(loggerName);
        }

        return loggerBuilder.build();
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
