package com.trabotto.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable exchange response after an order operation.
 */
public record OrderResult(
    String orderId,
    String positionId,
    String exchange,
    boolean accepted,
    String reason,
    BigDecimal executedPrice,
    BigDecimal executedQuantity,
    Instant executedAt
) {
}
