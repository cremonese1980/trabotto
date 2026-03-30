# Phase 0 — Knowledge Extraction

**Goal:** Transform Bull's video knowledge into structured, machine-readable YAML rules.
**Status:** IN PROGRESS
**Started:** 2026-03-25

## Tasks

### 0.1 Transcription Pipeline
- [x] Whisper transcription script (transcribe.py)
- [x] Confidence flagging for low-quality segments
- [x] Transcribe playlist "Neofiti" (47 videos) — medium model
- [x] Transcribe playlist "Tecniche" — medium model
- [x] Transcribe playlist "Nuova Neofiti" — medium model
- [x] Transcribe playlist "live-trading-crypto-analisi" — medium model
- [x] Transcribe playlist "live-streaming-su-youtube" — medium model
- [x] Transcribe playlist "live-trade-con-analisi" — medium model
- [ ] Review flagged segments (ongoing, low priority)

### 0.2 Rule Extraction
- [x] Define GPT extraction prompt (GPT_EXTRACTION_PROMPT_v1.md)
- [x] Define MACHINE_EXECUTABLE vs CONTEXT_ONLY distinction
- [x] Process first 5 transcriptions via Codex (2 neofiti + 3 tecniche)
- [x] Review with Claude → calibrate prompt
- [x] Process remaining transcriptions
- [ ] Consolidate and deduplicate rules

### 0.3 Evidence Screenshots
- [x] Download Bull trade screenshots from Discord
- [x] Naming convention: BULL_TRADE_NNN_PAIR_DIRECTION_TF.png
- [ ] Link screenshots to extracted YAML rules

## Definition of Done
- [x] All ~200 videos transcribed
- [x] All transcriptions processed into YAML rules
- [x] Rules classified as MACHINE_EXECUTABLE or CONTEXT_ONLY
- [ ] Flagged segments reviewed and corrected
- [ ] Complete catalog of Bull's strategies (viagra, bomba, sismografo, shimano, bud, etc.)
