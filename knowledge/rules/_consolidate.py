#!/usr/bin/env python3
"""
Consolidate all YAML rule files into:
1. consolidated-rules.yml (MACHINE_EXECUTABLE only, deduplicated, fixed)
2. consolidated-context.yml (CONTEXT_ONLY only, deduplicated)
3. RULES_INDEX.md (statistics)

Run from repo root: python3 knowledge/rules/_consolidate.py
"""

import os
import re
import yaml
import json
from collections import defaultdict
from pathlib import Path

RULES_DIR = Path(__file__).parent
KNOWLEDGE_DIR = RULES_DIR.parent

# ID prefix mapping
PLAYLIST_PREFIX = {
    "tecniche": "T",
    "neofiti": "N",
    "live-trading-crypto-analisi": "LTCRA",
    "nuova-neofiti": "NN",
    "live-streaming-su-youtube": "LSSY",
    "live-trade-con-analisi": "LTCA",
}

# Priority for dedup: prefer tecniche > live-trade-con-analisi > live-trading-crypto-analisi > live-streaming > nuova-neofiti > neofiti
PLAYLIST_PRIORITY = {
    "tecniche": 6,
    "live-trade-con-analisi": 5,
    "live-trading-crypto-analisi": 4,
    "live-streaming-su-youtube": 3,
    "nuova-neofiti": 2,
    "neofiti": 1,
}

# Vague condition replacements
CONDITION_FIXES = {
    "conviction_level == HIGH": "daily_setup_confirms_direction == true AND timeframe_minutes >= 30",
    "conviction_level == high": "daily_setup_confirms_direction == true AND timeframe_minutes >= 30",
    "conviction_level == very_high": "daily_setup_confirms_direction == true AND timeframe_minutes >= 30 AND ema10_commanding == true",
    "conviction_level == low_or_medium": "daily_setup_confirms_direction == false OR timeframe_minutes < 30",
    "conviction_level == low": "daily_setup_confirms_direction == false OR timeframe_minutes < 5",
    "chart_clean == true": "ema5_crossings_last_10_candles <= 2 AND price_respects_ema10 == true",
    "trend_very_strong == true": "ema5_clean == true AND price_above_ema10 == true AND ema10_slope > 0",
    "all_charts_dirty == true": "ema5_crossings_last_10_candles > 5 AND price_alternates_above_below_ema60 == true",
    "clean_conformations_found == 0": "conformations_with_ema5_clean_count == 0",
    "asset_was_dormant_before_signal == true": "price_change_24h_pct < 1.0 AND volume_change_24h_pct < 50",
    "strong_trend_identified == true": "ema5_clean == true AND price_above_ema10 == true",
    "daily_trend_very_strong == true": "daily_ema5_clean == true AND daily_price_above_ema10 == true",
    "trend_identified_as_strong == true": "ema5_clean == true AND price_above_ema10 == true",
    "no_panic_buying_detected == true": "volume_spike_ratio < 3.0 AND price_acceleration_1m < 2.0",
    "chart_pattern_clean_and_precise == true": "ema5_crossings_last_10_candles <= 2 AND candle_body_consistency > 0.7",
}

# Execution mechanics patterns (should be CONTEXT_ONLY)
EXECUTION_MECHANICS_PATTERNS = [
    "iceberg_order",
    "fee_discount",
    "keyboard_shortcut",
    "F1", "F2", "F3",
    "limit_order_fee",
    "sell_shortcut",
    "cancel_open_orders",
]


def fix_id_prefix(entity_id, playlist):
    """Fix neofiti IDs from P to N."""
    if playlist == "neofiti" and "_P_" in entity_id:
        return entity_id.replace("_P_", "_N_", 1)
    return entity_id


def fix_condition(cond):
    """Replace vague conditions with parametric ones."""
    cond_stripped = cond.strip()
    for vague, fixed in CONDITION_FIXES.items():
        if vague.lower() in cond_stripped.lower():
            return cond_stripped.replace(
                next(k for k in [vague, vague.lower(), vague.upper(), cond_stripped]
                     if k.lower() == vague.lower()),
                fixed
            )
    # More flexible matching
    for vague, fixed in CONDITION_FIXES.items():
        key = vague.split("==")[0].strip() if "==" in vague else vague
        if key.lower() in cond_stripped.lower():
            return fixed
    return cond_stripped


def is_execution_mechanics(entity):
    """Check if entity is about execution mechanics (should be CONTEXT_ONLY)."""
    text = str(entity.get("conditions", [])) + str(entity.get("notes", "")) + str(entity.get("rationale", ""))
    return any(p.lower() in text.lower() for p in EXECUTION_MECHANICS_PATTERNS)


