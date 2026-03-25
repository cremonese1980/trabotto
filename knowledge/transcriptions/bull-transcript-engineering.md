# 🔒 Bull Transcript Engineering – Single Source of Truth

**Status:** ACTIVE  
**Scope:** Trabotto / Bull Knowledge Base  
**Goal:** trasformare trascrizioni video di Bull in regole e setup formali, versionabili e utilizzabili da sistemi deterministici e AI.

---

## 1. Workflow ufficiale

### 1.1 Input

- Trascrizione grezza di un singolo video (o sezione coerente).
- Incollata manualmente dall'utente in una chat dedicata.
- Nessun pre-processing richiesto lato utente.

### 1.2 Output (unico artefatto da salvare)

- Regole e setup ingegnerizzati in formato YAML.
- Completi di metadata strutturati.
- Il testo originale NON è una fonte operativa.

> Il testo originale può essere conservato solo come riferimento umano, mai come input di sistema.

---

## 2. Fase 1 — Pulizia formale della trascrizione

### 2.1 Obiettivo

Ridurre la trascrizione a contenuto informativo operativo, eliminando rumore linguistico e narrativo.

### 2.2 Elementi da RIMUOVERE sempre

- saluti e chiusure
- call to action
- filler conversazionali
- ripetizioni retoriche
- storytelling non operativo
- meta-discorso sul video

### 2.3 Elementi da MANTENERE sempre

- regole esplicite
- condizioni di validazione / invalidazione
- warning e avvertimenti
- riferimenti a timeframe, liquidità, volatilità, struttura di mercato
- esempi solo se generalizzabili

### 2.4 Output

La pulizia è uno step interno, non un artefatto salvato.

---

## 3. Fase 2 — Core Engineering (YAML)

Questa è la parte centrale e l'unica da versionare per contenuto.

### 3.1 Tipologie ammesse

- `RULE`
- `SETUP`
- `INVALIDATION`
- `WARNING`
- `GUIDELINE`

---

## 4. Metadata – Enumerazione ufficiale

### 4.1 series

`principianti` | `tecniche`

### 4.2 entity_type

`RULE` | `SETUP` | `INVALIDATION` | `WARNING` | `GUIDELINE`

### 4.3 scope

`PRE_FILTER` | `LIQUIDITY_GATE` | `AI_DECISION_SUPPORT` | `TRADE_EXECUTION` | `MONITORING` | `GLOBAL`

### 4.4 severity

`HARD` | `SOFT` (solo per RULE / INVALIDATION)

### 4.5 applicability

```yaml
applicability:
  timeframes: [1m, 5m, 15m]
  market_regime: TREND | RANGE | HIGH_VOL
  session: ASIA | LONDON | NY | ANY
```

---

## 5. Cosa è esplicitamente FUORI

- riassunti discorsivi
- testo naturale come fonte di verità
- prompt globali
- fine-tuning sui video
- interpretazioni creative

---

## 6. Regole operative per la chat dedicata

1. **1 trascrizione = 1 messaggio**
2. L'assistente restituisce solo YAML ingegnerizzato
3. L'utente salva solo il YAML come source of truth dei contenuti

---

## 7. Golden Rule – Evolution Policy

- Questo documento è la **Single Source of Truth** del processo.
- Il formato raffinato **NON è immutabile**.
- Se emergono nuovi pattern o casi non previsti:
   1. il processo si ferma;
   2. il SSOT viene aggiornato;
   3. tutte le trascrizioni già ingegnerizzate vengono revisionate.
- **Nessun YAML può esistere fuori conformità dal SSOT corrente.**

---

## 8. Esempio CANONICO di YAML per-video

Questo esempio è normativo e definisce il formato ufficiale.

```yaml
video:
  id: YT_<youtube_id>
  author: Bull
  series: principianti | tecniche
  playlist: "Nome playlist"
  video_index: <int>
  title: "Titolo del video"
  duration_minutes: <int>
  published_at: YYYY-MM-DD

meta:
  intent: "Scopo principale del video"
  audience_level: neofiti | base | intermedio | avanzato
  tone: ["diretto", "duro", "educativo"]
  confidence: HIGH | MEDIUM | LOW

entities:
  - id: BULL_<SERIES>_<NNN>
    entity_type: RULE | SETUP | INVALIDATION | WARNING | GUIDELINE
    scope: PRE_FILTER | LIQUIDITY_GATE | AI_DECISION_SUPPORT | TRADE_EXECUTION | MONITORING | GLOBAL
    severity: HARD | SOFT

    source:
      timecode: "MM:SS-MM:SS"

    condition:
      - <condizione deterministica>
    action:
      - <azione>

    statement: "<affermazione operativa>"

    requirements: []
    invalidations: []

    applicability:
      timeframes: [1m, 5m, 15m]
      market_regime: TREND | RANGE | HIGH_VOL
      session: ASIA | LONDON | NY | ANY

    rationale:
      - "<perché Bull dice questo>"

    notes:
      - "<chiarimenti non esecutivi>"

summary:
  core_message: "Messaggio centrale del video in 1–2 frasi"
```
