package com.trabot.business.service;

import java.util.Set;

import com.trabot.persistance.model.pojo.CryptoPrice;

public interface PriceReader {
    
    Set<CryptoPrice> readPriceList(Set<String> symbols);

}
