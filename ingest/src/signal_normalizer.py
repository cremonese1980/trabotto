"""
Signal normalizer for BullBot Telegram messages.
Parses raw BullBot messages into structured signal dicts.
"""

import re
import logging
import warnings
from typing import Optional
from urllib.parse import urlparse, parse_qs

logger = logging.getLogger(__name__)

# Canonical strategy names mapped from message text (longest match first)
STRATEGY_MAP: dict[str, str] = {
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
    # Sentiment signals
    "panico pa panico pa panico pauraaa": "sentiment_panico",
    "c'è mini-fear": "sentiment_mini_fear",
    "c'è fear": "sentiment_fear",
    "c'è una mini fomo": "sentiment_mini_fomo",
    "c'è fomo": "sentiment_fomo",
}

# Strategies that require implicit timeframe "1d"
_IMPLICIT_DAILY_TF: set[str] = {
    "cambio_colore",
    "ath_proximity",
    "doppio_tocco",
    "proximity_min_ieri",
    "proximity_max_ieri",
    "daily_moon",
    "pesca_giornaliera",
}

# Raw substrings that cause a signal to be skipped
SKIP_STRATEGIES: list[str] = ["funding", "pesca giornaliera"]

TIMEFRAME_MAP: dict[str, str] = {
    "1min": "1m",
    "1 min": "1m",
    "1m": "1m",
    "5min": "5m",
    "5 min": "5m",
    "5m": "5m",
    "30min": "30m",
    "30 min": "30m",
    "30m": "30m",
    "1h": "1h",
    "1ora": "1h",
    "1giorno": "1d",
    "1 giorno": "1d",
    "1d": "1d",
    "daily": "1d",
}

# Sorted by length descending so longer keys match first
_STRATEGY_KEYS_SORTED: list[str] = sorted(STRATEGY_MAP.keys(), key=len, reverse=True)


def _extract_pair_from_url(url: str) -> Optional[str]:
    """
    Extract the trading pair from a bullweb URL.
    Example: "BYBIT:CFXUSDT.P" → "CFXUSDT"
    """
    try:
        parsed = urlparse(url)
        params = parse_qs(parsed.query)
        symbol = params.get("symbol", [None])[0]
        if not symbol:
            return None
        # Strip exchange prefix (e.g., "BYBIT:") and suffix (e.g., ".P")
        if ":" in symbol:
            symbol = symbol.split(":", 1)[1]
        symbol = symbol.rstrip(".P")
        # Remove trailing ".P" more precisely
        if symbol.endswith(".P"):
            symbol = symbol[:-2]
        return symbol or None
    except Exception:
        return None


def _extract_exchange(text: str) -> Optional[str]:
    """Extract exchange name from message text (e.g. 'Bybit', 'Binance')."""
    match = re.match(r"^\s*([A-Za-z]+)\s*-", text)
    if match:
        name = match.group(1).strip()
        return name.capitalize()
    return None


def _normalize_timeframe(raw: str) -> Optional[str]:
    """Map a raw timeframe string to the canonical form."""
    key = raw.strip().lower().replace(" ", "")
    # Try with spaces too
    for k, v in TIMEFRAME_MAP.items():
        if k.replace(" ", "") == key:
            return v
    return None


def _extract_timeframe(text: str) -> Optional[str]:
    """
    Find a timeframe token in the message text.
    Matches patterns like '30MIN', '5MIN', '1GIORNO', '1MIN'.
    """
    pattern = r"\b(\d+\s*(?:min|giorno|ora|h|m))\b"
    match = re.search(pattern, text, re.IGNORECASE)
    if match:
        raw = match.group(1).strip().lower().replace(" ", "")
        # Normalise to map key format
        for k, v in TIMEFRAME_MAP.items():
            if k.replace(" ", "") == raw:
                return v
    return None


