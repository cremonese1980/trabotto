# Phase 3 — AI Advisory + Trade Monitor + RAG

**Goal:** AI integration for entry advisory and active trade management ("the eye").
**Status:** NOT STARTED

## Tasks

### 3.1 AI Advisory (Entry)
- [ ] Ollama adapter (dev/test, free)
- [ ] Anthropic adapter (production)
- [ ] OpenAI adapter (production)
- [ ] Prompt engineering for entry decision
- [ ] Cost controls: timeout, max tokens, budget cap
- [ ] Fallback to rule-only if AI unavailable

### 3.2 Trade Monitor ("The Eye")
- [ ] Adaptive polling scheduler (TF-based intervals)
- [ ] BTC index continuous monitoring
- [ ] AI monitor prompt: position context + BTC → action
- [ ] MonitorAction execution: HOLD, MOVE_SL_TO_BREAKEVEN, TIGHTEN_TP, CLOSE_NOW
- [ ] Deterministic guardrails (SL only tightens, TP min 1%)
- [ ] Fallback: hard SL/TP on exchange if AI fails
- [ ] Cost control for monitoring (cheap model, short prompts)

### 3.3 RAG — Decision Memory
- [ ] pgvector setup for decision embeddings
- [ ] Embed decisions (pair + strategy + conditions + outcome)
- [ ] Retrieve top-K similar past decisions on new signal
- [ ] Feed similar decisions to AI as context

### 3.4 Shadow Trade Tracking
- [ ] ShadowTradeService: open phantom position on rejected signals
- [ ] Shadow monitor: low-priority price checks (no AI)
- [ ] Outcome recording: would have won/lost
- [ ] Analytics queries for threshold calibration

## Definition of Done
- [ ] AI evaluates entry decisions with Ollama (free)
- [ ] Trade monitor actively manages positions with AI
- [ ] BTC index is always part of monitoring context
- [ ] Shadow trades are tracked for skipped signals
- [ ] RAG retrieves similar past decisions
