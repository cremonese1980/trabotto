package com.trabot.business.exchange.bingx;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.trabot.business.service.PriceReader;
import com.trabot.persistance.model.pojo.CryptoPrice;

@Service(value = "bingxPriceReader")
public class BingxPriceReader implements PriceReader {

    @Override
    public Set<CryptoPrice> readPriceList(Set<String> symbols) {
	// TODO Auto-generated method stub
	return null;
    }

}
