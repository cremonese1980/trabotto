"""Smoke tests for telegram_listener configuration validation."""

import os
import sys
from pathlib import Path

import pytest

PROJECT_SRC = Path(__file__).resolve().parents[1] / "src"
sys.path.insert(0, str(PROJECT_SRC))


@pytest.fixture

def telegram_listener_module(monkeypatch):
    """Import telegram_listener with fake external dependencies for test isolation."""
    import types

    fake_dotenv = types.ModuleType("dotenv")
    fake_dotenv.load_dotenv = lambda: None
    monkeypatch.setitem(sys.modules, "dotenv", fake_dotenv)

    fake_telethon = types.ModuleType("telethon")
    fake_telethon.TelegramClient = object
    fake_events = types.ModuleType("events")
    fake_telethon.events = fake_events
    monkeypatch.setitem(sys.modules, "telethon", fake_telethon)

    fake_kafka = types.ModuleType("kafka_producer")
    fake_kafka.publish_signal = lambda signal: None
    monkeypatch.setitem(sys.modules, "kafka_producer", fake_kafka)

    import importlib

    monkeypatch.setenv("TELEGRAM_API_ID", "")
    monkeypatch.setenv("TELEGRAM_API_HASH", "")

    module = importlib.import_module("telegram_listener")
    module = importlib.reload(module)
    return module


def test_validate_config_exits_when_missing_values(telegram_listener_module, monkeypatch):
    monkeypatch.setattr(telegram_listener_module, "API_ID", None)
    monkeypatch.setattr(telegram_listener_module, "API_HASH", None)

    with pytest.raises(SystemExit) as exc_info:
        telegram_listener_module.validate_config()

    assert exc_info.value.code == 1


def test_validate_config_passes_when_present(telegram_listener_module, monkeypatch):
    monkeypatch.setattr(telegram_listener_module, "API_ID", "123456")
    monkeypatch.setattr(telegram_listener_module, "API_HASH", "hash")

    telegram_listener_module.validate_config()
