package com.trabotto.boot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabotto.application.SignalProcessingUseCase;
import com.trabotto.domain.model.Signal;
import com.trabotto.infrastructure.kafka.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes normalized signals from Kafka and delegates to the processing use case.
 * Lives in boot module because it wires infrastructure (Kafka) to application (use case).
 */
@Component
public class SignalConsumer {

    private static final Logger log = LoggerFactory.getLogger(SignalConsumer.class);

    private final SignalProcessingUseCase signalProcessingUseCase;
    private final ObjectMapper objectMapper;

    public SignalConsumer(SignalProcessingUseCase signalProcessingUseCase, ObjectMapper objectMapper) {
        this.signalProcessingUseCase = signalProcessingUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaConfig.SIGNALS_INCOMING, groupId = "trabotto-engine")
    public void onSignal(String payload) {
        try {
            Signal signal = objectMapper.readValue(payload, Signal.class);
            signalProcessingUseCase.process(signal);
        } catch (Exception e) {
            log.error("Failed to process signal: {}", payload, e);
        }
    }
}
