package com.trabotto.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabotto.domain.model.Signal;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();

    @Test
    void serializesAndDeserializesSignalRoundTrip() throws Exception {
        Signal signal = new Signal(
                "sig-roundtrip",
                Instant.parse("2026-03-20T08:00:00Z"),
                "SOLUSDT",
                "30m",
                "shimano",
                "Bybit",
                true,
                "telegram",
                "[raw-message]"
        );

        String json = objectMapper.writeValueAsString(signal);
        Signal restored = objectMapper.readValue(json, Signal.class);

        assertThat(restored).isEqualTo(signal);
        assertThat(json).contains("\"timestamp\":\"2026-03-20T08:00:00Z\"");
    }
}
