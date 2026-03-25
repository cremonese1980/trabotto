# Trabotto v2

**Rule-based crypto trading engine with AI-driven trade management.**

```
 ┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
 │   SIGNAL     │     │   DECISION   │     │   EXCHANGE   │     │    TRADE     │
 │   INGEST     │────▶│   PIPELINE   │────▶│  EXECUTION   │────▶│   MONITOR    │
 │              │     │              │     │              │     │  "The Eye"   │
 │ Telegram     │     │ Rule Engine  │     │ Bybit API    │     │              │
 │ BullWeb API  │     │ AI Advisory  │     │ (testnet +   │     │ AI actively  │
 │              │     │ Policy Check │     │  live)       │     │ manages SL/  │
 │              │     │ Shadow Trade │     │              │     │ TP + monitors│
 │              │     │              │     │              │     │ BTC index    │
 └──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
        │                    │                    │                     │
        ▼                    ▼                    ▼                     ▼
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                        DECISION MEMORY (PostgreSQL + pgvector)              │
 │          Every signal, decision, trade, and outcome is persisted.           │
 │                   RAG retrieval for similar past decisions.                 │
 └─────────────────────────────────────────────────────────────────────────────┘
```

## What It Does

Trabotto transforms unstructured trading knowledge into explicit, auditable rules —
then executes them with discipline no human can match.

1. **Captures signals** from a paid signal service (BullBot) via Telegram or REST API
2. **Evaluates signals** against a deterministic rule engine built from ~100 video transcriptions
3. **Asks AI for advisory** (optional for entry, mandatory for exit management)
4. **Enforces risk policy** (max positions, max daily loss, cooldown after loss)
5. **Executes trades** on Bybit (testnet or live)
6. **Actively manages open positions** — AI monitors price action + BTC index, dynamically adjusts SL/TP
7. **Tracks everything** — every decision is auditable, replayable, and feeds the RAG memory

Trades that get rejected are tracked as **shadow trades** — zero risk, maximum learning.

## Why It Exists

The trading strategies are proven. The problem is human discipline.
A bot that follows rules without emotion has a structural advantage over a human executing the same rules.

## Architecture

Hexagonal architecture (ports & adapters). The domain has zero external dependencies.
Signal sources, exchanges, and AI providers are all interchangeable behind ports.

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Core Engine | Java 21 + Spring Boot 3 | Decision pipeline, rule engine, policy engine |
| Signal Ingest | Python (Telethon) | Telegram listener, signal normalization |
| Messaging | Kafka | Event-driven pipeline between components |
| Database | PostgreSQL + pgvector | Decision memory, audit trail, RAG embeddings |
| AI (dev) | Ollama | Free, local AI for development and testing |
| AI (prod) | Claude / GPT | Production AI for entry advisory and trade monitoring |
| Exchange | Bybit v5 API | Order execution (testnet + live via config) |

## Knowledge Pipeline

```
Bull's YouTube videos (100+)
        │
        ▼
Whisper (local transcription)
        │
        ▼
GPT/Codex (rule extraction)
        │
        ▼
YAML rules (machine-executable + context-only)
        │
        ├──▶ Rule Engine (deterministic evaluation)
        └──▶ RAG (AI retrieval context)
```

## Trade Lifecycle

```
Signal received
    │
    ▼
Rule Engine evaluates → confidence score
    │
    ├── confidence >= threshold → ENTER TRADE
    │       │
    │       ▼
    │   Policy Engine checks (max positions, daily loss, etc.)
    │       │
    │       ▼
    │   Exchange: open position (hard SL/TP as safety net)
    │       │
    │       ▼
    │   Trade Monitor ("The Eye"):
    │       • Adaptive polling (TF 1min → every 5-30s)
    │       • AI evaluates position + BTC index
    │       • Moves SL to breakeven ASAP
    │       • Tightens TP based on conditions
    │       • Closes early if market turns
    │       │
    │       ▼
    │   Trade closed → outcome recorded → feeds RAG
    │
    └── confidence < threshold → SHADOW TRADE
            │
            ▼
        Track what would have happened (no money at risk)
            │
            ▼
        Outcome recorded → calibrates confidence thresholds
```

## Project Structure

```
trabotto/
├── ingest/                     # Python sidecar
│   ├── src/
│   │   ├── telegram_listener.py
│   │   ├── signal_normalizer.py
│   │   ├── kafka_producer.py
│   │   └── transcribe.py       # Whisper transcription pipeline
│   └── Dockerfile
│
├── trabotto-domain/            # Pure domain model (zero dependencies)
├── trabotto-infrastructure/    # Adapters (Bybit, Kafka, PostgreSQL, AI providers)
├── trabotto-application/       # Use cases and orchestration
├── trabotto-boot/              # Spring Boot entry point
│
├── knowledge/
│   ├── transcriptions/         # Whisper output (1 file per video)
│   ├── rules/                  # Extracted YAML rules
│   └── evidence/               # Trade screenshots from Bull
│
├── docs/
│   ├── ARCHITECTURE.md         # Full system design
│   ├── ROADMAP.md              # Phase-based development plan
│   └── phases/                 # Detailed task tracking per phase
│
└── docker-compose.yml          # Kafka + PostgreSQL + Ollama + ingest
```

## Current Status

| Phase | Description | Status |
|-------|------------|--------|
| 0 | Knowledge extraction (video → YAML rules) | 🟡 In Progress |
| 1 | Core engine (domain model, Kafka, rule engine) | 🟡 In Progress |
| 2 | Signal ingestion (Telegram + normalizer) | ⚪ Not Started |
| 3 | AI advisory + Trade Monitor + RAG | ⚪ Not Started |
| 4 | Paper trading (200+ trades on Bybit testnet) | ⚪ Not Started |
| 5 | Live trading (small size, post-validation) | ⚪ Not Started |

## Design Principles

- **Rules decide entry. AI manages exit.** Entry can be purely deterministic. Exit requires the AI "eye".
- **AI is untrusted input.** Every AI suggestion is validated by deterministic guardrails before execution.
- **Everything is auditable.** Signal → rules matched → AI advisory → policy check → execution → outcome.
- **RAG over fine-tuning.** Always. Fine-tuning only after labeled data from real trades.
- **Shadow trading for free learning.** Rejected signals are tracked to calibrate thresholds.
- **Ports & adapters.** Signal source, exchange, and AI provider are all swappable with zero core changes.

## License

Private project. Not open source.