def normalize_conditions(conditions):
    """Normalize conditions for dedup comparison."""
    if not conditions:
        return frozenset()
    normalized = []
    for c in conditions:
        c = c.strip().lower()
        c = re.sub(r'\s+', ' ', c)
        c = c.replace("== true", "").replace("==true", "").strip()
        normalized.append(c)
    return frozenset(normalized)


def conditions_signature(entity):
    """Create a signature for dedup based on conditions + action."""
    conds = normalize_conditions(entity.get("conditions", []))
    actions = tuple(sorted(entity.get("action", []))) if isinstance(entity.get("action"), list) else (str(entity.get("action", "")),)
    return (conds, actions)


def context_signature(entity):
    """Create a signature for CONTEXT_ONLY dedup based on statement content."""
    stmt = entity.get("statement", entity.get("rationale", ""))
    # Normalize: lowercase, strip whitespace, remove punctuation
    stmt = re.sub(r'[^\w\s]', '', stmt.lower().strip())
    stmt = re.sub(r'\s+', ' ', stmt)
    # Use first 80 chars as signature (captures the core message)
    tags = frozenset(entity.get("tags", []))
    return (stmt[:80], tags)


def read_all_rules():
    """Read all YAML rule files and return structured data."""
    all_entities = []

    for playlist_dir in sorted(RULES_DIR.iterdir()):
        if not playlist_dir.is_dir() or playlist_dir.name.startswith("_"):
            continue
        playlist = playlist_dir.name
        if playlist not in PLAYLIST_PREFIX:
            continue

        for yml_file in sorted(playlist_dir.glob("*.yml")):
            try:
                with open(yml_file) as f:
                    data = yaml.safe_load(f)
            except Exception as e:
                print(f"ERROR reading {yml_file}: {e}")
                continue

            if not data or "entities" not in data:
                continue

            video_info = data.get("video", {})
            source_file = f"{playlist}/{yml_file.name}"

            for entity in data["entities"]:
                if not isinstance(entity, dict):
                    continue
                entity["_source_file"] = source_file
                entity["_playlist"] = playlist
                entity["_video"] = video_info
                entity["_priority"] = PLAYLIST_PRIORITY.get(playlist, 0)

                # Fix ID prefix
                if "id" in entity:
                    entity["id"] = fix_id_prefix(entity["id"], playlist)

                all_entities.append(entity)

    return all_entities


def deduplicate_entities(entities):
    """Deduplicate entities with same conditions+action signature."""
    sig_groups = defaultdict(list)

    for e in entities:
        sig = conditions_signature(e)
        sig_groups[sig].append(e)

    deduped = []
    for sig, group in sig_groups.items():
        if len(group) == 1:
            winner = group[0]
            winner["_also_in"] = []
        else:
            # Sort by priority (tecniche first), then by condition count (more specific first)
            group.sort(key=lambda x: (
                -x["_priority"],
                -len(x.get("conditions", [])),
                -len(x.get("source", {}).get("timecode", "")),
            ))
            winner = group[0]
            winner["_also_in"] = [
                {"id": e["id"], "source_file": e["_source_file"]}
                for e in group[1:]
            ]
        deduped.append(winner)

    return deduped


def deduplicate_context_entities(entities):
    """Deduplicate CONTEXT_ONLY entities based on statement similarity."""
    sig_groups = defaultdict(list)

    for e in entities:
        sig = context_signature(e)
        sig_groups[sig].append(e)

    deduped = []
    for sig, group in sig_groups.items():
        if len(group) == 1:
            winner = group[0]
            winner["_also_in"] = []
        else:
            group.sort(key=lambda x: (-x["_priority"], -len(x.get("statement", ""))))
            winner = group[0]
            winner["_also_in"] = [
                {"id": e["id"], "source_file": e["_source_file"]}
                for e in group[1:]
            ]
        deduped.append(winner)

    return deduped


def format_entity_for_output(e, include_source_files=True):
    """Format an entity for YAML output, cleaning internal fields."""
    out = {}
    out["id"] = e["id"]
    out["entity_type"] = e.get("entity_type", "RULE")
    out["category"] = e.get("category", "MACHINE_EXECUTABLE")
    out["scope"] = e.get("scope", "TRADE_EXECUTION")
    out["severity"] = e.get("severity", "SOFT")
    out["tags"] = e.get("tags", [])

    if "source" in e:
        out["source"] = e["source"]

    if "conditions" in e:
        out["conditions"] = [fix_condition(c) for c in e["conditions"]]

    if "action" in e:
        out["action"] = e["action"]

    if "stop_loss" in e:
        out["stop_loss"] = e["stop_loss"]
    if "take_profit" in e:
        out["take_profit"] = e["take_profit"]

    if "applicability" in e:
        out["applicability"] = e["applicability"]

    # Normalize to English
    out["rationale"] = e.get("rationale", "")
    out["notes"] = e.get("notes", "")

    if "statement" in e:
        out["statement"] = e["statement"]

    # Source tracking
    out["source_files"] = [e["_source_file"]]
    if e.get("_also_in"):
        out["also_in"] = [x["id"] for x in e["_also_in"]]
        out["source_files"].extend([x["source_file"] for x in e["_also_in"]])

    return out


