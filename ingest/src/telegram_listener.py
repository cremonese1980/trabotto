#!/usr/bin/env python3
"""
Trabotto — Telegram Signal Ingest (Proof of Concept)

Connects to Telegram using YOUR account (Client API, not Bot API),
listens to the BullBot channel, and logs every message.

This is a PoC — no normalization, no Kafka, just prove we can read the channel.

First run will ask for your phone number and a verification code from Telegram.
After that, a session file is saved and you won't need to re-authenticate.

Setup:
  1. Go to https://my.telegram.org/apps and create an application
  2. You'll get an api_id (number) and api_hash (string)
  3. Create a .env file in this directory (see .env.example)
  4. pip install telethon python-dotenv
  5. python telegram_listener.py

Requirements:
  pip install telethon python-dotenv
"""

import asyncio
import logging
import os
import sys
from datetime import datetime
from pathlib import Path

from dotenv import load_dotenv
from telethon import TelegramClient, events

# Load config from .env
load_dotenv()

API_ID = os.getenv("TELEGRAM_API_ID")
API_HASH = os.getenv("TELEGRAM_API_HASH")
CHANNEL_NAME = os.getenv("TELEGRAM_CHANNEL", "BullBot - Tutti i Segnali")

# Session file persists auth between runs
SESSION_FILE = "trabotto_telegram_session"

# Log file for captured messages (append mode)
LOG_DIR = Path("knowledge/telegram_raw")
LOG_FILE = LOG_DIR / f"raw_messages_{datetime.now().strftime('%Y%m%d')}.log"

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler(sys.stdout),
    ]
)
logger = logging.getLogger("trabotto.ingest")


def validate_config():
    """Check that required env vars are set."""
    if not API_ID or not API_HASH:
        logger.error(
            "Missing TELEGRAM_API_ID and/or TELEGRAM_API_HASH.\n"
            "1. Go to https://my.telegram.org/apps\n"
            "2. Create an application\n"
            "3. Create a .env file with:\n"
            "   TELEGRAM_API_ID=your_id\n"
            "   TELEGRAM_API_HASH=your_hash\n"
            "   TELEGRAM_CHANNEL=BullBot - Tutti i Segnali"
        )
        sys.exit(1)


async def find_channel(client: TelegramClient, channel_name: str):
    """Find the BullBot channel among your dialogs."""
    logger.info(f"Searching for channel: '{channel_name}'")

    async for dialog in client.iter_dialogs():
        if channel_name.lower() in dialog.name.lower():
            logger.info(f"Found channel: '{dialog.name}' (id: {dialog.id})")
            return dialog.entity

    # If not found by name, list all channels/groups for debugging
    logger.warning(f"Channel '{channel_name}' not found. Listing all channels/groups:")
    async for dialog in client.iter_dialogs():
        if dialog.is_channel or dialog.is_group:
            logger.info(f"  - '{dialog.name}' (id: {dialog.id})")

    return None


def log_message_to_file(message):
    """Append raw message to daily log file."""
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        timestamp = message.date.strftime("%Y-%m-%d %H:%M:%S UTC")
        f.write(f"[{timestamp}] ID:{message.id}\n")
        f.write(f"{message.text or '[no text — possibly media only]'}\n")
        f.write(f"---\n")


async def main():
    validate_config()

    client = TelegramClient(SESSION_FILE, int(API_ID), API_HASH)
    await client.start()

    logger.info("Connected to Telegram successfully.")

    # Find the BullBot channel
    channel = await find_channel(client, CHANNEL_NAME)

    if not channel:
        logger.error(f"Could not find channel '{CHANNEL_NAME}'. Make sure you're a member.")
        await client.disconnect()
        sys.exit(1)

    # Log last 5 messages as proof it works
    logger.info("=== Last 5 messages from channel ===")
    async for message in client.iter_messages(channel, limit=5):
        timestamp = message.date.strftime("%Y-%m-%d %H:%M:%S")
        text = message.text or "[media/no text]"
        logger.info(f"[{timestamp}] {text[:200]}")
        log_message_to_file(message)

    # Now listen for new messages in real-time
    @client.on(events.NewMessage(chats=channel))
    async def handler(event):
        timestamp = event.message.date.strftime("%Y-%m-%d %H:%M:%S")
        text = event.message.text or "[media/no text]"
        logger.info(f"NEW SIGNAL [{timestamp}]: {text[:300]}")
        log_message_to_file(event.message)

    logger.info(f"\nListening for new messages on '{CHANNEL_NAME}'...")
    logger.info("Press Ctrl+C to stop.\n")

    await client.run_until_disconnected()


if __name__ == "__main__":
    asyncio.run(main())
