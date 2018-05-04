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

package com.expedia.haystack.sleuth.core.instrument.web.brave;

import java.net.URI;
import java.util.List;

import com.expedia.haystack.sleuth.core.instrument.web.adjuster.NameAdjuster;

import brave.SpanCustomizer;
import brave.http.HttpAdapter;
import brave.http.HttpClientParser;

public class HaystackHttpClientParser extends HttpClientParser {

    private final HaystackHttpParserHelper helper;

    public HaystackHttpClientParser(List<NameAdjuster> nameAdjusters) {
        this.helper = new HaystackHttpParserHelper(nameAdjusters);
    }

    @Override
    protected <Req> String spanName(HttpAdapter<Req, ?> adapter, Req req) {
        return helper.getName(URI.create(adapter.url(req)));
    }

    @Override
    protected void error(Integer httpStatus, Throwable error, SpanCustomizer customizer) {
        helper.error(httpStatus, error, customizer);
    }
}
