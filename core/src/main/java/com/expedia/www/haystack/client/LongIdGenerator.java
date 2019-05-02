package com.expedia.www.haystack.client;


import java.util.concurrent.ThreadLocalRandom;

public class LongIdGenerator implements IdGenerator {

    @Override
    public Long generateId() {
        return Math.abs(ThreadLocalRandom.current().nextLong());
    }
}
