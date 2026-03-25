# Phase 2 — Signal Ingestion

**Goal:** Telegram listener + signal normalizer + Kafka publishing.
**Status:** NOT STARTED

## Tasks

### 2.1 Telegram Listener (Production)
- [x] PoC: telegram_listener.py reads BullBot channel ✓
- [ ] Dockerize the listener
- [ ] Auto-reconnect on disconnect
- [ ] Health check endpoint

### 2.2 Signal Normalizer
- [ ] Catalog all message formats from accumulated raw messages
- [ ] Parse: exchange, pair, timeframe, strategy, diff_24h, perfezione
- [ ] Handle edge cases (cambio colore = daily, abbreviations like "bud")
- [ ] Unit tests for each known message format

### 2.3 Kafka Integration
- [ ] Publish normalized signals to signals.incoming topic
- [ ] Signal deduplication (same pair + strategy + time window)
- [ ] Dead letter queue for unparseable messages

### 2.4 BullWeb Fallback Adapter
- [ ] Cognito SRP authentication
- [ ] REST polling on /prod/api/v1/signal/filter
- [ ] Token refresh logic
- [ ] Same normalizer, different source

## Definition of Done
- [ ] Telegram listener runs in Docker, auto-reconnects
- [ ] Signals are normalized and published to Kafka
- [ ] At least 24h of signals captured and normalized without errors
- [ ] BullWeb adapter works as fallback (can switch via config)
