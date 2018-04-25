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
package com.expedia.www.haystack.client.dispatchers;

import java.io.Closeable;
import java.io.Flushable;

import com.expedia.www.haystack.client.Span;

/**
 * A Dispatcher is where a Tracer sends it's finished spans to be collected at some location.
 */
public interface Dispatcher extends Closeable, Flushable {

    /**
     * All dispatchers should dispatch to somewhere
     *
     * @param span Span to dispatch to the registered sink
     */
    void dispatch(Span span);

}

