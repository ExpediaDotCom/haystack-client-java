package com.expedia.www.haystack.client.propagation;

/**
 * Interface to capture any formating required for propagation to work
 * with a carrier.
 *
 */
public interface Codex<R,T> {

    /**
     * Encode a value to the proper format needed by the carrier it's
     * paired with.
     *
     * @param value Value to encode in the proper format for the carrier
     * @return The encoded value
     */
    R encode(T value);

    /**
     * Decode a value from a carrier back into the original format.
     *
     * @param value The encoded value
     * @return The decoded value
     */
    R decode(T value);

}
