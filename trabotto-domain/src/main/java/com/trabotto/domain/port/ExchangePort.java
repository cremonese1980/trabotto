package com.trabotto.domain.port;

import com.trabotto.domain.model.Candle;
import com.trabotto.domain.model.OrderRequest;
import com.trabotto.domain.model.OrderResult;
import com.trabotto.domain.model.Position;
import java.math.BigDecimal;
import java.util.List;

/**
 * Port for exchange-specific trading operations.
 */
public interface ExchangePort {

    String exchangeId();

    List<Candle> getCandles(String pair, String timeframe, int count);

    BigDecimal getCurrentPrice(String pair);

    OrderResult openPosition(OrderRequest request);

    OrderResult closePosition(String positionId);

    OrderResult modifyStopLossTakeProfit(String positionId, BigDecimal newSL, BigDecimal newTP);

    List<Position> getOpenPositions();

    boolean isPairAvailable(String pair);

    boolean isTestnet();
}
