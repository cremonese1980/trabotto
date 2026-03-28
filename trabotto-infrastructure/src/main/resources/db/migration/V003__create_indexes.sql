CREATE INDEX idx_shadow_strategy ON shadow_trades(strategy, outcome);
CREATE INDEX idx_shadow_confidence ON shadow_trades(confidence, outcome);

CREATE INDEX idx_decisions_pair_strategy ON decisions(pair, strategy);
CREATE INDEX idx_decisions_embedding ON decisions USING ivfflat (embedding vector_cosine_ops);
