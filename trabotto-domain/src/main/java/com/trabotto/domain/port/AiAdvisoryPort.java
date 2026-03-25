package com.trabotto.domain.port;

import com.trabotto.domain.model.AiAdvisory;
import com.trabotto.domain.model.AiConstraints;
import com.trabotto.domain.model.BtcSnapshot;
import com.trabotto.domain.model.OpenPositionContext;
import com.trabotto.domain.model.TradeMonitorAdvice;
import com.trabotto.domain.model.TradingContext;

/**
 * Port for AI advisory providers used in entry and monitoring flows.
 */
public interface AiAdvisoryPort {

    AiAdvisory evaluate(TradingContext context, AiConstraints constraints);

    TradeMonitorAdvice monitorPosition(
        OpenPositionContext positionContext,
        BtcSnapshot btcSnapshot,
        AiConstraints constraints
    );
}
