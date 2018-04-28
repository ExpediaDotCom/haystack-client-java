/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.haystack.sleuth.core.reporter.base;

import java.util.ArrayList;
import java.util.List;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.ClientException;

import lombok.Getter;

public class AccumulatorClient implements Client {

    @Getter
    private List<Span> spans = new ArrayList<>();

    @Override
    public void close() throws ClientException {
        flush();
    }

    @Override
    public void flush() throws ClientException {
        spans.clear();
    }

    @Override
    public boolean send(Span span) throws ClientException {
        return spans.add(span);
    }
}
