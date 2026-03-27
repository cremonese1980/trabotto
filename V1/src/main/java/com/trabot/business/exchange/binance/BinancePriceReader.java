package com.trabot.business.exchange.binance;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.trabot.business.exchange.ExchangeManager;
import com.trabot.business.service.PriceReader;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service(value = "binancePriceReader")
@RequiredArgsConstructor
public class BinancePriceReader implements PriceReader {
    
    private final ExchangeManager binanceManager;
    

    @Override
    public Set<CryptoPrice> readPriceList(Set<String> symbols){

	return binanceManager.readPriceList(symbols);
	
    }

}
