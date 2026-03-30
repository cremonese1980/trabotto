package com.trabotto.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyCheckResultTest {

    @Test
    void createsPolicyCheckResult() {
        PolicyCheckResult result = new PolicyCheckResult(
                false,
                List.of("max_positions_exceeded"),
                "Rejected by risk policy"
        );

        assertThat(result.approved()).isFalse();
        assertThat(result.violations()).containsExactly("max_positions_exceeded");
        assertThat(result.summary()).isEqualTo("Rejected by risk policy");
    }
}
