# Phase 0 — Knowledge Extraction

**Goal:** Transform Bull's video knowledge into structured, machine-readable YAML rules.
**Status:** IN PROGRESS
**Started:** 2026-03-25

## Tasks

### 0.1 Transcription Pipeline
- [x] Whisper transcription script (transcribe.py)
- [x] Confidence flagging for low-quality segments
- [ ] Transcribe playlist "Neofiti" (47 videos) — medium model
- [ ] Transcribe playlist "Tecniche" — medium model
- [ ] Review flagged segments (ongoing, low priority)

### 0.2 Rule Extraction
- [x] Define GPT extraction prompt (GPT_EXTRACTION_PROMPT_v1.md)
- [x] Define MACHINE_EXECUTABLE vs CONTEXT_ONLY distinction
- [ ] Process first 5 transcriptions via Codex (2 neofiti + 3 tecniche)
- [ ] Review with Claude → calibrate prompt
- [ ] Process remaining transcriptions
- [ ] Consolidate and deduplicate rules

### 0.3 Evidence Screenshots
- [ ] Download Bull trade screenshots from Discord
- [ ] Naming convention: BULL_TRADE_NNN_PAIR_DIRECTION_TF.png
- [ ] Link screenshots to extracted YAML rules

## Definition of Done
- [ ] All ~100 videos transcribed
- [ ] All transcriptions processed into YAML rules
- [ ] Rules classified as MACHINE_EXECUTABLE or CONTEXT_ONLY
- [ ] Flagged segments reviewed and corrected
- [ ] Complete catalog of Bull's strategies (viagra, bomba, sismografo, shimano, bud, etc.)
