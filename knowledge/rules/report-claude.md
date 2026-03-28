# Review a Campione delle Rules — Report Claude

Data: 2026-03-28
Reviewer: Claude Opus 4.6
Metodo: campione di 2-3 video per ciascuna delle 6 playlist, incrociando trascrizioni, regole YAML, prompt di estrazione (`gpt-transcription-prompt.md`) e vocabolario (`vocabulary-clean.json`).

## Mapping ID per playlist

| Playlist | Prefisso ID |
|----------|-------------|
| tecniche | T |
| neofiti | N |
| live-trading-crypto-analisi | LTCRA |
| nuova-neofiti | NN |
| live-streaming-su-youtube | LSSY |
| live-trade-con-analisi | LTCA |

Tutti gli ID nei file YAML seguono correttamente questo mapping.

---

## 1. Playlist: `live-streaming-su-youtube` (video 003, 005)

### Video 003 — BULL_LSSY_03 — "Live Q&A Discord: book, analisi mattutina, orari operativi"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_LSSY_03_xx` conforme al mapping |
| Litmus test | Buono con eccezione | `BULL_LSSY_03_08`: `all_charts_dirty == true` e borderline soggettivo. Riformulabile con indicatori misurabili (es. `ema5_crossings_count > N`) |
| Standard actions | OK | ENTER_LONG, SKIP_TRADE, WAIT, MONITOR_LEVEL, CLASSIFY_SETUP |
| Tags | OK | 2-3 per entity, coerenti col vocabolario |
| Vocabulary | OK | EMA, book, conformazione, breakout, cambio candela usati correttamente |
| SKIP vs WAIT | OK | `BULL_LSSY_03_07` (apertura mercati europei) = SKIP_TRADE corretto |
| SL/TP | OK | Nessun SL/TP inventato |
| **Score** | **4/5** | Buona estrazione. Unico punto debole: condizione soggettiva su grafici sporchi |

### Video 005 — BULL_LSSY_05 — "Live 4000 iscritti: pulizia grafici, breakout ai massimi"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_LSSY_05_xx` conforme |
| Litmus test | Buono con eccezioni | `no_panic_buying_detected == true` (difficile da misurare), `chart_clean == true` (vago) |
| Conditions | `BULL_LSSY_05_06`: `trend_very_strong == true` manca definizione operativa | Servono parametri tipo `ema5_clean == true AND price_above_ema10` |
| Actions | OK | |
| Vocabulary | OK | EMA 5, hammer, finta corretti |
| **Score** | **3.5/5** | Condizioni a volte troppo astratte, da riformulare con pseudo-code osservabile |

---

## 2. Playlist: `live-trade-con-analisi` (video 001, 005)

### Video 001 — BULL_LTCA_01 — "4 trade in analisi tecnica: RLC long, ETH short x2, EOS stop"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_LTCA_01_xx` conforme |
| Litmus test | Buono con eccezione | `conviction_level == HIGH` in `BULL_LTCA_01_06` e soggettivo — viola il litmus test |
| Conditions | Perlopiu eccellenti | `price_above_ema60 == true`, `ema60_tested_twice == true` sono osservabili |
| Actions | OK | ENTER_LONG/SHORT, MOVE_SL_TO_BREAKEVEN, TIGHTEN_TP, WAIT |
| Vocabulary | Eccellente | "perfezione" usata in senso tecnico corretto, "finta", "terzo tocco" |
| **Score** | **4/5** | Ottima estrazione. `conviction_level` andrebbe riformulato con parametri misurabili (es. `daily_setup_confirms_direction AND tf >= 30m`) |

### Video 005 — BULL_LTCA_05 — "Trade in perdita: regola dei 40 secondi, EMA 223"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_LTCA_05_xx` conforme |
| Litmus test | Eccellente | `seconds_remaining_in_candle <= 20`, `price_below_ema223 == true` sono precisi e misurabili |
| Conditions | `BULL_LTCA_05_05` (iceberg) | Descrive tecnica di esecuzione, non regola di trading. Potrebbe essere CONTEXT_ONLY secondo il prompt ("basic execution mechanics -> CONTEXT_ONLY") |
| Cross-TF | Buono | Nota che EMA 223 su 1m corrisponde a EMA 10 su 30m — coerente col vocabolario |
| **Score** | **4/5** | Regole precise e ben formulate. Iceberg da rivalutare come categoria |

---

## 3. Playlist: `live-trading-crypto-analisi` (video 010, 020)

