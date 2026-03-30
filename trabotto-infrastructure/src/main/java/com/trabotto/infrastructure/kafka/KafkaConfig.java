package com.trabotto.infrastructure.kafka;

import org.springframework.context.annotation.Configuration;

/**
 * Kafka topic constants and configuration.
 */
@Configuration
public class KafkaConfig {

    public static final String SIGNALS_INCOMING = "signals.incoming";
    public static final String SIGNALS_WATCHLIST = "signals.watchlist";
    public static final String DECISIONS_PENDING = "decisions.pending";
}
