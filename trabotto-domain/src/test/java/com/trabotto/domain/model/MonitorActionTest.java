package com.trabotto.domain.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorActionTest {

    @Test
    void containsExpectedEnumValues() {
        assertThat(EnumSet.allOf(MonitorAction.class))
                .containsExactlyInAnyOrder(
                        MonitorAction.HOLD,
                        MonitorAction.MOVE_SL_TO_BREAKEVEN,
                        MonitorAction.TIGHTEN_TP,
                        MonitorAction.CLOSE_NOW
                );
    }
}
