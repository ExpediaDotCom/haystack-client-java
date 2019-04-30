package com.expedia.www.haystack.client;

import java.util.UUID;

public class UUIDv4Generator implements IdGenerator {

    @Override
    public UUID generateId() {
        return UUID.randomUUID();
    }
}
