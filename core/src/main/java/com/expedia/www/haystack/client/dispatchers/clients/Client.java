package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.Closeable;
import java.io.Flushable;

import com.expedia.www.haystack.client.Span;

/**
 * A Client is how a RemoteDispatcher sends it's finished spans to a remote endpoint
 */
public interface Client extends Closeable, Flushable {


    @Override
    public void close() throws ClientException;

    @Override
    public void flush() throws ClientException;

    /**
     * All clients should control how they send spans to somewhere
     *
     * @param span Span to send off to the endpoint
     * @return Returns <code>true</code> if the operation was successful,
     *         <code>false</code> if it was unsuccessful
     * @throws ClientException throws a <code>ClientException</code> if an exception occured
     */
    boolean send(Span span) throws ClientException;
}
