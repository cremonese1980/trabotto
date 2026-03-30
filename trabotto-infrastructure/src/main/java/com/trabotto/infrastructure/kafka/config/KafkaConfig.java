package com.trabotto.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates all Kafka topics at application startup via KafkaAdmin auto-configuration.
 * Topics are idempotent: existing topics with matching config are left untouched.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String SIGNALS_INCOMING = "signals.incoming";
    public static final String SIGNALS_WATCHLIST = "signals.watchlist";
    public static final String DECISIONS_PENDING = "decisions.pending";

    // Normalised signals entering the decision pipeline
    @Bean
    public NewTopic signalsIncoming() {
        return TopicBuilder.name("signals.incoming").partitions(1).replicas(1).build();
    }

    // Decision engine output awaiting policy approval
    @Bean
    public NewTopic decisionsPending() {
        return TopicBuilder.name("decisions.pending").partitions(1).replicas(1).build();
    }

    // Policy-approved orders ready for exchange execution
    @Bean
    public NewTopic ordersApproved() {
        return TopicBuilder.name("orders.approved").partitions(1).replicas(1).build();
    }

    // Execution confirmations from the exchange
    @Bean
    public NewTopic ordersExecuted() {
        return TopicBuilder.name("orders.executed").partitions(1).replicas(1).build();
    }

    // Policy-rejected decisions (audit trail)
    @Bean
    public NewTopic ordersRejected() {
        return TopicBuilder.name("orders.rejected").partitions(1).replicas(1).build();
    }

    // Each AI monitoring evaluation tick for an open position
    @Bean
    public NewTopic monitorTick() {
        return TopicBuilder.name("monitor.tick").partitions(1).replicas(1).build();
    }

    // SL/TP modifications or early closes triggered by the monitor
    @Bean
    public NewTopic monitorAction() {
        return TopicBuilder.name("monitor.action").partitions(1).replicas(1).build();
    }

    // Closed trades with full P&L outcome
    @Bean
    public NewTopic tradesClosed() {
        return TopicBuilder.name("trades.closed").partitions(1).replicas(1).build();
    }

    // Shadow position opened for a signal that was rejected by the policy engine
    @Bean
    public NewTopic shadowOpened() {
        return TopicBuilder.name("shadow.opened").partitions(1).replicas(1).build();
    }

    // Shadow position closed with theoretical outcome
    @Bean
    public NewTopic shadowClosed() {
        return TopicBuilder.name("shadow.closed").partitions(1).replicas(1).build();
    }
}
