package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.ClientException;

public class RemoteDispatcher implements Dispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDispatcher.class);

    private Client client;

    public RemoteDispatcher(Client client) {
        this.client = client;
    }

    @Override
    public void dispatch(Span span) {
        try {
            client.send(span);
        } catch (ClientException e) {
            LOGGER.error("Client Failure: {}:{}", client.getClass(), e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public void flush() throws IOException {
        client.flush();
    }

}
