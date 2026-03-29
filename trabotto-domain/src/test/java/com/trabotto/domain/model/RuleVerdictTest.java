package com.trabotto.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleVerdictTest {

    @ParameterizedTest
    @EnumSource(TradeAction.class)
    void supportsEveryTradeActionValue(TradeAction action) {
        RuleVerdict verdict = new RuleVerdict(
                "ruleset-alpha",
                "v1",
                action,
                75,
                List.of("rule-1", "rule-2"),
                "Matched rule set"
        );

        assertThat(verdict.action()).isEqualTo(action);
        assertThat(verdict.confidence()).isEqualTo(75);
    }
}
