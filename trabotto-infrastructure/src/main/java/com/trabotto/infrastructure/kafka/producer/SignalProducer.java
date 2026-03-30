package com.trabotto.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabotto.domain.model.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes normalised {@link Signal} events to the {@code signals.incoming} Kafka topic.
 *
 * <p>Key = {@code signal.pair()} — ensures all signals for the same trading pair land on the
 * same partition (ordered processing per pair) and enables consumer-side deduplication.
 * Value = JSON-serialised Signal.
 */
@Component
public class SignalProducer {

    private static final Logger log = LoggerFactory.getLogger(SignalProducer.class);
    private static final String TOPIC = "signals.incoming";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SignalProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Serialises {@code signal} to JSON and publishes it to {@code signals.incoming}.
     *
     * @param signal the normalised signal to publish
     * @throws SignalPublishException if JSON serialisation fails
     */
    public void publishSignal(Signal signal) {
        String payload = toJson(signal);
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(TOPIC, signal.pair(), payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish signal id={} pair={} strategy={}: {}",
                        signal.id(), signal.pair(), signal.strategy(), ex.getMessage(), ex);
            } else {
                log.info("Published signal id={} pair={} strategy={} timeframe={} topic={} partition={} offset={}",
                        signal.id(),
                        signal.pair(),
                        signal.strategy(),
                        signal.timeframe(),
                        TOPIC,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String toJson(Signal signal) {
        try {
            return objectMapper.writeValueAsString(signal);
        } catch (JsonProcessingException e) {
            throw new SignalPublishException("Failed to serialise signal id=" + signal.id(), e);
        }
    }
}
