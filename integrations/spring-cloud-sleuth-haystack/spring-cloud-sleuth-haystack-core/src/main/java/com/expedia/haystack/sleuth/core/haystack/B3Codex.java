package com.expedia.haystack.sleuth.core.haystack;

import java.util.UUID;

import com.expedia.www.haystack.client.propagation.Codex;

import brave.internal.HexCodec;
import lombok.val;

/**
 * Encode/decode from B3 to Haystack
 */
public class B3Codex implements Codex<String, String> {

    @Override
    public String encode(String value) {
        long low = HexCodec.lowerHexToUnsignedLong(value);

        long high = 0L;

        if (value.length() == 32) {
            high = HexCodec.lowerHexToUnsignedLong(value, 0);
        }

        return new UUID(high, low).toString();
    }

    @Override
    public String decode(String value) {
        UUID uuid = UUID.fromString(value);
        val leastSignificantBits = uuid.getLeastSignificantBits();
        val mostSignificantBits = uuid.getMostSignificantBits();

        return HexCodec.toLowerHex(mostSignificantBits, leastSignificantBits);
    }
}
