CREATE TABLE decisions (
    id               UUID PRIMARY KEY,
    "timestamp"      TIMESTAMPTZ NOT NULL,
    signal_id        VARCHAR NOT NULL,
    pair             VARCHAR NOT NULL,
    timeframe        VARCHAR NOT NULL,
    strategy         VARCHAR NOT NULL,
    matched_rules    JSONB NOT NULL,
    rule_verdict     VARCHAR NOT NULL,
    ai_advisory      JSONB,
    policy_result    JSONB NOT NULL,
    final_action     VARCHAR NOT NULL,
    rejection_reason VARCHAR,
    order_result     JSONB,
    trade_outcome    JSONB,
    ruleset_version  VARCHAR NOT NULL,
    embedding        vector(384)
);
