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

import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.dropwizard.DropwizardMetricsRegistry;
import com.expedia.www.haystack.client.metrics.dropwizard.NameMapper;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.dropwizard.setup.Environment;

/**
 * A factory for configuring and building {@link DropwizardMetricsRegistry} instances.
 *
 * See {@link MetricsFactory} for more options, if any.
 */
@JsonTypeName("dropwizard")
public class DropwizardMetricsFactory implements MetricsFactory {

    @Nullable
    private NameMapper nameMapper = NameMapper.DEFAULT;

    @Override
    public MetricsRegistry build(Environment environment) {
        return new DropwizardMetricsRegistry(environment.metrics(), nameMapper);
    }
}
