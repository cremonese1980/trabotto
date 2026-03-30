package com.trabotto.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabotto.domain.model.Signal;
import com.trabotto.infrastructure.kafka.consumer.SignalConsumer;
import com.trabotto.infrastructure.kafka.producer.SignalProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Integration test verifying the full Kafka round-trip:
 * {@link SignalProducer} publishes a {@link Signal} → Kafka → {@link SignalConsumer} receives it.
 *
 * <p>A real Kafka broker runs inside a Docker container managed by Testcontainers.
 * {@link DynamicPropertySource} wires the broker address into the Spring context at test startup.
 */
@SpringBootTest(classes = KafkaTestApplication.class)
@Testcontainers
class SignalKafkaIntegrationTest {

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void overrideKafkaBootstrapServers(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "trabotto-test");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
    }

    @Autowired
    private SignalProducer signalProducer;

    /** Wrapped in a Mockito spy so we can verify invocations without modifying production code. */
    @SpyBean
    private SignalConsumer signalConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publishedSignalIsReceivedByConsumer() throws Exception {
        Signal signal = new Signal(
                "test-id-001",
                Instant.parse("2024-06-01T10:00:00Z"),
                "BTCUSDT",
                "5",
                "viagra",
                "Bybit",
                true,
                "test-source",
                "raw message text"
        );

        signalProducer.publishSignal(signal);

        // Wait up to 10 s for the consumer to receive and process the message
        ArgumentCaptor<ConsumerRecord<String, String>> captor =
                ArgumentCaptor.forClass(ConsumerRecord.class);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> verify(signalConsumer, atLeastOnce()).onMessage(captor.capture()));

        // Deserialise the captured payload and assert the domain fields
        ConsumerRecord<String, String> received = captor.getValue();
        Signal deserialized = objectMapper.readValue(received.value(), Signal.class);

        assertThat(deserialized.pair()).isEqualTo("BTCUSDT");
        assertThat(deserialized.strategy()).isEqualTo("viagra");
        assertThat(deserialized.timeframe()).isEqualTo("5");
        assertThat(deserialized.id()).isEqualTo("test-id-001");
    }

    @Test
    void preservesAllSignalFieldsDuringKafkaRoundTrip() throws Exception {
        Signal signal = new Signal(
                "test-id-002",
                Instant.parse("2024-06-01T12:34:56Z"),
                "ETHUSDT",
                "30m",
                "shimano",
                "Binance",
                false,
                "test-source-2",
                "raw two"
        );

        signalProducer.publishSignal(signal);

        ArgumentCaptor<ConsumerRecord<String, String>> captor =
                ArgumentCaptor.forClass(ConsumerRecord.class);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> verify(signalConsumer, atLeastOnce()).onMessage(captor.capture()));

        ConsumerRecord<String, String> received = captor.getValue();
        Signal deserialized = objectMapper.readValue(received.value(), Signal.class);

        assertThat(deserialized).isEqualTo(signal);
        assertThat(received.key()).isEqualTo("ETHUSDT");
    }
}
