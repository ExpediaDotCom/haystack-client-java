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

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.NoopClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.dropwizard.setup.Environment;

/**
 * A factory for configuring and building {@link NoopClient} instances.
 *
 * All configaruation is ignored by the client.
 *
 * See {@link BaseClientFactory} for more options, if any.
 * See {@link ClientFactory} for more options, if any.
 */
@JsonTypeName("noop")
public class NoopClientFactory extends BaseClientFactory {

    public NoopClientFactory() {
        // set a format to pass validation
        setFormat(new StringFormatFactory());
    }

	@Override
	public Client build(Environment environment, MetricsRegistry metrics) {
      return new NoopClient();
	}

}
