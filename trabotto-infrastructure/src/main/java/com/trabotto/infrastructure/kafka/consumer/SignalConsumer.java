package com.trabotto.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabotto.domain.model.Signal;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * Consumes normalised {@link Signal} events from the {@code signals.incoming} Kafka topic
 * and hands them off to the application layer for processing.
 */
@Component
public class SignalConsumer {

    private static final Logger log = LoggerFactory.getLogger(SignalConsumer.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public SignalConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Receives a raw JSON message from {@code signals.incoming}, deserialises it to a
     * {@link Signal}, and logs the key fields for observability.
     *
     * @param record the Kafka record containing the JSON-serialised Signal as its value
     */
    @KafkaListener(
            topics = "signals.incoming",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        Signal signal = deserialise(record.value());
        if (signal == null) {
            return; // deserialization logged the error; skip poison pill
        }

        log.info("Received signal: {} {} {}", signal.pair(), signal.strategy(), signal.timeframe());

        // TODO: wire to SignalProcessingUseCase
    }

    private Signal deserialise(String json) {
        try {
            return objectMapper.readValue(json, Signal.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialise Signal from Kafka message: {}", json, e);
            return null;
        }
    }
}
