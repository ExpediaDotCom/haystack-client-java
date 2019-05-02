package com.expedia.www.haystack.client;

import com.fasterxml.uuid.Generators;

public class UUIDv3Generator implements IdGenerator {

    @Override
    public Object generateId() {
        return Generators.timeBasedGenerator().generate();
    }
}
