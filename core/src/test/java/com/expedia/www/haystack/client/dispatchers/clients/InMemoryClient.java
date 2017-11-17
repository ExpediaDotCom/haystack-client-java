package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;

public class InMemoryClient extends NoopClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryClient.class);

    private Semaphore limiter;
    private List<Span> total;
    private List<Span> recieved;
    private List<Span> flushed;

    public InMemoryClient() {
        this(Integer.MAX_VALUE);
    }

    public InMemoryClient(int limit) {
        limiter = new Semaphore(limit);
        total = new ArrayList<>();
        recieved = new ArrayList<>();
        flushed = new ArrayList<>();
    }

    @Override
    public boolean send(Span span) {
        LOGGER.info("Span sent to client: " + span);
        try {
            limiter.acquire();
            total.add(span);
            recieved.add(span);
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("Client closed");
        flush();
    }

    @Override
    public void flush() throws IOException {
        LOGGER.info("Client flushed");
        flushed.addAll(recieved);
        recieved.clear();
    }

    public List<Span> getTotalSpans() {
        return Collections.unmodifiableList(total);
    }

    public List<Span> getFlushedSpans() {
        return Collections.unmodifiableList(flushed);
    }

    public List<Span> getRecievedSpans() {
        return Collections.unmodifiableList(recieved);
    }
}
