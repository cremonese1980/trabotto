# Trabotto v2 — System Architecture

## 1. Vision

Trabotto is a **rule-based decision engine with AI advisory** for crypto trading.  
It transforms unstructured trading knowledge (Bull's techniques) into explicit, auditable,
versionable rules — then executes them with discipline no human can match.

**Core principle:** the system does NOT predict the market.  
It applies a known, validated strategy with zero emotional bias.

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        SIGNAL SOURCES (Port)                        │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────────┐ │
│  │  Telegram    │  │  BullWeb API │  │  Future Source (any)       │ │
│  │  (primary)   │  │  (fallback)  │  │                            │ │
│  └──────┬───────┘  └──────┬───────┘  └─────────────┬──────────────┘ │
│         └─────────────────┼────────────────────────┘                │
│                           ▼                                         │
│                   SignalSourcePort                                   │
└───────────────────────────┬─────────────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     SIGNAL NORMALIZER                                │
│  Raw message → SignalEvent (pair, timeframe, strategy, exchange,    │
│                             perfezione, timestamp)                   │
└───────────────────────────┬─────────────────────────────────────────┘
                            ▼
                    Kafka: signals.incoming
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     DECISION PIPELINE                                │
│                                                                     │
│  ┌──────────────┐    ┌──────────────┐    ┌───────────────────────┐  │
│  │ Market Data  │    │ Rule Engine  │    │ AI Advisory (gated)   │  │
│  │ Enrichment   │───▶│ (YAML rules) │───▶│ Ollama / Claude /    │  │
│  │ (Bybit API)  │    │              │    │ OpenAI               │  │
│  └──────────────┘    └──────────────┘    └───────────────────────┘  │
│         │                    │                      │               │
│         ▼                    ▼                      ▼               │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    DECISION AGGREGATOR                       │    │
│  │  rules_result + ai_advisory + market_context → Decision     │    │
│  └─────────────────────────────┬───────────────────────────────┘    │
└────────────────────────────────┼────────────────────────────────────┘
                                 ▼
                    Kafka: decisions.pending
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     POLICY ENGINE                                    │
│  Pre-execution checks:                                              │
│  - max open positions                                               │
│  - max daily loss                                                   │
│  - pair exposure limit                                              │
│  - cooldown after loss                                              │
│  - confidence threshold                                             │
└───────────────────────────┬─────────────────────────────────────────┘
                            ▼
                    Kafka: orders.approved
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     EXCHANGE EXECUTION (Port)                       │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────────┐ │
│  │  Bybit       │  │  Binance     │  │  Future Exchange           │ │
│  │  (day one)   │  │  (later)     │  │                            │ │
│  └──────┬───────┘  └──────┬───────┘  └─────────────┬──────────────┘ │
│         └─────────────────┼────────────────────────┘                │
│                           ▼                                         │
│                   ExchangePort                                      │
└───────────────────────────┬─────────────────────────────────────────┘
                            ▼
                    Kafka: orders.executed
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  TRADE MONITOR ("The Eye") — AI REQUIRED            │
│                                                                     │
│  For each open position, adaptive polling loop:                     │
│  - TF 1min  → check every 5-30 seconds                             │
│  - TF 5min  → check every 1-3 minutes                              │
│  - TF 30min → check every 5-10 minutes                             │
│                                                                     │
│  On each tick, AI evaluates:                                        │
│  - current price vs entry                                           │
│  - BTC index behavior (always monitored)                            │
│  - price action since entry                                         │
│  - current SL/TP levels                                             │
│                                                                     │
│  AI can decide:                                                     │
│  - HOLD (do nothing)                                                │
│  - MOVE_SL_TO_BREAKEVEN (SL → entry price)                         │
│  - TIGHTEN_TP (e.g., settle for 1.5% instead of 3%)                │
│  - CLOSE_NOW (market conditions changed)                            │
│                                                                     │
│  Fallback if AI unavailable: hard SL/TP on exchange (safety net)    │
└───────────────────────────┬─────────────────────────────────────────┘
                            ▼
                    Kafka: trades.closed (when position ends)
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     DECISION MEMORY                                 │
│  Every decision is persisted:                                       │
│  - signal received                                                  │
│  - rules matched                                                    │
│  - AI advisory (if invoked)                                         │
│  - policy checks passed/failed                                      │
│  - order executed (or rejected + reason)                            │
│  - outcome (P&L, duration, fees)                                    │
│                                                                     │
│  PostgreSQL + vector embeddings for RAG retrieval                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Ports & Adapters (Hexagonal Architecture)

### 3.1 Signal Source Port

```java
public interface SignalSourcePort {

    /**
     * Unique identifier for this source (e.g., "bullbot-telegram", "bullbot-web").
     */
    String sourceId();

    /**
     * The RuleSet ID associated with this signal source.
     * When source changes, the ruleset changes with it.
     */
    String associatedRuleSetId();

    /**
     * Start listening for signals. Implementations push to the provided consumer.
     */
    void subscribe(Consumer<RawSignal> signalConsumer);

    /**
     * Health check.
     */
    boolean isAvailable();
}
```

**Adapters:**
- `TelegramSignalAdapter` — Telegram Client API (TDLib), listens to BullBot channel
- `BullWebSignalAdapter` — REST polling on `/prod/api/v1/signal/filter` with Cognito SRP auth
- Future: any source that can emit a `RawSignal`

### 3.2 Exchange Port

```java
public interface ExchangePort {

    String exchangeId();

    /**
     * Fetch last N candles for a pair/timeframe.
     */
    List<Candle> getCandles(String pair, String timeframe, int count);

    /**
     * Get current price.
     */
    BigDecimal getCurrentPrice(String pair);

    /**
     * Open a position.
     */
    OrderResult openPosition(OrderRequest request);

    /**
     * Close a position.
     */
    OrderResult closePosition(String positionId);

    /**
     * Modify SL/TP on an open position.
     * Critical for dynamic trade management (move SL to breakeven, adjust TP).
     */
    OrderResult modifyStopLossTakeProfit(String positionId, BigDecimal newSL, BigDecimal newTP);

    /**
     * Get open positions.
     */
    List<Position> getOpenPositions();

    /**
     * Check if pair is tradeable.
     */
    boolean isPairAvailable(String pair);

    /**
     * Get order book depth for liquidity assessment.
     * Used by LIQUIDITY_GATE rules before entry.
     */
    OrderBookSnapshot getOrderBookDepth(String pair, int depth);

    /**
     * Get 24h trading volume for a pair.
     * Low volume pairs risk unfilled orders.
     */
    BigDecimal get24hVolume(String pair);

    /**
     * Testnet vs live is a config concern, not a code concern.
     */
    boolean isTestnet();
}
```

**Adapters:**
- `BybitExchangeAdapter` — Bybit v5 API (testnet + live via config)
- `BinanceExchangeAdapter` — (future)

### 3.3 AI Advisory Port

```java
public interface AiAdvisoryPort {

    /**
     * Ask AI for an opinion on a trading setup (pre-entry).
     * AI output is untrusted — always validated deterministically.
     * For entry: optional (rules can decide alone).
     * For exit/monitoring: REQUIRED (replaces the human "eye").
     */
    AiAdvisory evaluate(TradingContext context, AiConstraints constraints);

    /**
     * AI evaluates an open position and recommends action.
     * This is the "eye" — the active trade manager.
     * Called on every monitoring tick (adaptive frequency).
     */
    TradeMonitorAdvice monitorPosition(OpenPositionContext positionContext,
                                        MarketSnapshot btcSnapshot,
                                        AiConstraints constraints);
}
```

**Constraints enforce:**
- max timeout (e.g., 3 seconds for monitoring, 5 seconds for entry)
- max cost per call (monitoring calls are frequent — cost matters)
- fallback to hard SL/TP if AI unavailable (safety net)

**Adapters:**
- `OllamaAdvisoryAdapter` — local, free, for dev/test
- `AnthropicAdvisoryAdapter` — Claude API, for production
- `OpenAiAdvisoryAdapter` — GPT API, for production
- `NoOpAdvisoryAdapter` — entry only: skip AI advisory (pure rule-based)
    - WARNING: NoOp is NOT valid for trade monitoring — hard SL/TP fallback activates instead

---

## 4. Domain Model

### 4.1 Signal

```java
public record Signal(
    String id,
    Instant timestamp,
    String pair,           // e.g., "CELOUSDT"
    String timeframe,      // e.g., "5" (minutes), "1D"
    String strategy,       // e.g., "viagra", "bomba", "ath500"
    String exchange,       // e.g., "Bybit"
    boolean perfezione,    // BullBot quality flag
    String sourceId,       // e.g., "bullbot-telegram"
    String rawMessage      // original message for audit
) {}
```

### 4.2 Rule

```yaml
# Example rule (YAML — Single Source of Truth)
id: BULL_BOMBA_001
version: 1
type: reversal
name: "Bomba short on sismografo"
source: "Bull course video #14"
conditions:
  - strategy_match: "bomba"
  - sub_type: "sismografo"
  - diff_24h_below: -3.0
  - perfezione: true
  - timeframe_in: ["1", "5"]
action:
  direction: SHORT
  entry: market
  stop_loss_pct: 1.5
  take_profit_pct: 3.0
  position_size_pct: 2.0    # % of available balance
confidence: high
tags: ["sismografo", "bomba", "short"]
warnings:
  - "Never enter if BTC is pumping hard"
evidence_images:
  - "BULL_TRADE_042_RDNTUSDT_SHORT_1min.png"
```

### 4.3 Decision

```java
public record Decision(
    String id,
    Instant timestamp,
    Signal signal,
    List<String> matchedRuleIds,
    RuleVerdict ruleVerdict,      // ENTER_LONG, ENTER_SHORT, SKIP, AMBIGUOUS
    AiAdvisory aiAdvisory,        // nullable
    PolicyCheckResult policyCheck,
    FinalAction finalAction,      // EXECUTE, REJECT
    String rejectionReason,       // if rejected
    // filled after execution
    OrderResult orderResult,
    // filled after close
    TradeOutcome outcome          // P&L, fees, duration
) {}
```

---

## 5. Rule Engine

The rule engine is **deterministic** — same input always produces same output.

```
Signal + MarketData → RuleEngine.evaluate(signal, marketData, ruleSet)
                           │
                           ├─ filter rules by strategy match
                           ├─ evaluate conditions against market data
                           ├─ score matched rules
                           │
                           ▼
                    RuleVerdict {
                        action: ENTER_SHORT,
                        confidence: 0.85,
                        matchedRules: ["BULL_BOMBA_001"],
                        reasoning: ["bomba + sismografo + diff -5.31% + perfezione"]
                    }
```

**Key design decisions:**
- Rules are loaded from YAML at startup and on hot-reload (file watcher)
- Rules are versioned: `ruleSetId: bull-v1.3`
- Every evaluation is logged with the exact rule version used
- **V1: pure pattern matching** — no ML in the rule engine
- **V2+ (future):** once we have 500+ decisions logged (real + shadow trades),
  ML can be introduced to: calibrate confidence scores, weight rules by historical
  win rate, optimize thresholds. But ML requires data first — and data comes from V1.


### 5.1 Consolidated Rules (SSOT)

Rules are stored in `knowledge/rules/consolidated-rules.yml` — the Single Source of Truth.

- 743 MACHINE_EXECUTABLE rules, deduplicated from ~200 Bull videos across 6 playlists
- 470 CONTEXT_ONLY entities in `consolidated-context.yml` (for RAG)
- Index in `RULES_INDEX.md` with statistics and image linking

**Rule structure:**
- id, entity_type (SETUP/RULE/INVALIDATION), category, scope, severity
- tags (for strategy matching), conditions (pseudo-code), action (standard list)
- stop_loss, take_profit (when specified), applicability, rationale, notes
- source_files (traceability to original video)

**Standard actions (DRAFT — will expand):**
ENTER_LONG, ENTER_SHORT, EXIT_POSITION, SKIP_TRADE, MOVE_SL_TO_BREAKEVEN,
REDUCE_SIZE, TIGHTEN_TP, MONITOR_LEVEL, CLASSIFY_SETUP, WAIT

**Rule Engine v1 does NOT filter by timeframe.** Bull's conformations are identical 
across all TFs — only SL/TP magnitude and monitoring frequency scale with TF. 
Exception: a few rules are explicitly daily-only (cambio_colore, bicicletta).

**Perfezione** (chart cleanliness) is a first-class quality signal reflecting 
Bull's "chi comanda" principle: clean EMAs = someone controlling with money = predictable.

---

## 6. Kafka Topics

| Topic                | Key              | Purpose                              |
|----------------------|------------------|--------------------------------------|
| `signals.incoming`   | pair             | Normalized signals from any source   |
| `decisions.pending`  | signal.id        | Decision engine output               |
| `orders.approved`    | decision.id      | Policy-approved orders               |
| `orders.executed`    | order.id         | Execution confirmations              |
| `orders.rejected`    | decision.id      | Policy-rejected decisions (audit)    |
| `shadow.opened`      | signal.id        | Shadow position opened (skipped trade)|
| `shadow.closed`      | shadow.id        | Shadow position outcome resolved     |
| `monitor.tick`       | position.id      | Each AI monitoring evaluation        |
| `monitor.action`     | position.id      | SL/TP modifications, early closes    |
| `trades.closed`      | trade.id         | Closed trades with P&L               |

**Idempotency:** every message has a deterministic key.  
Duplicate signals (same pair + strategy + timestamp window) are deduplicated.

### 6.1 Watchlist / Incubator

Not all signals warrant immediate action. Some show developing conformations 
that may become tradeable after N more candles.

| Topic                  | Key    | Purpose                                    |
|------------------------|--------|--------------------------------------------|
| `signals.watchlist`    | pair   | Signals with verdict WAIT or MONITOR_LEVEL |

**Flow:**
1. Signal arrives → rule engine evaluates → verdict = WAIT or MONITOR_LEVEL
2. Signal published to `signals.watchlist`
3. WatchlistMonitor re-evaluates periodically (configurable per TF)
4. If conditions improve → promote to `decisions.pending`
5. If expired (setup invalidated, too much time) → discard

**Status:** Topic defined. Re-evaluation logic is TODO.

---

## 7. Shadow Trading — Tracking Skipped Trades

### Purpose
Every signal that gets rejected (low confidence, policy block, etc.) is still valuable.
We open a **shadow position** — no money, no exchange order — and track what would have
happened if we had entered.

### Why This Is Critical
- Calibrates confidence thresholds: "we skipped 80 trades that would have won → threshold too high"
- Validates rules: "rule BULL_BOMBA_003 rejected 50 trades, 40 would have lost → rule is good"
- Builds the dataset needed for future ML calibration
- Zero risk, maximum learning

### How It Works

```
Signal → Decision Pipeline → REJECTED (confidence 0.45 < threshold 0.6)
    │
    ▼
ShadowTradeService.open(signal, rejectionReason, marketSnapshot)
    │
    ├─ Record: entry price, theoretical SL, theoretical TP
    ├─ Record: BTC price at signal time
    ├─ Track: did price hit TP or SL first?
    ├─ Track: how long would it have taken?
    │
    ▼
ShadowTradeOutcome {
    signalId, pair, strategy, timeframe,
    theoreticalEntry, theoreticalSL, theoreticalTP,
    wouldHaveWon: true/false,
    wouldHavePnlPct: +2.1%,
    timeToOutcome: 14min,
    rejectionReason: "confidence_below_threshold",
    matchedRules: ["BULL_BOMBA_003"],
    confidence: 0.45
}
```

### Shadow Monitor
Shadow positions are monitored with LOWER priority than real positions:
- Polling interval: 2x the normal interval for the timeframe
- No AI calls — just price checks against theoretical SL/TP
- Runs on a separate thread pool (never competes with real trade monitoring)

### Schema

```sql
CREATE TABLE shadow_trades (
    id              UUID PRIMARY KEY,
    signal_id       VARCHAR NOT NULL,
    pair            VARCHAR NOT NULL,
    timeframe       VARCHAR NOT NULL,
    strategy        VARCHAR NOT NULL,
    rejection_reason VARCHAR NOT NULL,
    confidence      DECIMAL NOT NULL,
    matched_rules   JSONB NOT NULL,
    entry_price     DECIMAL NOT NULL,
    theoretical_sl  DECIMAL NOT NULL,
    theoretical_tp  DECIMAL NOT NULL,
    btc_price_at_signal DECIMAL NOT NULL,
    outcome         VARCHAR,              -- WON, LOST, EXPIRED, null if still open
    outcome_price   DECIMAL,
    outcome_pnl_pct DECIMAL,
    time_to_outcome INTERVAL,
    created_at      TIMESTAMPTZ NOT NULL,
    closed_at       TIMESTAMPTZ
);

CREATE INDEX idx_shadow_strategy ON shadow_trades(strategy, outcome);
CREATE INDEX idx_shadow_confidence ON shadow_trades(confidence, outcome);
```

### Analytics Queries (examples)
```sql
-- Win rate of skipped trades by strategy
SELECT strategy, outcome, COUNT(*), AVG(outcome_pnl_pct)
FROM shadow_trades GROUP BY strategy, outcome;

-- Should we lower the confidence threshold?
SELECT confidence, outcome, COUNT(*)
FROM shadow_trades
WHERE confidence BETWEEN 0.4 AND 0.6
GROUP BY confidence, outcome;
```

---

## 7. RAG — Decision Memory

### Purpose
Retrieve past decisions similar to the current setup for:
- pattern analysis ("last 10 times we saw bomba + sismografo on RDNTUSDT")
- win rate per rule
- confidence calibration

### Implementation
- PostgreSQL with `pgvector` extension
- Each decision is embedded (pair + strategy + conditions + outcome)
- On new signal: retrieve top-K similar past decisions
- Feed to AI Advisory as context (if AI is enabled)

### Schema (simplified)

```sql
CREATE TABLE decisions (
    id              UUID PRIMARY KEY,
    timestamp       TIMESTAMPTZ NOT NULL,
    signal_id       VARCHAR NOT NULL,
    pair            VARCHAR NOT NULL,
    timeframe       VARCHAR NOT NULL,
    strategy        VARCHAR NOT NULL,
    matched_rules   JSONB NOT NULL,
    rule_verdict    VARCHAR NOT NULL,
    ai_advisory     JSONB,
    policy_result   JSONB NOT NULL,
    final_action    VARCHAR NOT NULL,
    rejection_reason VARCHAR,
    order_result    JSONB,
    trade_outcome   JSONB,
    ruleset_version VARCHAR NOT NULL,
    embedding       vector(384)
);

CREATE INDEX idx_decisions_pair_strategy ON decisions(pair, strategy);
CREATE INDEX idx_decisions_embedding ON decisions USING ivfflat (embedding vector_cosine_ops);
```

---

## 8. Policy Engine

Pre-execution safety net. **No trade passes without policy approval.**

```yaml
# policy.yaml — versioned independently from rules
version: policy-v1.0
max_open_positions: 3
max_daily_loss_usdt: 200
max_single_position_pct: 5.0      # % of balance
min_confidence: 0.6
cooldown_after_loss_minutes: 30
blocked_pairs: []                  # emergency block list
trading_hours:                     # optional
  start: "08:00"
  end: "22:00"
  timezone: "Europe/Madrid"
mode: TESTNET                      # TESTNET | LIVE | PAPER
```

---

## 9. Trade Monitor — "The Eye"

This is the most critical AI component. It replaces Bull's human eye on open positions.

### 9.1 Why AI is Required Here

Bull doesn't set static SL/TP and walk away. He actively watches:
- the pair's price action
- BTC as a market index (if BTC dumps, everything dumps)
- whether the setup is still valid
- whether to move SL to breakeven
- whether to settle for less TP or let it run

A static SL/TP bot would lose money. **The edge is in the management, not the entry.**

### 9.2 Adaptive Polling

```java
public record MonitoringConfig(
    Duration pollInterval,       // adaptive based on timeframe
    Duration maxPositionAge,     // safety: force close after X hours
    int maxAiFailures            // switch to hard SL/TP after N consecutive AI failures
) {
    public static MonitoringConfig forTimeframe(String timeframe) {
        return switch (timeframe) {
            case "1"  -> new MonitoringConfig(Duration.ofSeconds(15), Duration.ofHours(1), 3);
            case "5"  -> new MonitoringConfig(Duration.ofMinutes(2), Duration.ofHours(4), 3);
            case "15" -> new MonitoringConfig(Duration.ofMinutes(5), Duration.ofHours(8), 3);
            case "30" -> new MonitoringConfig(Duration.ofMinutes(8), Duration.ofHours(12), 3);
            default   -> new MonitoringConfig(Duration.ofMinutes(5), Duration.ofHours(8), 3);
        };
    }
}
```

### 9.3 Monitor Loop (per position)

```
For each open position:
  1. Fetch current price (pair)
  2. Fetch BTC current price + last 5 candles
  3. Fetch pair's candles since entry
  4. Build OpenPositionContext:
     - entry price, current price, unrealized P&L %
     - current SL, current TP
     - time in trade
     - BTC behavior (up/down/flat since entry)
  5. Call AiAdvisoryPort.monitorPosition(context, btcSnapshot, constraints)
  6. AI returns TradeMonitorAdvice:
     - HOLD → do nothing, check again next tick
     - MOVE_SL_TO_BREAKEVEN → ExchangePort.modifyStopLossTakeProfit(pos, entryPrice, currentTP)
     - TIGHTEN_TP → ExchangePort.modifyStopLossTakeProfit(pos, currentSL, newTP)
     - CLOSE_NOW → ExchangePort.closePosition(positionId)
  7. Log everything to monitor.tick topic
  8. If action taken → log to monitor.action topic
  9. Wait pollInterval, repeat
```

### 9.4 BTC Index Monitoring

BTC is monitored continuously, independent of open positions.
When AI evaluates a position, it always receives BTC context:

```java
public record BtcSnapshot(
    BigDecimal currentPrice,
    BigDecimal priceAtTradeEntry,    // BTC price when we opened the position
    BigDecimal changeSinceEntry,     // % change
    String trend,                    // UP, DOWN, FLAT (derived from last 5 candles)
    List<Candle> recentCandles       // last 5 BTC candles at the position's timeframe
) {}
```

If BTC dumps hard (e.g., -2% in minutes), AI should consider closing longs or tightening SL
regardless of the individual pair's behavior.

### 9.5 SL/TP Management Rules (from Bull)

These are deterministic guardrails that the AI must respect:

```yaml
# sl_tp_management.yaml
initial_stop_loss_max_pct: 1.0          # hard max, never wider
initial_take_profit_min_pct: 2.0        # aim high initially
breakeven_trigger_pct: 0.5              # if +0.5%, consider moving SL to 0
breakeven_sl_offset_pct: 0.05           # SL slightly above entry (cover fees)
min_acceptable_tp_pct: 1.0              # never settle for less than 1%
max_position_duration_hours: 4          # for TF 1-5min trades
```

AI can suggest actions, but the validator enforces:
- SL can only move UP (for longs) or DOWN (for shorts) — never widen
- TP can be tightened but never below `min_acceptable_tp_pct`
- Position must close after `max_position_duration_hours` (safety)

### 9.6 Fallback: AI Unavailable

If AI fails to respond within timeout, or fails N consecutive times:
- Hard SL/TP on exchange remain active (placed at order creation)
- Monitor switches to "degraded mode": only checks if exchange-side SL/TP triggered
- Alert is logged
- NO manual intervention required — the trade is always protected

### 9.7 Cost Control for Monitoring

Monitoring calls AI frequently. Cost must be controlled:
- Use Ollama (free) for dev/test
- In production: use cheapest model that's "good enough" (e.g., Claude Haiku, GPT-4o-mini)
- Reserve expensive models (Opus, GPT-4) for entry decisions only
- Monitoring prompt should be SHORT: position context + BTC snapshot + "what action?"
- Estimated cost per monitored trade (TF 5min, 2h duration):
  ~60 AI calls × ~500 tokens each = ~30K tokens ≈ $0.01-0.05 depending on model

---

## 10. Module Structure (Maven Multi-Module)

```
trabotto/
├── pom.xml                              # parent POM (Java modules only)
│
├── ingest/                              # Python sidecar (NOT a Maven module)
│   ├── pyproject.toml
│   ├── requirements.txt
│   ├── Dockerfile
│   ├── src/
│   │   ├── telegram_listener.py         # Telethon Client API → BullBot channel
│   │   ├── signal_normalizer.py         # parse raw message → structured signal dict
│   │   ├── kafka_producer.py            # publish to signals.incoming
│   │   └── transcribe.py               # Whisper transcription utility
│   │   # TODO: bullweb_poller.py — BullWeb REST API fallback
│   └── tests/
│       ├── test_signal_normalizer.py    # full coverage, all strategies
│       └── test_telegram_listener.py    # smoke tests
│
├── trabotto-domain/                     # pure domain model, zero dependencies
│   └── src/main/java/
│       └── com/trabotto/domain/
│           ├── model/                   # Signal, Decision, Rule, Order, Position, etc.
│           ├── port/                    # SignalSourcePort, ExchangePort, AiAdvisoryPort
│           └── engine/                  # RuleEngine, PolicyEngine, DecisionAggregator
│
├── trabotto-infrastructure/
│   └── src/main/java/
│       └── com/trabotto/infrastructure/
│           ├── exchange/
│           │   └── bybit/              # BybitExchangeAdapter (TODO)
│           ├── ai/
│           │   ├── ollama/             # OllamaAdvisoryAdapter (TODO)
│           │   ├── anthropic/          # AnthropicAdvisoryAdapter (TODO)
│           │   └── openai/             # OpenAiAdvisoryAdapter (TODO)
│           ├── persistence/
│           │   └── postgresql/         # Flyway migrations V001-V003 ✓
│           ├── rules/
│           │   ├── YamlRuleLoader.java          # loads consolidated-rules.yml ✓
│           │   ├── ConsolidatedRule.java         # YAML deserialization DTO ✓
│           │   └── ConsolidatedRulesYaml.java   # wrapper for top-level YAML ✓
│           ├── kafka/
│           │   ├── config/
│           │   │   ├── KafkaConfig.java         # topic definitions ✓
│           │   │   └── ObjectMapperConfig.java   # Jackson config ✓
│           │   ├── producer/
│           │   │   └── SignalProducer.java       # publishes to signals.incoming ✓
│           │   └── consumer/
│           │       └── SignalConsumer.java       # receives signals, calls use case ✓
│
├── trabotto-application/
│   └── src/main/java/
│       └── com/trabotto/application/
│           ├── engine/
│           │   └── DefaultRuleEngine.java       # strategy tag matching, confidence ✓
│           ├── SignalProcessingUseCase.java      # signal → rule engine → log verdict ✓
│           ├── DecisionExecutionUseCase.java     # (TODO)
│           ├── TradeMonitoringUseCase.java       # (TODO)
│           └── ShadowTradeUseCase.java           # (TODO)
│
├── trabotto-boot/                       # Spring Boot entry point, config
│   └── src/main/java/
│       └── com/trabotto/boot/
│           ├── TrabottoApplication.java
│           └── config/
│
├── docker-compose.yml                   # orchestrates: Java app + Python ingest
│                                        #   + Kafka + PostgreSQL + Ollama
├── docs/
│   ├── ARCHITECTURE.md                  # this file
│   └── TODO.md
│
└── knowledge/
    ├── transcriptions/                  # Bull video transcriptions (1 file per video)
    ├── rules/                           # extracted YAML rules (output of GPT extraction)
    └── evidence/                        # Bull trade screenshots (named: BULL_TRADE_NNN_*.png)
```

**Note:** the `ingest/` folder is a standalone Python application, not a Maven module.
It communicates with the Java core exclusively through Kafka (`signals.incoming` topic).
This clean boundary means:
- Python sidecar can be developed and tested independently
- If Telegram source dies, only `ingest/` is affected
- Java core never knows or cares that signals come from Python

---

## 11. Technology Stack

| Component            | Technology                    | Rationale                        |
|----------------------|-------------------------------|----------------------------------|
| Language             | Java 21                       | known stack, mature ecosystem    |
| Framework            | Spring Boot 3                 | known stack, production-grade    |
| Build                | Maven                         | known stack                      |
| Messaging            | Kafka                         | event-driven, replay, scaling    |
| Database             | PostgreSQL + pgvector          | decision memory + RAG            |
| AI (dev)             | Ollama                        | free, local, no API costs        |
| AI (prod)            | Claude / GPT                  | best models, gated usage         |
| Exchange             | Bybit v5 API                  | Bull's primary exchange          |
| Telegram             | Telethon (Python sidecar)     | signal ingestion, pragmatism     |
| Containers           | Docker + Compose              | local dev environment            |
| Testing              | JUnit 5 + Testcontainers      | integration tests with real deps |

---

## 12. Signal Flow (End-to-End)

```
1. Bull's bot detects a "conformazione" on the market
2. BullBot sends signal to Telegram channel + BullWeb
3. TelegramSignalAdapter receives message in real-time
4. SignalNormalizer parses: "Bybit - CELOUSDT 5MIN viagra +5.04% PERFEZIONE"
   → Signal(pair=CELOUSDT, tf=5, strategy=viagra, perfezione=true)
5. Signal published to Kafka: signals.incoming
6. SignalProcessingUseCase consumes the signal:
   a. BybitExchangeAdapter.getCandles(CELOUSDT, 5m, 50)
   b. RuleEngine.evaluate(signal, candles, bullRuleSet)
      → RuleVerdict(ENTER_LONG, confidence=0.78, rules=[BULL_VIAGRA_003])
   c. (optional) RAG retrieves 5 similar past decisions
   d. (optional) AiAdvisory.evaluate(signal + candles + similar_decisions)
      → "Consistent with historical pattern, 68% win rate on similar setups"
   e. DecisionAggregator produces Decision
7. Decision published to Kafka: decisions.pending
8. PolicyEngine checks:
   ✓ open positions < 3
   ✓ daily loss < 200 USDT
   ✓ confidence 0.78 > threshold 0.6
   ✓ no cooldown active
   → APPROVED
9. Order published to Kafka: orders.approved

   ALTERNATIVE PATH (if rejected at step 8):
   8b. PolicyEngine rejects (e.g., confidence 0.45 < threshold 0.6)
   8c. ShadowTradeUseCase.open(signal, rejectionReason, currentPrice, btcPrice)
   8d. Shadow monitor tracks: did price hit theoretical TP or SL?
   8e. Shadow outcome logged → invaluable data for threshold calibration

10. BybitExchangeAdapter.openPosition(LONG, CELOUSDT, size, SL, TP)
    - Hard SL/TP placed on exchange immediately (safety net)
11. Execution confirmed → Kafka: orders.executed
12. Trade Monitor activates for this position:
    a. TF=5min → poll every 2 minutes
    b. Each tick: fetch CELOUSDT price + BTC price + candles
    c. AI evaluates: "Position +0.6%, BTC stable → MOVE_SL_TO_BREAKEVEN"
    d. ExchangePort.modifyStopLossTakeProfit(pos, entryPrice + 0.05%, currentTP)
    e. Next tick: "Position +1.8%, BTC weakening → TIGHTEN_TP to 2%"
    f. Position hits TP at 2% → exchange closes automatically
13. Trade closed → Kafka: trades.closed
14. Decision memory updated with full lifecycle:
    - entry reason, rules matched, AI advisory
    - every monitor tick logged (SL/TP modifications, AI reasoning)
    - outcome: P&L, fees, duration, number of SL adjustments
15. All of the above is logged, auditable, replayable
```

---

## 13. What We Reuse from TriageMate

| TriageMate Component        | Trabotto Equivalent               |
|-----------------------------|-----------------------------------|
| Event ingestion pipeline    | Signal ingestion pipeline         |
| Kafka producer/consumer     | Same patterns, different topics   |
| Decision engine             | Rule engine (adapted for trading) |
| Policy engine               | Policy engine (trading policies)  |
| Decision memory + audit     | Trade decision memory + audit     |
| Idempotency patterns        | Signal deduplication              |
| Hexagonal architecture      | Same architecture                 |
| Transactional outbox        | Same pattern for reliability      |

---

## 14. Open Questions

- [x] ~~Telegram channel: confirm it stays active post-April 2025~~ → CONFIRMED active
- [x] ~~TDLib vs Telethon~~ → **Telethon (Python sidecar)** — pragmatism wins
- [x] BullBot signal format: catalog ALL strategy types and message patterns → DONE (25+ strategies cataloged from live Telegram data)
- [ ] Bybit API: testnet account setup + API keys
- [ ] Ollama model selection for advisory (llama3, mistral?)
- [ ] Ollama model selection for monitoring (must be fast + cheap)
- [ ] Position sizing strategy (fixed USDT vs % of balance)
- [ ] How to handle signals for pairs not available on Bybit testnet
- [ ] Trade Monitor: exact polling intervals per TF (needs real-world tuning)
- [ ] BTC index: which TF candles to fetch for BTC context?
- [ ] Liquidity gate: getOrderBookDepth() and get24hVolume() — when to implement?
- [ ] Watchlist re-evaluation: how often, what triggers promotion?
- [ ] Standard actions list: current list is DRAFT, needs expansion from live trading
- [ ] Rule language: replace tag-in-description hack with proper Tag field in domain Rule
- [ ] Evidence images: automated linking to rules via RULES_INDEX.md

---

## 15. Risk Mitigation

| Risk                              | Mitigation                                    |
|-----------------------------------|-----------------------------------------------|
| Bull's rules don't work automated | 200+ paper trades before live                 |
| BullBot changes signal format     | Normalizer with fallback + alert              |
| Telegram channel closed           | BullWeb API adapter ready                     |
| AI hallucination on entry         | AI is advisory only for entry, rules decide   |
| AI hallucination on monitoring    | Deterministic guardrails: SL only tightens    |
| AI unavailable during trade       | Hard SL/TP always on exchange as safety net   |
| Monitoring AI cost explosion      | Use cheap models for monitoring, budget cap   |
| Exchange API downtime             | Circuit breaker, no partial execution         |
| BTC flash crash                   | BTC index monitoring triggers emergency close |
| Overfitting rules to past data    | Out-of-sample validation period               |
| Slippage on illiquid pairs        | Liquidity filter in policy engine             |
| Cascading losses                  | Max daily loss + cooldown + kill switch        |

---

## 16. Environments

| Environment | AI Provider    | Exchange          | Rules        | Purpose                    |
|-------------|----------------|-------------------|--------------|----------------------------|
| Dev         | Ollama (local) | Bybit testnet     | Full ruleset | Development, unit tests    |
| Staging     | Claude/GPT     | Bybit testnet     | Full ruleset | Integration, paper trading |
| Prod        | Claude/GPT     | Bybit live        | Full ruleset | Live trading (5 USDT min)  |

All environments use the same codebase. Differences are config-only:
- `bybit.base-url`: testnet.bybit.com vs api.bybit.com
- `trabotto.ai.provider`: ollama / anthropic / openai
- `trabotto.mode`: DEV / STAGING / PROD
