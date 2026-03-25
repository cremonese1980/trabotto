# Phase 4 — Paper Trading

**Goal:** 200+ trades on Bybit testnet to validate the system end-to-end.
**Status:** NOT STARTED

## Tasks

### 4.1 Bybit Testnet Integration
- [ ] Bybit testnet account + API keys
- [ ] BybitExchangeAdapter: open/close/modify positions
- [ ] Handle pairs not available on testnet

### 4.2 End-to-End Pipeline
- [ ] Signal → Rule Engine → Policy → Exchange → Monitor → Close
- [ ] Full audit trail for every decision
- [ ] Shadow trades running in parallel

### 4.3 Validation
- [ ] 200+ real trades on testnet
- [ ] Win rate analysis per strategy
- [ ] Win rate analysis per rule
- [ ] Shadow trade comparison: "what we skipped vs what happened"
- [ ] Confidence threshold calibration
- [ ] Policy parameter tuning (max positions, daily loss limit)

### 4.4 Reporting
- [ ] Daily P&L summary
- [ ] Per-strategy breakdown
- [ ] AI cost tracking
- [ ] Decision quality metrics

## Definition of Done
- [ ] 200+ trades completed on testnet
- [ ] Net positive win rate after commissions (simulated)
- [ ] Shadow trade data validates confidence thresholds
- [ ] System runs 24h+ without manual intervention
- [ ] Decision to proceed to live trading or iterate
