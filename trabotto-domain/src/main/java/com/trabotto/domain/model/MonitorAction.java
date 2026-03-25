package com.trabotto.domain.model;

/**
 * Action proposed by AI while monitoring an already open trade.
 */
public enum MonitorAction {
    HOLD,
    MOVE_SL_TO_BREAKEVEN,
    TIGHTEN_TP,
    CLOSE_NOW
}