def get_strategy_from_tags(tags):
    """Extract strategy name from tags."""
    known_strategies = [
        "bomba", "sismografo", "viagra", "ath500", "cambiocolore",
        "rottura10daily", "bicicletta", "ema223", "tf30ema60", "short",
        "rialzi", "shimano", "bud", "terzo_tocco", "candelina",
        "tre_cime", "cambio_candela", "breakout", "finta",
    ]
    if not tags:
        return "general"
    for t in tags:
        if t.lower() in known_strategies:
            return t.lower()
    # Check composite
    for t in tags:
        for s in known_strategies:
            if s in t.lower():
                return s
    return "general"


def sort_key(entity):
    """Sort by strategy, severity (HARD first), scope."""
    scope_order = {"PRE_FILTER": 0, "LIQUIDITY_GATE": 1, "TRADE_EXECUTION": 2, "MONITORING": 3, "GLOBAL": 4}
    strategy = get_strategy_from_tags(entity.get("tags", []))
    severity = 0 if entity.get("severity") == "HARD" else 1
    scope = scope_order.get(entity.get("scope", "TRADE_EXECUTION"), 5)
    return (strategy, severity, scope)


def collect_all_conditions(entities):
    """Collect all unique condition variable names across all entities."""
    variables = set()
    functions = set()
    for e in entities:
        for c in e.get("conditions", []):
            c = fix_condition(c)
            # Extract variable names: things like "variable_name == value" or "variable_name >= value"
            # Match patterns like word_word == value
            var_matches = re.findall(r'([a-z][a-z0-9_]+(?:\([^)]*\))?)\s*(?:==|!=|>=|<=|>|<)', c)
            for v in var_matches:
                v = v.strip()
                if v in ("true", "false", "null"):
                    continue
                if "(" in v:
                    functions.add(v)
                else:
                    variables.add(v)
            # Also catch standalone boolean variables: "variable_name == true"
            bool_matches = re.findall(r'([a-z][a-z0-9_]+)\s*==\s*(?:true|false)', c)
            for v in bool_matches:
                variables.add(v.strip())
    return sorted(variables), sorted(functions)


