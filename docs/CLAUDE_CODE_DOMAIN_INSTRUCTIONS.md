# Instructions for Claude Code — Trabotto Domain Model

## Context
You are working on Trabotto, a crypto trading decision engine.
Read `docs/ARCHITECTURE.md` for the full system design.
This task: create the `trabotto-domain` Maven module with the core domain model.

## Constraints
- Java 21, no framework dependencies (NO Spring, NO Jakarta, NO external libs)
- The domain module must compile with ZERO dependencies (only java.base)
- Comments in English
- Production-grade code, SOLID principles
- All classes are records or interfaces — no mutable state in domain model
- Package: `com.trabotto.domain`

## What to Create

### Module Structure
```
trabotto-domain/
├── pom.xml                          # minimal POM, no dependencies
└── src/main/java/com/trabotto/domain/
    ├── model/
    │   ├── Signal.java              # incoming signal from any source
    │   ├── RawSignal.java           # raw unparsed signal (string + metadata)
    │   ├── Rule.java                # single trading rule from YAML
    │   ├── RuleSet.java             # versioned collection of rules
    │   ├── RuleVerdict.java         # output of rule engine evaluation
    │   ├── Decision.java            # full decision (signal + rules + AI + policy)
    │   ├── TradeAction.java         # enum: ENTER_LONG, ENTER_SHORT, SKIP, AMBIGUOUS
    │   ├── OrderRequest.java        # what we send to the exchange
    │   ├── OrderResult.java         # what the exchange returns
    │   ├── Position.java            # an open position being monitored
    │   ├── TradeOutcome.java        # final result after position closes
    │   ├── AiAdvisory.java          # AI opinion (pre-entry)
    │   ├── TradeMonitorAdvice.java   # AI opinion (during trade monitoring)
    │   ├── MonitorAction.java       # enum: HOLD, MOVE_SL_TO_BREAKEVEN, TIGHTEN_TP, CLOSE_NOW
    │   ├── BtcSnapshot.java         # BTC index state for trade monitoring
    │   ├── Candle.java              # OHLCV candle
    │   ├── PolicyCheckResult.java   # result of policy engine check
    │   ├── ShadowTrade.java         # a skipped trade tracked for analysis
    │   └── ShadowTradeOutcome.java  # what would have happened
    ├── port/
    │   ├── SignalSourcePort.java     # interface for signal ingestion
    │   ├── ExchangePort.java        # interface for exchange operations
    │   └── AiAdvisoryPort.java      # interface for AI advisory (entry + monitoring)
    └── engine/
        ├── RuleEngine.java          # interface for rule evaluation
        ├── PolicyEngine.java        # interface for pre-execution policy checks
        └── DecisionAggregator.java  # interface combining rules + AI + policy
```

### Key Design from ARCHITECTURE.md

#### Signal (record)
```java
public record Signal(
    String id,
    Instant timestamp,
    String pair,           // e.g., "CELOUSDT"
    String timeframe,      // e.g., "5", "30", "1D"
    String strategy,       // e.g., "viagra", "bomba", "ath500"
    String exchange,       // e.g., "Bybit"
    boolean perfezione,    // BullBot quality flag
    String sourceId,       // e.g., "bullbot-telegram"
    String rawMessage      // original message for audit
) {}
```

#### RawSignal (record) — what the ingest module produces
```java
public record RawSignal(
    String sourceId,       // e.g., "bullbot-telegram"
    Instant receivedAt,
    String rawContent,     // the original message text
    Map<String, String> metadata  // source-specific metadata (channel name, message id, etc.)
) {}
```

#### ExchangePort — note the modifyStopLossTakeProfit method
```java
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
```

#### AiAdvisoryPort — two methods, entry and monitoring
```java
public interface AiAdvisoryPort {
    AiAdvisory evaluate(TradingContext context, AiConstraints constraints);
    TradeMonitorAdvice monitorPosition(OpenPositionContext positionContext,
                                       BtcSnapshot btcSnapshot,
                                       AiConstraints constraints);
}
```
Create TradingContext, AiConstraints, and OpenPositionContext as records too.

#### SignalSourcePort
```java
public interface SignalSourcePort {
    String sourceId();
    String associatedRuleSetId();
    void subscribe(Consumer<RawSignal> signalConsumer);
    boolean isAvailable();
}
```

### Parent POM
The parent `pom.xml` in the project root should define:
- groupId: `com.trabotto`
- Java 21
- `trabotto-domain` as the first module

Create both the parent POM and the domain module POM.

## Validation
After creating all files:
1. Run `mvn compile` from the project root
2. Ensure zero compilation errors
3. Ensure zero external dependencies in trabotto-domain

## Important
- Read ARCHITECTURE.md before starting
- Don't add anything not in these instructions
- Don't create test classes yet
- Every record should have a Javadoc comment explaining its role
