package com.trabot.business.service;

import java.util.Set;

import com.trabot.persistance.model.entities.InstantPrice;
import com.trabot.persistance.model.pojo.CryptoPrice;

public interface WindowTimePriceManager {
    
    Set<InstantPrice> pushLastPrices(long timestamp, Set<CryptoPrice> cryptoPrices);
    
    void init(Set<String> symbols, int analyserPeriod);

}