def _extract_diff_24h(text: str) -> Optional[float]:
    """
    Parse diff_24h_pct from text.
    Handles patterns like:
      'differenza 24h: -5.04%'
      '24h: +11.69%'
      'Differenza candela giorno precedente: -5.0%'
    """
    patterns = [
        r"differenza candela giorno precedente[:\s]+([+-]?\d+(?:\.\d+)?)\s*%",
        r"differenza\s+24h[:\s]+([+-]?\d+(?:\.\d+)?)\s*%",
        r"24h[:\s]+([+-]?\d+(?:\.\d+)?)\s*%",
    ]
    for pat in patterns:
        match = re.search(pat, text, re.IGNORECASE)
        if match:
            try:
                return float(match.group(1))
            except ValueError:
                pass
    return None


def _match_strategy(text: str) -> tuple[Optional[str], Optional[str]]:
    """
    Return (canonical_strategy, strategy_detail) by matching the longest
    key in STRATEGY_MAP against the lowercased message text.
    """
    text_lower = text.lower()
    for key in _STRATEGY_KEYS_SORTED:
        if key in text_lower:
            canonical = STRATEGY_MAP[key]
            # strategy_detail: the portion of text that was matched (original casing trimmed)
            idx = text_lower.find(key)
            detail = text[idx: idx + len(key)].strip()
            return canonical, detail
    return None, None


def _check_skip(text: str) -> tuple[bool, Optional[str]]:
    """Return (skipped, skip_reason) based on SKIP_STRATEGIES."""
    text_lower = text.lower()
    for skip in SKIP_STRATEGIES:
        if skip in text_lower:
            reason = skip.replace(" ", "_")
            return True, reason
    return False, None


def normalize(raw_message: str, bullweb_url: Optional[str] = None) -> dict:
    """
    Parse a raw BullBot message into a structured signal dict.

    Args:
        raw_message: The text between square brackets (or the full line).
        bullweb_url: The bullweb chart URL accompanying the message.

    Returns:
        Structured signal dict. Never raises — unknown/unresolvable fields
        are set to None and logged.
    """
    # Strip surrounding brackets if present
    text = raw_message.strip()
    if text.startswith("[") and "]" in text:
        text = text[1: text.index("]")].strip()

    # Check for skip strategies first
    skipped, skip_reason = _check_skip(text)
    if skipped:
        # For skipped signals, do minimal parsing
        exchange = _extract_exchange(text)
        pair = None
        if bullweb_url:
            pair = _extract_pair_from_url(bullweb_url)
        return {
            "exchange": exchange,
            "pair": pair,
            "timeframe": "1d",
            "strategy": skip_reason,
            "strategy_detail": None,
            "diff_24h_pct": _extract_diff_24h(text),
            "perfezione": "PERFEZIONE" in raw_message.upper(),
            "raw_message": raw_message,
            "bullweb_url": bullweb_url,
            "skipped": True,
            "skip_reason": skip_reason,
        }

    exchange = _extract_exchange(text)

    # Pair: always prefer URL; fallback to text with warning
    pair: Optional[str] = None
    if bullweb_url:
        pair = _extract_pair_from_url(bullweb_url)
    if not pair:
        warnings.warn(
            f"No URL provided or pair not extractable from URL for message: {raw_message!r}. "
            "Attempting to extract pair from text (may be truncated).",
            UserWarning,
            stacklevel=2,
        )
        logger.warning("Pair extracted from text (fallback) for message: %r", raw_message)
        # Try to extract pair from text: token after "- " before next space/TF
        match = re.search(r"-\s+([A-Z0-9]+)\s", text)
        if match:
            pair = match.group(1)

    strategy, strategy_detail = _match_strategy(text)
    if strategy is None:
        logger.warning("Unknown strategy in message: %r", raw_message)
        strategy = "unknown"
        strategy_detail = None

    # Timeframe: try explicit token, then implicit from strategy
    timeframe = _extract_timeframe(text)
    if timeframe is None and strategy in _IMPLICIT_DAILY_TF:
        timeframe = "1d"

    diff_24h = _extract_diff_24h(text)
    perfezione = "PERFEZIONE" in raw_message.upper()

    return {
        "exchange": exchange,
        "pair": pair,
        "timeframe": timeframe,
        "strategy": strategy,
        "strategy_detail": strategy_detail,
        "diff_24h_pct": diff_24h,
        "perfezione": perfezione,
        "raw_message": raw_message,
        "bullweb_url": bullweb_url,
        "skipped": False,
        "skip_reason": None,
    }
