package com.trabotto.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SignalTest {

    @Test
    void createsRecordWithAllFields() {
        Signal signal = new Signal(
                "sig-1",
                Instant.parse("2026-03-01T10:15:30Z"),
                "BTCUSDT",
                "5m",
                "viagra_long",
                "Bybit",
                true,
                "telegram",
                "raw"
        );

        assertThat(signal.id()).isEqualTo("sig-1");
        assertThat(signal.timestamp()).isEqualTo(Instant.parse("2026-03-01T10:15:30Z"));
        assertThat(signal.pair()).isEqualTo("BTCUSDT");
        assertThat(signal.timeframe()).isEqualTo("5m");
        assertThat(signal.strategy()).isEqualTo("viagra_long");
        assertThat(signal.exchange()).isEqualTo("Bybit");
        assertThat(signal.perfezione()).isTrue();
        assertThat(signal.sourceId()).isEqualTo("telegram");
        assertThat(signal.rawMessage()).isEqualTo("raw");
    }

    @Test
    void allowsNullValuesForReferenceFields() {
        Signal signal = new Signal(null, null, null, null, null, null, false, null, null);

        assertThat(signal.id()).isNull();
        assertThat(signal.timestamp()).isNull();
        assertThat(signal.pair()).isNull();
        assertThat(signal.timeframe()).isNull();
        assertThat(signal.strategy()).isNull();
        assertThat(signal.exchange()).isNull();
        assertThat(signal.perfezione()).isFalse();
        assertThat(signal.sourceId()).isNull();
        assertThat(signal.rawMessage()).isNull();
    }

    @Test
    void hasValueBasedEquality() {
        Signal left = new Signal("id-1", Instant.parse("2026-01-01T00:00:00Z"), "ETHUSDT", "1h", "shimano", "Bybit", false, "src", "raw");
        Signal right = new Signal("id-1", Instant.parse("2026-01-01T00:00:00Z"), "ETHUSDT", "1h", "shimano", "Bybit", false, "src", "raw");

        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
    }
}
