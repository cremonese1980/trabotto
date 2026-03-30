package com.trabot.business.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trabot.business.service.WindowTimePrice;
import com.trabot.business.service.WindowTimePriceManager;
import com.trabot.persistance.model.entities.InstantPrice;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WindowTimePriceManagerImpl implements WindowTimePriceManager {

    private final Map<String, WindowTimePrice> priceMap = new ConcurrentHashMap<String, WindowTimePrice>();

    @Override
    public Set<InstantPrice> pushLastPrices(long timestamp, Set<CryptoPrice> cryptoPrices) {

	return cryptoPrices.parallelStream().map(cryptoPrice -> {
	    try {
		return priceMap.get(cryptoPrice.getSymbol()).addPrice(cryptoPrice, timestamp);
		
	    } catch (InterruptedException e) {
		log.error(e.getMessage(),e);
		return null;
	    }
	    
	}).collect(Collectors.toSet());

    }

    @Override
    public void init(Set<String> symbols, int analyserPeriod) {

	symbols.stream().forEach(symbol -> {
	    priceMap.put(symbol, new WindowTimePrice(analyserPeriod));
	});
    }

}