def main():
    print("Reading all YAML rule files...")
    all_entities = read_all_rules()
    print(f"  Total entities: {len(all_entities)}")

    # Split by category
    machine_exec = [e for e in all_entities if e.get("category") == "MACHINE_EXECUTABLE"]
    context_only = [e for e in all_entities if e.get("category") == "CONTEXT_ONLY"]

    # Reclassify execution mechanics
    reclassified = []
    kept_me = []
    for e in machine_exec:
        if is_execution_mechanics(e):
            e["category"] = "CONTEXT_ONLY"
            e["_reclassified"] = True
            reclassified.append(e)
            context_only.append(e)
        else:
            kept_me.append(e)

    print(f"  MACHINE_EXECUTABLE: {len(kept_me)} (reclassified {len(reclassified)} to CONTEXT_ONLY)")
    print(f"  CONTEXT_ONLY: {len(context_only)}")

    # Deduplicate
    deduped_me = deduplicate_entities(kept_me)
    deduped_ctx = deduplicate_context_entities(context_only)

    print(f"  After dedup - ME: {len(deduped_me)}, CTX: {len(deduped_ctx)}")

    # Sort
    deduped_me.sort(key=sort_key)
    deduped_ctx.sort(key=lambda e: (get_strategy_from_tags(e.get("tags", [])), e.get("id", "")))

    # Format for output
    rules_output = [format_entity_for_output(e) for e in deduped_me]
    context_output = [format_entity_for_output(e) for e in deduped_ctx]

    # Write consolidated-rules.yml
    rules_doc = {
        "metadata": {
            "generated": "2026-03-28",
            "generator": "Claude Opus 4.6",
            "description": "Consolidated MACHINE_EXECUTABLE rules from all 6 playlists, deduplicated and fixed",
            "total_rules": len(rules_output),
            "source_files_count": len(set(e["_source_file"] for e in kept_me)),
        },
        "rules": rules_output,
    }

    rules_path = RULES_DIR / "consolidated-rules.yml"
    with open(rules_path, "w") as f:
        yaml.dump(rules_doc, f, default_flow_style=False, allow_unicode=True, sort_keys=False, width=120)
    print(f"  Written: {rules_path}")

    # Write consolidated-context.yml
    context_doc = {
        "metadata": {
            "generated": "2026-03-28",
            "generator": "Claude Opus 4.6",
            "description": "Consolidated CONTEXT_ONLY entities for RAG system, deduplicated",
            "total_entities": len(context_output),
        },
        "entities": context_output,
    }

    context_path = RULES_DIR / "consolidated-context.yml"
    with open(context_path, "w") as f:
        yaml.dump(context_doc, f, default_flow_style=False, allow_unicode=True, sort_keys=False, width=120)
    print(f"  Written: {context_path}")

    # Generate RULES_INDEX.md
    # Stats
    strategies = defaultdict(int)
    scopes = defaultdict(int)
    severities = defaultdict(int)
    all_strats = set()

    for e in rules_output:
        strat = get_strategy_from_tags(e.get("tags", []))
        strategies[strat] += 1
        all_strats.add(strat)
        scopes[e.get("scope", "unknown")] += 1
        severities[e.get("severity", "unknown")] += 1

    # Collect all conditions
    all_variables, all_functions = collect_all_conditions(rules_output)

    # Evidence mapping
    evidence_dir = KNOWLEDGE_DIR / "evidence"
    evidence_files = []
    evidence_mapping = []
    if evidence_dir.exists():
        evidence_files = sorted(f.name for f in evidence_dir.iterdir() if f.is_file())
        for ef in evidence_files:
            matched_rules = []
            ef_lower = ef.lower()
            for r in rules_output:
                for tag in r.get("tags", []):
                    if tag.lower() in ef_lower:
                        matched_rules.append(r["id"])
                        break
            if matched_rules:
                evidence_mapping.append((ef, matched_rules))

    # Write RULES_INDEX.md
    index_lines = []
    index_lines.append("# Rules Index")
    index_lines.append("")
    index_lines.append(f"Generated: 2026-03-28 | Generator: Claude Opus 4.6")
    index_lines.append("")
    index_lines.append(f"**Total MACHINE_EXECUTABLE rules:** {len(rules_output)}")
    index_lines.append(f"**Total CONTEXT_ONLY entities:** {len(context_output)}")
    index_lines.append("")

    index_lines.append("## Rules by Strategy")
    index_lines.append("")
    index_lines.append("| Strategy | Count |")
    index_lines.append("|----------|-------|")
    for s in sorted(strategies.keys()):
        index_lines.append(f"| {s} | {strategies[s]} |")
    index_lines.append("")

    index_lines.append("## Rules by Scope")
    index_lines.append("")
    index_lines.append("| Scope | Count |")
    index_lines.append("|-------|-------|")
    for s in sorted(scopes.keys()):
        index_lines.append(f"| {s} | {scopes[s]} |")
    index_lines.append("")

    index_lines.append("## Rules by Severity")
    index_lines.append("")
    index_lines.append("| Severity | Count |")
    index_lines.append("|----------|-------|")
    for s in sorted(severities.keys()):
        index_lines.append(f"| {s} | {severities[s]} |")
    index_lines.append("")

    index_lines.append("## All Unique Strategies")
    index_lines.append("")
    for s in sorted(all_strats):
        index_lines.append(f"- `{s}`")
    index_lines.append("")

    index_lines.append("## All Unique Conditions (for Rule Engine Evaluator)")
    index_lines.append("")
    index_lines.append("### Boolean / Numeric Variables")
    index_lines.append("")
    index_lines.append(f"Total: {len(all_variables)} unique variables")
    index_lines.append("")
    index_lines.append("```")
    for v in all_variables:
        index_lines.append(v)
    index_lines.append("```")
    index_lines.append("")
    index_lines.append("### Functions")
    index_lines.append("")
    index_lines.append(f"Total: {len(all_functions)} unique functions")
    index_lines.append("")
    index_lines.append("```")
    for f in all_functions:
        index_lines.append(f)
    index_lines.append("```")
    index_lines.append("")

    index_lines.append("## Evidence Mapping")
    index_lines.append("")
    if evidence_mapping:
        index_lines.append("| Evidence File | Matched Rules |")
        index_lines.append("|--------------|---------------|")
        for ef, rules in evidence_mapping:
            index_lines.append(f"| `{ef}` | {', '.join(rules)} |")
    else:
        index_lines.append("No `knowledge/evidence/` directory found. Evidence mapping will be populated when evidence files are added.")
    index_lines.append("")

    index_path = RULES_DIR / "RULES_INDEX.md"
    with open(index_path, "w") as f:
        f.write("\n".join(index_lines))
    print(f"  Written: {index_path}")

    print("Done!")


if __name__ == "__main__":
    main()
