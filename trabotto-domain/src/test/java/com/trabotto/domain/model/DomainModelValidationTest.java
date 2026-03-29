package com.trabotto.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelValidationTest {

    @Test
    void signalRejectsBlankId() {
        assertThatThrownBy(() -> new Signal(
                " ",
                Instant.parse("2026-01-01T00:00:00Z"),
                "BTCUSDT",
                "5m",
                "viagra_short",
                "Bybit",
                true,
                "bullbot",
                "raw"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("id");
    }

    @Test
    void advisoryRejectsInvalidConfidence() {
        assertThatThrownBy(() -> new AiAdvisory(TradeAction.ENTER_LONG, 101, "High conviction", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confidence");
    }

    @Test
    void ruleVerdictDefensivelyCopiesMatchedRuleIds() {
        List<String> mutableIds = new ArrayList<>();
        mutableIds.add("rule-1");

        RuleVerdict verdict = new RuleVerdict("base", "v1", TradeAction.ENTER_SHORT, 80, mutableIds, "All checks passed");
        mutableIds.add("rule-2");

        assertThat(verdict.matchedRuleIds()).containsExactly("rule-1");
        assertThatThrownBy(() -> verdict.matchedRuleIds().add("rule-3"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void decisionRequiresCompleteObjectGraph() {
        Signal signal = new Signal(
                "signal-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                "BTCUSDT",
                "5m",
                "viagra_short",
                "Bybit",
                true,
                "bullbot",
                "[example raw message]"
        );
        RuleVerdict verdict = new RuleVerdict("base", "v1", TradeAction.ENTER_LONG, 70, List.of("rule-1"), "Momentum setup");
        AiAdvisory advisory = new AiAdvisory(TradeAction.ENTER_LONG, 65, "Market context confirms setup", true);
        PolicyCheckResult policy = new PolicyCheckResult(true, List.of(), "All pre-trade checks passed");

        Decision decision = new Decision(
                "decision-1",
                Instant.parse("2026-01-01T00:01:00Z"),
                signal,
                verdict,
                advisory,
                policy,
                TradeAction.ENTER_LONG,
                "Rule and policy agree on long entry"
        );

        assertThat(decision.finalAction()).isEqualTo(TradeAction.ENTER_LONG);
    }
}
