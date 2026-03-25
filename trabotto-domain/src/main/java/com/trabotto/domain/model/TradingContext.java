package com.trabotto.domain.model;

import java.util.List;

/**
 * Entry evaluation context given to AI before opening a position.
 */
public record TradingContext(
    Signal signal,
    RuleVerdict ruleVerdict,
    List<Candle> recentCandles,
    BtcSnapshot btcSnapshot
) {
}
