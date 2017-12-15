package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("noop")
public class NoopDispatcherFactory implements DispatcherFactory {

    @Override
    public Dispatcher build() {
        return new NoopDispatcher();
    }

}
