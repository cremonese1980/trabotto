"""
Unit tests for signal_normalizer.py.
Uses real BullBot message examples from the spec.
"""

import sys
import os
import warnings
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "src"))

from signal_normalizer import normalize, STRATEGY_MAP, SKIP_STRATEGIES


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

BYBIT_CFXUSDT_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:CFXUSDT.P&timeframe=30"
BYBIT_BONKUSDT_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:1000BONKUSDT.P&timeframe=30"
BYBIT_SXP_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:SXPUSDT.P&timeframe=5"
BYBIT_OPN_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:OPNUSDT.P&timeframe=1440"
BYBIT_RENDER_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:RENDERUSDT.P&timeframe=1440"
BINANCE_BFUSD_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BINANCE:BFUSDUSDC.P&timeframe=1440"
BYBIT_PIPPIN_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:PIPPINUSDT.P&timeframe=1"
BYBIT_TAU_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:TAUSDT.P&timeframe=1"
BYBIT_AKT_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:AKTUSDT.P&timeframe=5"
BYBIT_WIF_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:WIFUSDT.P&timeframe=30"
BYBIT_OL_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:OLUSDT.P&timeframe=5"
BYBIT_ONT_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:ONTUSDT.P&timeframe=1440"
BYBIT_RATS_URL = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:1000RATSUSDT.P&timeframe=30"


# ---------------------------------------------------------------------------
# Strategy: viagra_short
# ---------------------------------------------------------------------------

class TestViagraShort:
    def test_basic(self):
        msg = "[Bybit - CFXUSDT    30MIN  viagra sotto ema 10. differenza 24h:  -5.04% PERFEZIONE]"
        sig = normalize(msg, BYBIT_CFXUSDT_URL)
        assert sig["strategy"] == "viagra_short"
        assert sig["strategy_detail"] == "viagra sotto ema 10"
        assert sig["pair"] == "CFXUSDT"
        assert sig["exchange"] == "Bybit"
        assert sig["timeframe"] == "30m"
        assert sig["diff_24h_pct"] == -5.04
        assert sig["perfezione"] is True
        assert sig["skipped"] is False
        assert sig["skip_reason"] is None

    def test_pair_from_url_overrides_truncated_text(self):
        # Text has "1000BONKUS" (truncated) but URL has the full symbol
        msg = "[Bybit - 1000BONKUS 30MIN  viagra sotto ema 10. differenza 24h:  -5.38% PERFEZIONE]"
        sig = normalize(msg, BYBIT_BONKUSDT_URL)
        assert sig["pair"] == "1000BONKUSDT"

    def test_perfezione_present(self):
        msg = "[Bybit - CFXUSDT 30MIN viagra sotto ema 10. differenza 24h: -5.04% PERFEZIONE]"
        sig = normalize(msg, BYBIT_CFXUSDT_URL)
        assert sig["perfezione"] is True

    def test_perfezione_absent(self):
        msg = "[Bybit - TAUSDT 1MIN emersione incrocio 60/223. 24h: -9.89%]"
        sig = normalize(msg, BYBIT_TAU_URL)
        assert sig["perfezione"] is False


# ---------------------------------------------------------------------------
# Strategy: viagra_long
# ---------------------------------------------------------------------------

class TestViagraLong:
    def test_basic(self):
        msg = "[Bybit - 1000RATSUS 30MIN  viagra sopra ema 10. differenza 24h:  +10.63% PERFEZIONE]"
        sig = normalize(msg, BYBIT_RATS_URL)
        assert sig["strategy"] == "viagra_long"
        assert sig["pair"] == "1000RATSUSDT"
        assert sig["diff_24h_pct"] == 10.63
        assert sig["perfezione"] is True


# ---------------------------------------------------------------------------
# Strategy: rottura_ema223
# ---------------------------------------------------------------------------

class TestRotturaEma223:
    def test_basic(self):
        msg = "[Bybit - SXPUSDT    5MIN Rottura ema 223 tf 5 differenza 24h:  +7.82%]"
        sig = normalize(msg, BYBIT_SXP_URL)
        assert sig["strategy"] == "rottura_ema223"
        assert sig["pair"] == "SXPUSDT"
        assert sig["timeframe"] == "5m"
        assert sig["diff_24h_pct"] == 7.82
        assert sig["perfezione"] is False


