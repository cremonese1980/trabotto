package com.trabotto.domain.engine;

import com.trabotto.domain.model.AiAdvisory;
import com.trabotto.domain.model.Decision;
import com.trabotto.domain.model.PolicyCheckResult;
import com.trabotto.domain.model.RuleVerdict;
import com.trabotto.domain.model.Signal;

/**
 * Aggregates rule verdict, AI advisory, and policy checks into the final decision.
 */
public interface DecisionAggregator {

    Decision aggregate(
        Signal signal,
        RuleVerdict ruleVerdict,
        AiAdvisory aiAdvisory,
        PolicyCheckResult policyCheckResult
    );
}
