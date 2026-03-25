package com.trabotto.domain.port;

import com.trabotto.domain.model.RawSignal;
import java.util.function.Consumer;

/**
 * Port for signal ingestion adapters.
 */
public interface SignalSourcePort {

    String sourceId();

    String associatedRuleSetId();

    void subscribe(Consumer<RawSignal> signalConsumer);

    boolean isAvailable();
}
