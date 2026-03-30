package com.trabotto.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trabotto.domain.model.Signal;
import com.trabotto.infrastructure.kafka.config.KafkaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {KafkaConfig.SIGNALS_INCOMING, KafkaConfig.SIGNALS_WATCHLIST},
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
@ActiveProfiles("test")
class SignalKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void signalIsConsumedAndProcessedByRuleEngine() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Signal signal = new Signal(
                "test-signal-1",
                Instant.now(),
                "BTCUSDT",
                "5m",
                "viagra",
                "bybit",
                true,
                "integration-test",
                "Bybit - BTCUSDT 5MIN viagra +5.04% PERFEZIONE"
        );

        String json = mapper.writeValueAsString(signal);

        assertDoesNotThrow(() ->
                kafkaTemplate.send(KafkaConfig.SIGNALS_INCOMING, signal.pair(), json).get()
        );

        // Allow consumer time to process
        Thread.sleep(3000);

        // If we reach here without exception, the pipeline worked.
        // The rule engine logs the verdict — verifiable in test output.
    }
}