### Video 010 — BULL_LTCRA_10 — "Spot tricks e BullBot: shimano 30min su COTI e YGG"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_LTCRA_10_xx` conforme |
| Litmus test | `bullbot_shimano_detected == true` e corretto (segnale esterno osservabile) | |
| Lingua | ANOMALIA | Rules, CTX e summary in **inglese** invece che italiano. Incoerente con playlist 1 e 2 |
| Conditions | `BULL_LTCRA_10_03`: `strong_trend_identified == true` e vago | Servono parametri espliciti |
| Vocabulary | OK | shimano, conformazione, EMA 60 usati correttamente |
| **Score** | **3.5/5** | Lingua incoerente, alcune condizioni vaghe |

### Video 020 — BULL_LTCRA_20 — "4 Trade: RLC Daily Breakout, ETH Triple EMA 60 Short"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_LTCRA_20_xx` conforme |
| Lingua | Inglese | Stessa anomalia del video 010 |
| Litmus test | `conviction_level == very_high` e `conviction_level == low_or_medium` violano il litmus test (soggettivo) | Stessa issue di LTCA_01 |
| Conditions | `ema60_tests_are_precise == true` e vago — "precise" come? | |
| Contenuto | Possibile sovrapposizione con LTCA_01 | Stessi trade (RLC long, ETH short, EOS loss). Regole complementari ma rischio ridondanza |
| **Score** | **3/5** | Condizioni soggettive, lingua incoerente, sovrapposizione contenuto |

---

## 4. Playlist: `neofiti` (video 005, 020)

### Video 005 — BULL_N_05 (nel file: BULL_P_05) — "Book Bybit vs Binance: liquidita, livelli e grafici puliti"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | ANOMALIA | Usa `BULL_P_05_xx` ma il mapping corretto per neofiti e `N`, non `P`. video.id = `YT_BULL_P_005` anche non conforme |
| Litmus test | OK | `book_large_level_visible == true`, `touch_count(level) >= 3` sono osservabili |
| Actions | `BULL_P_05_03` ha azioni multiple: `ENTER_LONG` + `ENTER_SHORT` + `CLASSIFY_SETUP` | Ambiguo — dovrebbero essere entity separate o un solo CLASSIFY_SETUP |
| Ratio ME/CTX | 3 ME + 3 CTX | Equilibrato per neofiti |
| **Score** | **3.5/5** | ID prefix errato (`P` invece di `N`), multi-action da rivedere |

### Video 020 — BULL_N_20 (nel file: BULL_P_20) — "Live trading short: tre sessioni multi-crypto"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | ANOMALIA | Usa `BULL_P_20_xx` ma il mapping corretto e `N` per neofiti |
| Lingua | Inglese | Incoerente col video 005 della stessa playlist (italiano) |
| Litmus test | Eccellente | `price_bouncing_with_lower_highs_sequence >= 3`, `large_green_candle_closes_above_ema60 == true` |
| Vocabulary | OK | EMA 60, strappo, finta usati correttamente |
| Ratio ME/CTX | 5 ME + 2 CTX | Troppo sbilanciato verso ME per la serie neofiti (il prompt dice "principianti -> more CONTEXT_ONLY") |
| **Score** | **3/5** | ID prefix errato, lingua incoerente, ratio ME/CTX sbilanciato |

---

## 5. Playlist: `nuova-neofiti` (video 003, 007)

### Video 003 — BULL_NN_03 — "Lettura delle candele, EMA e pulizia del grafico"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_NN_03_xx` conforme al mapping |
| Litmus test | OK | Condizioni con pseudo-code osservabile |
| Ratio ME/CTX | 2 ME + 5 CTX | Coerente con neofiti |
| Vocabulary | OK | finta, EMA, conformazioni usati correttamente |
| **Score** | **4/5** | Buon contenuto, ID conforme, buon bilanciamento |

### Video 007 — BULL_NN_07 — "Punti trigger e breakout"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_NN_07_xx` conforme |
| Litmus test | Eccellente | `touch_count(resistance_level) >= 3`, `large_order_quantity_decreasing == true` |
| Vocabulary | Eccellente | terzo tocco, trigger, book tutti usati correttamente e coerenti col vocabolario |
| **Score** | **4.5/5** | Ottima qualita complessiva |

---

## 6. Playlist: `tecniche` (video 010, 025, 045)

### Video 010 — BULL_T_10 — "TF 30 minuti - EMA 60 come spartiacque per inversioni di trend"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_T_10_xx` conforme |
| video.id | Lieve discrepanza | `BULL_T_010` ma il template nel prompt usa `YT_BULL_T_010` |
| Lingua | Inglese | Incoerente con video 045 (italiano) nella stessa playlist |
| Litmus test | Eccellente | `price_below_ema60_30m == true`, `first_touch_ema60_30m_after_crash == true` |
| Strategy name | `tf30ema60` nei tag | Conforme alla lista del prompt |
| Ratio ME/CTX | 6 ME + 3 CTX | Coerente con tecniche |
| **Score** | **4/5** | Eccellente estrazione, lingua inglese unico difetto |