# ---------------------------------------------------------------------------
# Strategy: cambio_colore (implicit daily TF)
# ---------------------------------------------------------------------------

class TestCambioColore:
    def test_implicit_daily_tf(self):
        msg = "[Bybit - OPNUSDT    sta per fare il cambio colore PERFEZIONE]"
        sig = normalize(msg, BYBIT_OPN_URL)
        assert sig["strategy"] == "cambio_colore"
        assert sig["timeframe"] == "1d"
        assert sig["perfezione"] is True
        assert sig["pair"] == "OPNUSDT"


# ---------------------------------------------------------------------------
# Strategy: proximity_min_ieri (implicit daily TF)
# ---------------------------------------------------------------------------

class TestProximityMinIeri:
    def test_implicit_daily_tf(self):
        msg = "[Bybit - RENDERUSDT 1GIORNO differenza 3% su minimo di ieri. Differenza candela giorno precedente: -5.0% PERFEZIONE]"
        sig = normalize(msg, BYBIT_RENDER_URL)
        assert sig["strategy"] == "proximity_min_ieri"
        assert sig["timeframe"] == "1d"
        assert sig["diff_24h_pct"] == -5.0
        assert sig["pair"] == "RENDERUSDT"

    def test_differenza_candela_giorno_precedente(self):
        """'Differenza candela giorno precedente' should parse into diff_24h_pct."""
        msg = "[Bybit - RENDERUSDT 1GIORNO differenza 3% su minimo di ieri. Differenza candela giorno precedente: -5.0% PERFEZIONE]"
        sig = normalize(msg, BYBIT_RENDER_URL)
        assert sig["diff_24h_pct"] == -5.0


# ---------------------------------------------------------------------------
# Strategy: ath_proximity (implicit daily TF)
# ---------------------------------------------------------------------------

class TestAthProximity:
    def test_basic(self):
        msg = "[Binance - BFUSDUSDC  è quasi in ATH di 500 giorni 0.04% dal cambio candela]"
        sig = normalize(msg, BINANCE_BFUSD_URL)
        assert sig["strategy"] == "ath_proximity"
        assert sig["timeframe"] == "1d"
        assert sig["exchange"] == "Binance"
        assert sig["pair"] == "BFUSDUSDC"


# ---------------------------------------------------------------------------
# Strategy: emersione_incrocio
# ---------------------------------------------------------------------------

class TestEmersioneIncrocio:
    def test_perfezione_true(self):
        msg = "[Bybit - PIPPINUSDT 1MIN emersione incrocio 60/223. 24h: -27.19% PERFEZIONE]"
        sig = normalize(msg, BYBIT_PIPPIN_URL)
        assert sig["strategy"] == "emersione_incrocio"
        assert sig["timeframe"] == "1m"
        assert sig["diff_24h_pct"] == -27.19
        assert sig["perfezione"] is True
        assert sig["pair"] == "PIPPINUSDT"

    def test_perfezione_false(self):
        msg = "[Bybit - TAUSDT     1MIN emersione incrocio 60/223. 24h: -9.89%]"
        sig = normalize(msg, BYBIT_TAU_URL)
        assert sig["strategy"] == "emersione_incrocio"
        assert sig["perfezione"] is False


# ---------------------------------------------------------------------------
# Strategy: bud
# ---------------------------------------------------------------------------

class TestBud:
    def test_basic(self):
        msg = "[Bybit - AKTUSDT    5MIN bud. 24h: +11.69%]"
        sig = normalize(msg, BYBIT_AKT_URL)
        assert sig["strategy"] == "bud"
        assert sig["timeframe"] == "5m"
        assert sig["diff_24h_pct"] == 11.69
        assert sig["pair"] == "AKTUSDT"

    def test_second_bud_signal(self):
        msg = "[Bybit - OLUSDT     5MIN bud. 24h: +9.44%]"
        sig = normalize(msg, BYBIT_OL_URL)
        assert sig["strategy"] == "bud"
        assert sig["pair"] == "OLUSDT"
        assert sig["diff_24h_pct"] == 9.44


# ---------------------------------------------------------------------------
# Strategy: shimano
# ---------------------------------------------------------------------------

