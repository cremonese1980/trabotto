package com.trabotto.domain.model;

/**
 * Action recommended by strategy evaluation for a trading opportunity.
 */
public enum TradeAction {
    ENTER_LONG,
    ENTER_SHORT,
    SKIP,
    AMBIGUOUS
}
