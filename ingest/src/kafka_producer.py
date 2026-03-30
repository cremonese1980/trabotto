"""Kafka producer utilities for publishing normalized signals."""

import json
import logging
import os
from typing import Any

from confluent_kafka import Producer

logger = logging.getLogger("trabotto.ingest.kafka")

KAFKA_BROKER = os.getenv("KAFKA_BROKER", "localhost:9092")
TOPIC = "signals.incoming"


def _build_producer() -> Producer:
    """Create and return a Kafka producer instance."""
    return Producer({"bootstrap.servers": KAFKA_BROKER})


PRODUCER = _build_producer()


def _delivery_report(err: Any, msg: Any) -> None:
    """Kafka delivery callback used for debug logging."""
    if err is not None:
        logger.error("Kafka delivery failed for key=%s: %s", msg.key(), err)
        return

    logger.debug(
        "Kafka message delivered topic=%s partition=%s offset=%s",
        msg.topic(),
        msg.partition(),
        msg.offset(),
    )


def publish_signal(signal: dict) -> None:
    """Publish a normalized signal dict to Kafka."""
    key = signal["pair"]
    value = json.dumps(signal)

    PRODUCER.produce(TOPIC, key=key, value=value, callback=_delivery_report)
    PRODUCER.poll(0)
    PRODUCER.flush(1)
