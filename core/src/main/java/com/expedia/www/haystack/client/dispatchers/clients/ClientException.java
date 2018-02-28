package com.expedia.www.haystack.client.dispatchers.clients;

/**
 * An Exception to indicate a client failure
 *
 */
public class ClientException extends RuntimeException {
    public ClientException() {
        super();
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }
}
