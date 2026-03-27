package com.trabot.business.factory;

import org.springframework.stereotype.Service;

import com.trabot.business.exchange.binance.BinanceManager;
import com.trabot.business.exchange.bingx.BingXManager;
import com.trabot.business.service.OperationalConfigurationService;
import com.trabot.business.service.PriceReader;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PriceReaderFactory {
    
    private final OperationalConfigurationService operationalConfigurationService;
    private final PriceReader binancePriceReader;
    private final PriceReader bingxPriceReader;
    
    private String selectedPriceReaderLiteral;
    private PriceReader selectedPriceReader;
    
    public PriceReader getPriceReader() {
	
	if(selectedPriceReaderLiteral == null) {
	    
	    selectedPriceReaderLiteral = operationalConfigurationService.getValue("price.reader");
	}
	
	if(selectedPriceReader == null) {
	    
	    if (BinanceManager.LITERAL_KEY .equalsIgnoreCase(selectedPriceReaderLiteral)) {
		selectedPriceReader = binancePriceReader;
		
	    } else if (BingXManager.LITERAL_KEY.equalsIgnoreCase(selectedPriceReaderLiteral)) {
		selectedPriceReader =  bingxPriceReader;
		
	    } else {
		throw new IllegalArgumentException("Invalid price reader value");
	    }
	}
	
	return selectedPriceReader;

    }

}
