package com.trabot.business.service;

import java.util.Set;

import com.trabot.persistance.model.entities.InstantPrice;

public interface PriceQueueForDb {
    
    void append(Set<InstantPrice> prices);

}
