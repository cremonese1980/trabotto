# ARCHITECTURE.md — Updates to apply (March 28, 2026)

## Changes to apply to docs/ARCHITECTURE.md:

---

### 1. Add to Section 3.2 ExchangePort — add these methods:

```java
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
```

Note: Not needed for v1 (5 USDT minimum size), but architecture must support it.

---

### 2. Add new Section 6.1 — Watchlist/Incubator

After the Kafka Topics table, add:

```
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
```

---

### 3. Add new Section 5.1 — Consolidated Rules

After Section 5 (Rule Engine), add:

```
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
```

---

### 4. Update Section 10 — Module Structure

Replace the ingest/ section with:

```
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
```

Replace the infrastructure section with:

```
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
```

Add to application section:

```
├── trabotto-application/
│   └── src/main/java/
│       └── com/trabotto/application/
│           ├── engine/
│           │   └── DefaultRuleEngine.java       # strategy tag matching, confidence ✓
│           ├── SignalProcessingUseCase.java      # signal → rule engine → log verdict ✓
│           ├── DecisionExecutionUseCase.java     # (TODO)
│           ├── TradeMonitoringUseCase.java       # (TODO)
│           └── ShadowTradeUseCase.java           # (TODO)
```

---

### 5. Update Section 14 — Open Questions

Mark resolved:

```
- [x] BullBot signal format: catalog ALL strategy types and message patterns → DONE (25+ strategies cataloged from live Telegram data)
```

Add new:

```
- [ ] Liquidity gate: getOrderBookDepth() and get24hVolume() — when to implement?
- [ ] Watchlist re-evaluation: how often, what triggers promotion?
- [ ] Standard actions list: current list is DRAFT, needs expansion from live trading
- [ ] Rule language: replace tag-in-description hack with proper Tag field in domain Rule
- [ ] Evidence images: automated linking to rules via RULES_INDEX.md
```

---

### 6. Add new Section 16 — Environments

After Section 15 (Risk Mitigation), add:

```
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
```
