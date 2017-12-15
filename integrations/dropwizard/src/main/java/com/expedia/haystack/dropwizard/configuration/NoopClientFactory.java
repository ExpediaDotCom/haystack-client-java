package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.NoopClient;
import com.fasterxml.jackson.annotation.JsonTypeName;

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
	public Client build() {
      return new NoopClient();
	}

}
