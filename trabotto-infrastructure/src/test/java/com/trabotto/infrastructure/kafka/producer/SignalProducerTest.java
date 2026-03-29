package com.trabotto.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabotto.domain.model.Signal;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignalProducerTest {

    @Test
    void publishSignalUsesPairAsKafkaKey() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        Signal signal = new Signal(
                "signal-123",
                Instant.parse("2026-02-01T10:00:00Z"),
                "BTCUSDT",
                "5m",
                "viagra_short",
                "Bybit",
                true,
                "bullbot",
                "raw"
        );

        when(objectMapper.writeValueAsString(eq(signal))).thenReturn("{\"id\":\"signal-123\"}");
        when(kafkaTemplate.send(eq("signals.incoming"), eq("BTCUSDT"), eq("{\"id\":\"signal-123\"}")))
                .thenReturn(new CompletableFuture<>());

        SignalProducer producer = new SignalProducer(kafkaTemplate, objectMapper);
        producer.publishSignal(signal);

        verify(kafkaTemplate).send("signals.incoming", "BTCUSDT", "{\"id\":\"signal-123\"}");
    }

    @Test
    void publishSignalWrapsSerializationFailure() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        Signal signal = new Signal(
                "signal-123",
                Instant.parse("2026-02-01T10:00:00Z"),
                "BTCUSDT",
                "5m",
                "viagra_short",
                "Bybit",
                true,
                "bullbot",
                "raw"
        );

        when(objectMapper.writeValueAsString(eq(signal))).thenThrow(new JsonProcessingException("boom") {
        });

        SignalProducer producer = new SignalProducer(kafkaTemplate, objectMapper);

        assertThatThrownBy(() -> producer.publishSignal(signal))
                .isInstanceOf(SignalPublishException.class)
                .hasMessageContaining("signal-123");
    }
}
