# Phase 1 — Core Engine

**Goal:** Domain model + Kafka pipeline + rule engine skeleton.
**Status:** IN PROGRESS
**Started:** 2026-03-25

## Tasks

### 1.1 Domain Model (trabotto-domain)
- [x] Generate via Codex (CLAUDE_CODE_DOMAIN_INSTRUCTIONS.md)
- [x] Signal, RawSignal, Rule, Decision records
- [x] SignalSourcePort, ExchangePort, AiAdvisoryPort interfaces
- [x] RuleEngine, PolicyEngine interfaces
- [x] TradeMonitorAdvice, MonitorAction, BtcSnapshot
- [x] ShadowTrade, ShadowTradeOutcome
- [x] mvn compile passes with zero dependencies

### 1.2 Parent POM + Module Structure
- [x] Parent POM with Java 21
- [x] trabotto-domain module
- [x] trabotto-infrastructure module (skeleton)
- [x] trabotto-application module (skeleton)
- [x] trabotto-boot module (skeleton)

### 1.3 Kafka Setup
- [x] Docker Compose: Kafka + Zookeeper
- [ ] Topic definitions (signals.incoming, decisions.pending, etc.)
- [ ] SignalConsumer skeleton in trabotto-infrastructure
- [ ] Basic producer/consumer integration test

### 1.4 PostgreSQL Setup
- [x] Docker Compose: PostgreSQL + pgvector
- [ ] Flyway or manual schema for decisions table
- [ ] Shadow trades table
- [ ] Basic repository skeleton

### 1.5 Rule Engine (v1)
- [ ] YAML rule loader (file watcher for hot-reload)
- [ ] Rule matcher: signal → matching rules → RuleVerdict
- [ ] Deterministic evaluation (same input → same output)
- [ ] Unit tests with sample Bull rules

## Definition of Done
- [x] `docker-compose up` starts Kafka + PostgreSQL
- [x] Domain model compiles clean
- [ ] Can publish a test signal to Kafka and consume it
- [ ] Rule engine evaluates sample rules against sample signals
