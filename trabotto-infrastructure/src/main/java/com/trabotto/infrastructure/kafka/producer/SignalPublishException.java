package com.trabotto.infrastructure.kafka.producer;

/**
 * Thrown when a {@link Signal} cannot be serialised or published to Kafka.
 */
public class SignalPublishException extends RuntimeException {

    public SignalPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
