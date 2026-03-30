package com.trabotto.application.engine;

import com.trabotto.domain.model.Rule;
import com.trabotto.domain.model.RuleSet;
import com.trabotto.domain.model.RuleVerdict;
import com.trabotto.domain.model.Signal;
import com.trabotto.domain.model.TradeAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRuleEngineTest {

    private DefaultRuleEngine engine;
    private RuleSet ruleSet;

    @BeforeEach
    void setUp() {
        engine = new DefaultRuleEngine();

        List<Rule> rules = List.of(
                new Rule("BULL_V_01", "SETUP", "viagra,scalping", 1, true,
                        TradeAction.ENTER_LONG, "ema5_above_ema10 == true"),
                new Rule("BULL_V_02", "RULE", "viagra,ema5", 1, true,
                        TradeAction.ENTER_LONG, "volume_high == true"),
                new Rule("BULL_V_03", "INVALIDATION", "viagra,invalidation", 1, true,
                        TradeAction.SKIP, "btc_dropping == true"),
                new Rule("BULL_B_01", "SETUP", "bomba,breakout", 1, true,
                        TradeAction.ENTER_LONG, "breakout_confirmed == true"),
                new Rule("BULL_B_02", "RULE", "bomba,ema60", 1, true,
                        TradeAction.ENTER_SHORT, "ema60_resistance == true"),
                new Rule("BULL_G_01", "RULE", "general", 2, true,
                        TradeAction.SKIP, "market_closed == true")
        );

        ruleSet = new RuleSet("test", "1.0", "test", Instant.now(), rules);
    }

    @Test
    void viagraStrategyMatchesViagraRules() {
        Signal signal = signal("viagra", false);
        RuleVerdict verdict = engine.evaluate(signal, ruleSet);

        // Should match BULL_V_01 (SETUP) and BULL_V_02 (RULE), NOT BULL_V_03 (INVALIDATION)
        assertEquals(2, verdict.matchedRuleIds().size());
        assertTrue(verdict.matchedRuleIds().contains("BULL_V_01"));
        assertTrue(verdict.matchedRuleIds().contains("BULL_V_02"));
        assertFalse(verdict.matchedRuleIds().contains("BULL_V_03"));
    }

    @Test
    void bombaStrategyMatchesBombaRules() {
        Signal signal = signal("bomba", false);
        RuleVerdict verdict = engine.evaluate(signal, ruleSet);

        assertEquals(2, verdict.matchedRuleIds().size());
        assertTrue(verdict.matchedRuleIds().contains("BULL_B_01"));
        assertTrue(verdict.matchedRuleIds().contains("BULL_B_02"));
    }

    @Test
    void unknownStrategyMatchesZeroRulesAndReturnsSkip() {
        Signal signal = signal("nonexistent_strategy", false);
        RuleVerdict verdict = engine.evaluate(signal, ruleSet);

        assertEquals(TradeAction.SKIP, verdict.action());
        assertTrue(verdict.matchedRuleIds().isEmpty());
        assertEquals(0, verdict.confidence());
    }

    @Test
    void perfezioneIncreasesConfidence() {
        Signal withoutPerf = signal("viagra", false);
        Signal withPerf = signal("viagra", true);

        RuleVerdict verdictNo = engine.evaluate(withoutPerf, ruleSet);
        RuleVerdict verdictYes = engine.evaluate(withPerf, ruleSet);

        assertEquals(verdictNo.confidence() + 20, verdictYes.confidence());
    }

    @Test
    void confidenceCappedAt100() {
        // Build a ruleset with many matching rules to overflow confidence
        List<Rule> manyRules = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            manyRules.add(new Rule("R_" + i, "RULE", "overflow", 1, true,
                    TradeAction.ENTER_LONG, "condition == true"));
        }
        RuleSet bigSet = new RuleSet("big", "1.0", "test", Instant.now(), manyRules);

        Signal signal = new Signal("s1", Instant.now(), "BTCUSDT", "5m",
                "overflow", "bybit", true, "src1", "raw");
        RuleVerdict verdict = engine.evaluate(signal, bigSet);

        // 50 + (10 * 20) + 20 = 270 → capped at 100
        assertEquals(100, verdict.confidence());
    }

    @Test
    void mostCommonActionIsSelected() {
        // bomba has BULL_B_01 (ENTER_LONG) and BULL_B_02 (ENTER_SHORT) - tie broken by stream order
        Signal signal = signal("bomba", false);
        RuleVerdict verdict = engine.evaluate(signal, ruleSet);
        // With equal counts, either is acceptable
        assertNotNull(verdict.action());
        assertTrue(verdict.action() == TradeAction.ENTER_LONG
                || verdict.action() == TradeAction.ENTER_SHORT);
    }

    @Test
    void ruleSetMetadataPreservedInVerdict() {
        Signal signal = signal("viagra", false);
        RuleVerdict verdict = engine.evaluate(signal, ruleSet);

        assertEquals("test", verdict.ruleSetId());
        assertEquals("1.0", verdict.ruleSetVersion());
    }

    private static Signal signal(String strategy, boolean perfezione) {
        return new Signal("sig-1", Instant.now(), "BTCUSDT", "5m",
                strategy, "bybit", perfezione, "src1", "raw message");
    }
}
