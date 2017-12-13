package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.expedia.www.haystack.client.Span;

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
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
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
        private List<Dispatcher> dispatchers = new ArrayList<>();;

        public Builder withDispatcher(Dispatcher dispatcher) {
            dispatchers.add(dispatcher);
            return this;
        }

        public ChainedDispatcher build() {
            return new ChainedDispatcher(dispatchers);
        }
    }
}
