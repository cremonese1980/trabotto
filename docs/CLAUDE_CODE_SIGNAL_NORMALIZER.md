# Claude Code Task — Signal Normalizer

## Context
Read docs/ARCHITECTURE.md for system context.
This is the Telegram signal normalizer for Trabotto. It parses raw BullBot messages
from the Telegram channel into structured Signal objects.

## Input
Raw messages look like this (from the Telegram listener):
```
[Bybit - CFXUSDT    30MIN  viagra sotto ema 10. differenza 24h:  -5.04% PERFEZIONE]
[Bybit - 1000BONKUS 30MIN  viagra sotto ema 10. differenza 24h:  -5.38% PERFEZIONE]
[Bybit - SXPUSDT    5MIN Rottura ema 223 tf 5 differenza 24h:  +7.82%]
[Bybit - OPNUSDT    sta per fare il cambio colore PERFEZIONE]
[Bybit - RENDERUSDT 1GIORNO differenza 3% su minimo di ieri. Differenza candela giorno precedente: -5.0% PERFEZIONE]
[Binance - BFUSDUSDC  è quasi in ATH di 500 giorni 0.04% dal cambio candela]
[Bybit - PIPPINUSDT 1MIN emersione incrocio 60/223. 24h: -27.19% PERFEZIONE]
[Bybit - TAUSDT     1MIN emersione incrocio 60/223. 24h: -9.89%]
[Bybit - AKTUSDT    5MIN bud. 24h: +11.69%]
[Bybit - WIFUSDT    30MIN SHIMANO differenza 24h:  -5.18%]
[Bybit - OLUSDT     5MIN bud. 24h: +9.44%]
[Bybit - ONTUSDT    pesca giornaliera 1. 24h: -6.66%]
[Bybit - 1000RATSUS 30MIN  viagra sopra ema 10. differenza 24h:  +10.63% PERFEZIONE]
```

Each message also contains a link like:
`https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:CFXUSDT.P&timeframe=30`

CRITICAL: The pair in the message text can be TRUNCATED (e.g., "1000BONKUS" instead of
"1000BONKUSDT", "1000RATSUS" instead of "1000RATSUSDT"). Always extract the pair from
the URL parameter `symbol` — it's always correct. Strip the exchange prefix and ".P" suffix.
Example: "BYBIT:CFXUSDT.P" → "CFXUSDT"

## Output
Create `ingest/src/signal_normalizer.py` that:

1. Takes a raw message string (the text between square brackets + the URL)
2. Returns a structured dict with these fields:

```python
{
    "exchange": "Bybit",              # from message start
    "pair": "CFXUSDT",                # ALWAYS from URL, never from message text
    "timeframe": "30m",              # normalized: 1m, 5m, 30m, 1h, 1d
    "strategy": "viagra",            # normalized strategy name (see list below)
    "strategy_detail": "sotto ema 10", # additional detail from message
    "diff_24h_pct": -5.04,           # nullable float
    "perfezione": true,              # boolean
    "raw_message": "original text",   # for audit
    "bullweb_url": "full url",        # for reference
    "skipped": false,                 # true if strategy is in skip list
    "skip_reason": null               # "funding_rate" or "pesca_giornaliera" etc.
}
```

## Strategy Normalization Map

Normalize these from the message text to canonical names:

```python
STRATEGY_MAP = {
    # Direct matches (case insensitive)
    "bomba sismografo": "bomba_sismografo",
    "bomba": "bomba",
    "terzotocco ema60": "terzo_tocco_ema60",
    "shimano": "shimano",
    "maxy bud tf 5": "maxy_bud",
    "maxy bud": "maxy_bud",
    "emersione incrocio 60/223": "emersione_incrocio",
    "viagra sotto ema 10": "viagra_short",
    "viagra sopra ema 10": "viagra_long",
    "bud": "bud",
    "rimbalzo ema 60 tf 5": "rimbalzo_ema60",
    "rimbalzo ema 223 tf 5": "rimbalzo_ema223",
    "incrocio 60 - 223": "incrocio_60_223",
    "tre cime": "tre_cime",
    "kfc": "kfc",
    "rottura ema 60 tf 5": "rottura_ema60",
    "rottura ema 223 tf 5": "rottura_ema223",
    "molla tf 1": "molla",
    "hammer tf 30": "hammer",
    "antincrocio tf 1": "antincrocio",
    "antincrocio tf 5": "antincrocio",
    "nata ema60 a timeframe 30": "nata_ema60",
    "cambio colore": "cambio_colore",
    "doppio tocco": "doppio_tocco",
    "differenza 3% su minimo di ieri": "proximity_min_ieri",
    "differenza 3% su massimo di ieri": "proximity_max_ieri",
    "daily to the moon": "daily_moon",
    "quasi in ath": "ath_proximity",
    "rottura ema 10": "rottura_ema10",

    # Sentiment signals (not for entry, context only)
    "panico pa panico pa panico pauraaa": "sentiment_panico",
    "c'è mini-fear": "sentiment_mini_fear",
    "c'è fear": "sentiment_fear",
    "c'è una mini fomo": "sentiment_mini_fomo",
    "c'è fomo": "sentiment_fomo",
}
```

## Strategies to SKIP (not processed further)

```python
SKIP_STRATEGIES = ["funding", "pesca giornaliera"]
```

If the message contains any of these, set `skipped=true` and `skip_reason`.

## Timeframe Normalization

```python
TIMEFRAME_MAP = {
    "1min": "1m", "1 min": "1m", "1m": "1m",
    "5min": "5m", "5 min": "5m", "5m": "5m",
    "30min": "30m", "30 min": "30m", "30m": "30m",
    "1h": "1h", "1ora": "1h",
    "1giorno": "1d", "1d": "1d", "daily": "1d",
}
```

For signals where TF is implicit:
- cambio colore → always "1d"
- quasi in ATH → always "1d"
- doppio tocco → always "1d"
- differenza 3% su min/max ieri → always "1d"
- daily to the moon → always "1d"
- pesca giornaliera → always "1d"

If TF cannot be determined, set to null.

## Edge Cases to Handle
- Pair truncation: "1000BONKUS" in text but "1000BONKUSDT" in URL → use URL
- Extra spaces in message text → normalize
- Missing diff_24h → set to null
- "Differenza candela giorno precedente" → parse as diff_24h_pct
- Messages without URL → parse pair from text as fallback, log warning
- Unknown strategy → set strategy to "unknown", log warning, do NOT crash

## File Structure

Create these files:
- `ingest/src/signal_normalizer.py` — the normalizer class/functions
- `ingest/tests/test_signal_normalizer.py` — unit tests using real examples above

## Tests Must Cover
- Each strategy type at least once
- Pair extraction from URL
- Pair truncation fallback
- Perfezione flag presence/absence
- diff_24h parsing (positive, negative, absent)
- Timeframe normalization
- Skip strategies (funding, pesca)
- Unknown strategy handling
- cambio colore implicit TF

## Code Standards
- Python 3.12+
- Comments in English
- Type hints on all functions
- No external dependencies (stdlib only + re for regex)
- Functions, not classes (keep it simple)