class TestShimano:
    def test_basic(self):
        msg = "[Bybit - WIFUSDT    30MIN SHIMANO differenza 24h:  -5.18%]"
        sig = normalize(msg, BYBIT_WIF_URL)
        assert sig["strategy"] == "shimano"
        assert sig["timeframe"] == "30m"
        assert sig["diff_24h_pct"] == -5.18
        assert sig["pair"] == "WIFUSDT"


# ---------------------------------------------------------------------------
# Skip strategies
# ---------------------------------------------------------------------------

class TestSkipStrategies:
    def test_pesca_giornaliera(self):
        msg = "[Bybit - ONTUSDT    pesca giornaliera 1. 24h: -6.66%]"
        sig = normalize(msg, BYBIT_ONT_URL)
        assert sig["skipped"] is True
        assert sig["skip_reason"] == "pesca_giornaliera"
        assert sig["pair"] == "ONTUSDT"

    def test_funding(self):
        msg = "[Bybit - BTCUSDT    funding rate alert 24h: +0.01%]"
        url = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:BTCUSDT.P&timeframe=1"
        sig = normalize(msg, url)
        assert sig["skipped"] is True
        assert sig["skip_reason"] == "funding"


# ---------------------------------------------------------------------------
# Pair extraction
# ---------------------------------------------------------------------------

class TestPairExtraction:
    def test_pair_always_from_url(self):
        msg = "[Bybit - TRUNCATED 30MIN shimano differenza 24h: -1.0%]"
        url = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:REALUSDT.P&timeframe=30"
        sig = normalize(msg, url)
        assert sig["pair"] == "REALUSDT"

    def test_pair_fallback_from_text_when_no_url(self):
        msg = "[Bybit - AKTUSDT    5MIN bud. 24h: +11.69%]"
        with warnings.catch_warnings(record=True) as w:
            warnings.simplefilter("always")
            sig = normalize(msg, bullweb_url=None)
            assert any("fallback" in str(warning.message).lower() or "url" in str(warning.message).lower() for warning in w)
        assert sig["pair"] == "AKTUSDT"

    def test_pair_truncation_corrected_via_url(self):
        # 1000RATSUS in text, 1000RATSUSDT in URL
        msg = "[Bybit - 1000RATSUS 30MIN  viagra sopra ema 10. differenza 24h:  +10.63% PERFEZIONE]"
        sig = normalize(msg, BYBIT_RATS_URL)
        assert sig["pair"] == "1000RATSUSDT"


# ---------------------------------------------------------------------------
# Timeframe normalization
# ---------------------------------------------------------------------------

class TestTimeframeNormalization:
    def test_1min(self):
        msg = "[Bybit - PIPPINUSDT 1MIN emersione incrocio 60/223. 24h: -27.19% PERFEZIONE]"
        assert normalize(msg, BYBIT_PIPPIN_URL)["timeframe"] == "1m"

    def test_5min(self):
        msg = "[Bybit - AKTUSDT 5MIN bud. 24h: +11.69%]"
        assert normalize(msg, BYBIT_AKT_URL)["timeframe"] == "5m"

    def test_30min(self):
        msg = "[Bybit - CFXUSDT 30MIN viagra sotto ema 10. differenza 24h: -5.04% PERFEZIONE]"
        assert normalize(msg, BYBIT_CFXUSDT_URL)["timeframe"] == "30m"

    def test_1giorno(self):
        msg = "[Bybit - RENDERUSDT 1GIORNO differenza 3% su minimo di ieri. Differenza candela giorno precedente: -5.0% PERFEZIONE]"
        assert normalize(msg, BYBIT_RENDER_URL)["timeframe"] == "1d"

    def test_implicit_daily_cambio_colore(self):
        msg = "[Bybit - OPNUSDT sta per fare il cambio colore PERFEZIONE]"
        assert normalize(msg, BYBIT_OPN_URL)["timeframe"] == "1d"

    def test_implicit_daily_ath_proximity(self):
        msg = "[Binance - BFUSDUSDC è quasi in ATH di 500 giorni 0.04% dal cambio candela]"
        assert normalize(msg, BINANCE_BFUSD_URL)["timeframe"] == "1d"


# ---------------------------------------------------------------------------
# diff_24h_pct parsing
# ---------------------------------------------------------------------------