### Video 025 — BULL_T_25 — "Segnale Bomba"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_T_25_xx` conforme |
| Lingua | Inglese | Incoerente |
| Strategy name | `bomba` e `bud` usati correttamente | Conformi alla lista del prompt |
| Litmus test | `bullbot_bomba_signal == true` e osservabile (segnale esterno) | OK |
| Conditions | `asset_was_dormant_before_signal == true` | Vago — "dormant" non ha parametri. Riformulare es. `price_change_24h_pct < 1` |
| **Score** | **4/5** | Buona, condizione "dormant" da parametrizzare |

### Video 045 — BULL_T_45 — "Le Tre Cime - tecnica di trading su pennant con EMA"

| Aspetto | Valutazione | Note |
|---------|-------------|------|
| ID format | OK | `BULL_T_45_xx` conforme |
| Lingua | Italiano | Coerente |
| Litmus test | Eccellente | `swing_high_count >= 3`, `each_swing_high < previous_swing_high` sono precisi e misurabili |
| Strategy name | "tre cime" non e nella lista del prompt | Ma e chiaramente una strategia specifica di Bull; la lista andrebbe aggiornata |
| **Score** | **4.5/5** | Migliore qualita complessiva del campione |

---

## Riepilogo Problemi Sistematici

| # | Problema | Gravita | Playlist | Suggerimento |
|---|----------|---------|----------|--------------|
| 1 | **Lingua incoerente** (mix italiano/inglese) | MEDIA | 3, 4(020), 6(010,025) | Uniformare: decidere una lingua e ri-generare i file incoerenti |
| 2 | **`conviction_level` soggettivo** | MEDIA | 2(001), 3(020) | Viola il litmus test. Riformulare con: `daily_setup_confirms_direction == true AND timeframe >= 30m` |
| 3 | **Condizioni vaghe** senza parametri | MEDIA | 1(003,005), 3(010,020), 6(025) | "chart_clean", "trend_very_strong", "asset_dormant" richiedono soglie esplicite |
| 4 | **ID prefix errato nella playlist neofiti** | MEDIA | 4(005,020) | Usano `BULL_P_xx` ma il mapping corretto e `BULL_N_xx`. Da ri-generare |
| 5 | **video.id inconsistente** | BASSA | Varie | Formati diversi: `YT_BULL_P_005`, `BULL_LTCA_01`, `BULL_T_010` — uniformare |
| 6 | **Sovrapposizione contenuto** | BASSA | 2(001) vs 3(020) | LTCA_01 e LTCRA_20 coprono gli stessi trade (RLC/ETH/EOS). Regole complementari ma rischio ridondanza |
| 7 | **Multi-action** in una entity | BASSA | 4(005) | `ENTER_LONG` + `ENTER_SHORT` + `CLASSIFY_SETUP` nella stessa entity non ha senso operativo |
| 8 | **Iceberg come MACHINE_EXECUTABLE** | BASSA | 2(005) | Il prompt classifica "basic execution mechanics" come CONTEXT_ONLY |

## Statistiche Campione

| Playlist | Video | ME | CTX | Score Medio |
|----------|-------|----|-----|-------------|
| live-streaming-su-youtube | 003, 005 | 16 | 12 | 3.75/5 |
| live-trade-con-analisi | 001, 005 | 11 | 4 | 4/5 |
| live-trading-crypto-analisi | 010, 020 | 10 | 10 | 3.25/5 |
| neofiti | 005, 020 | 8 | 5 | 3.25/5 |
| nuova-neofiti | 003, 007 | 8 | 7 | 4.25/5 |
| tecniche | 010, 025, 045 | 19 | 8 | 4.17/5 |

## Raccomandazioni Prioritarie

1. **Uniformare la lingua** — Scegliere italiano o inglese e rigenerare i file incoerenti
2. **Correggere gli ID neofiti** — Da `BULL_P_xx` a `BULL_N_xx` per i file della playlist neofiti
3. **Eliminare `conviction_level`** — Sostituire con condizioni osservabili (timeframe, conferma daily, pulizia EMA)
4. **Parametrizzare condizioni vaghe** — Ogni condizione machine-executable deve avere una soglia o un indicatore misurabile
5. **Aggiornare `gpt-transcription-prompt.md`** — Estendere il formato ID per supportare tutti i 6 prefissi (T, N, LTCRA, NN, LSSY, LTCA) e aggiungere "tre cime" alla lista dei nomi strategia
