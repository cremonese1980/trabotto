package com.trabotto.domain.engine;

import com.trabotto.domain.model.Decision;
import com.trabotto.domain.model.PolicyCheckResult;

/**
 * Pre-execution policy engine contract.
 */
public interface PolicyEngine {

    PolicyCheckResult validate(Decision decision);
}
