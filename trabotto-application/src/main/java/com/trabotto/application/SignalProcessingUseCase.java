package com.trabotto.application;

import com.trabotto.domain.engine.RuleEngine;
import com.trabotto.domain.model.RuleSet;
import com.trabotto.domain.model.RuleVerdict;
import com.trabotto.domain.model.Signal;
import com.trabotto.domain.model.TradeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Processes incoming signals through the rule engine and logs the verdict.
 * Future: publish Decision to Kafka decisions.pending topic.
 */
@Service
public class SignalProcessingUseCase {

    private static final Logger log = LoggerFactory.getLogger(SignalProcessingUseCase.class);

    private final RuleEngine ruleEngine;
    private final RuleSet ruleSet;

    public SignalProcessingUseCase(RuleEngine ruleEngine, RuleSet ruleSet) {
        this.ruleEngine = ruleEngine;
        this.ruleSet = ruleSet;
    }

    public RuleVerdict process(Signal signal) {
        RuleVerdict verdict = ruleEngine.evaluate(signal, ruleSet);

        log.info("Signal {}/{} → {} confidence={} rules={}",
                signal.pair(),
                signal.strategy(),
                verdict.action(),
                verdict.confidence(),
                verdict.matchedRuleIds());

        if (verdict.action() == TradeAction.AMBIGUOUS) {
            log.info("Signal {}/{} → WATCHLIST",
                    signal.pair(), signal.strategy());
            // TODO: publish to signals.watchlist for periodic re-evaluation
        }

        return verdict;
    }
}
