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

import javax.annotation.Nullable;

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.LoggerClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.dropwizard.setup.Environment;

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
    public Client build(Environment environment, MetricsRegistry metrics) {
        LoggerClient.Builder builder = new LoggerClient.Builder(metrics, getFormat().build(environment));
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
