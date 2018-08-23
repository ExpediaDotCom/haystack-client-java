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

import com.expedia.www.haystack.client.Span;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChainedDispatcher implements Dispatcher {
    private final List<Dispatcher> dispatchers;

    public ChainedDispatcher(List<Dispatcher> dispatchers) {
        this.dispatchers = Collections.unmodifiableList(dispatchers);
    }

    public ChainedDispatcher(Dispatcher... dispatchers) {
        final ArrayList<Dispatcher> holder = new ArrayList<>();
        Collections.addAll(holder, dispatchers);
        this.dispatchers = Collections.unmodifiableList(holder);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .toString();
    }

    @Override
    public void dispatch(Span span) {
        for (Dispatcher dispatcher : dispatchers) {
            dispatcher.dispatch(span);
        }
    }

    @Override
    public void close() throws IOException {
        List<IOException> exceptions = new ArrayList<>();

        for (Dispatcher dispatcher : dispatchers) {
            try {
                dispatcher.close();
            } catch (IOException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            // rethrow the first failure
            throw exceptions.get(0);
        }
    }

    @Override
    public void flush() throws IOException {
        List<IOException> exceptions = new ArrayList<>();

        for (Dispatcher dispatcher : dispatchers) {
            try {
                dispatcher.flush();
            } catch (IOException e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            // rethrow the first failure
            throw exceptions.get(0);
        }
    }

    public static class Builder {
        private List<Dispatcher> dispatchers = new ArrayList<>();

        public Builder withDispatcher(Dispatcher dispatcher) {
            dispatchers.add(dispatcher);
            return this;
        }

        public ChainedDispatcher build() {
            return new ChainedDispatcher(dispatchers);
        }
    }
}
