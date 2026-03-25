# Phase 5 — Live Trading

**Goal:** Real money, small size, controlled risk.
**Status:** NOT STARTED

## Prerequisites
- [ ] Phase 4 complete with positive validation
- [ ] Net positive win rate on 200+ testnet trades
- [ ] Shadow trade analysis confirms threshold calibration
- [ ] Budget defined: max amount willing to lose entirely

## Tasks

### 5.1 Go-Live Preparation
- [ ] Bybit live account + API keys (restricted permissions)
- [ ] Switch config: TESTNET → LIVE
- [ ] Start with minimal size (e.g., $200-500 total)
- [ ] Kill switch: manual emergency stop

### 5.2 Monitoring
- [ ] Real-time P&L dashboard
- [ ] Alert on: max daily loss, consecutive losses, system errors
- [ ] Weekly review of decisions vs shadow trades

### 5.3 Scaling
- [ ] Gradual size increase based on track record
- [ ] Add Binance adapter (if needed)
- [ ] Consider second signal source / ruleset

## Definition of Done
- [ ] System is profitable over 30+ days
- [ ] Or: clear data showing which rules/strategies to adjust
