package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.expedia.www.haystack.client.Span;

public class ChainedDispatcher implements Dispatcher {
    private final List<Dispatcher> dispatchers;

    public ChainedDispatcher(Dispatcher... dispatchers) {
        this.dispatchers = new ArrayList<>();
        Collections.addAll(this.dispatchers, dispatchers);
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
}