class TestDiff24h:
    def test_negative(self):
        msg = "[Bybit - CFXUSDT 30MIN viagra sotto ema 10. differenza 24h: -5.04% PERFEZIONE]"
        assert normalize(msg, BYBIT_CFXUSDT_URL)["diff_24h_pct"] == -5.04

    def test_positive(self):
        msg = "[Bybit - AKTUSDT 5MIN bud. 24h: +11.69%]"
        assert normalize(msg, BYBIT_AKT_URL)["diff_24h_pct"] == 11.69

    def test_absent(self):
        msg = "[Bybit - OPNUSDT sta per fare il cambio colore PERFEZIONE]"
        assert normalize(msg, BYBIT_OPN_URL)["diff_24h_pct"] is None


# ---------------------------------------------------------------------------
# Unknown strategy
# ---------------------------------------------------------------------------

class TestUnknownStrategy:
    def test_unknown_does_not_crash(self):
        msg = "[Bybit - XYZUSDT 5MIN this_is_a_totally_unknown_pattern 24h: +1.0%]"
        url = "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:XYZUSDT.P&timeframe=5"
        sig = normalize(msg, url)
        assert sig["strategy"] == "unknown"
        assert sig["skipped"] is False
        assert sig["pair"] == "XYZUSDT"


# ---------------------------------------------------------------------------
# Raw message and URL preserved
# ---------------------------------------------------------------------------

class TestAuditFields:
    def test_raw_message_preserved(self):
        msg = "[Bybit - CFXUSDT 30MIN viagra sotto ema 10. differenza 24h: -5.04% PERFEZIONE]"
        sig = normalize(msg, BYBIT_CFXUSDT_URL)
        assert sig["raw_message"] == msg

    def test_bullweb_url_preserved(self):
        msg = "[Bybit - CFXUSDT 30MIN viagra sotto ema 10. differenza 24h: -5.04% PERFEZIONE]"
        sig = normalize(msg, BYBIT_CFXUSDT_URL)
        assert sig["bullweb_url"] == BYBIT_CFXUSDT_URL


# ---------------------------------------------------------------------------
# Full strategy-map coverage
# ---------------------------------------------------------------------------

@pytest.mark.parametrize(
    ("raw_strategy", "expected"),
    sorted(STRATEGY_MAP.items(), key=lambda kv: kv[0]),
)
def test_every_strategy_in_strategy_map_is_recognized(raw_strategy, expected):
    msg = f"[Bybit - BTCUSDT 5MIN {raw_strategy}. 24h: +1.00%]"
    sig = normalize(msg, None)
    assert sig["strategy"] == expected


@pytest.mark.parametrize("skip_token", SKIP_STRATEGIES)
def test_every_skip_strategy_is_skipped(skip_token):
    msg = f"[Bybit - BTCUSDT 1GIORNO {skip_token} 24h: +0.10%]"
    sig = normalize(msg, "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:BTCUSDT.P&timeframe=1440")
    assert sig["skipped"] is True
    assert sig["skip_reason"] == skip_token.replace(" ", "_")


@pytest.mark.parametrize(
    "message",
    [
        "[Bybit - ADAUSDT 5MIN shimano differenza 24h: +1.0%]",
        "[Binance - ETHUSDT 1MIN bud 24h: -0.5%]",
    ],
)
def test_no_url_uses_pair_fallback(message):
    sig = normalize(message, None)
    assert sig["pair"] in {"ADAUSDT", "ETHUSDT"}


@pytest.mark.parametrize(
    "canonical_strategy",
    [
        "cambio_colore",
        "ath_proximity",
        "doppio_tocco",
        "proximity_min_ieri",
        "proximity_max_ieri",
        "daily_moon",
    ],
)
def test_all_implicit_daily_tf_strategies_set_daily_timeframe(canonical_strategy):
    key = next(raw for raw, canonical in STRATEGY_MAP.items() if canonical == canonical_strategy)
    msg = f"[Bybit - SOLUSDT {key}]"
    sig = normalize(msg, "https://bullweb.scalpingthebull.com/chart/?symbol=BYBIT:SOLUSDT.P&timeframe=1440")
    assert sig["strategy"] == canonical_strategy
    assert sig["timeframe"] == "1d"


def test_empty_message_returns_unknown_strategy():
    sig = normalize("", None)
    assert sig["strategy"] == "unknown"
    assert sig["pair"] is None


def test_null_message_raises_attribute_error():
    with pytest.raises(AttributeError):
        normalize(None, None)
