package com.expedia.haystack.dropwizard.configuration;

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public interface ClientFactory extends Discoverable {
    Client build();
}

