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
