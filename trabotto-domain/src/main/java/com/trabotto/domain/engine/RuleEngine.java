package com.trabotto.domain.engine;

import com.trabotto.domain.model.RuleSet;
import com.trabotto.domain.model.RuleVerdict;
import com.trabotto.domain.model.Signal;

/**
 * Deterministic rule engine contract for evaluating signals.
 */
public interface RuleEngine {

    RuleVerdict evaluate(Signal signal, RuleSet ruleSet);
}
