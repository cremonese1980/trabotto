package com.trabot.business.process.impl;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.trabot.business.factory.PriceReaderFactory;
import com.trabot.business.process.PriceReaderProcess;
import com.trabot.business.service.OperationalConfigurationService;
import com.trabot.business.service.PriceQueueForDb;
import com.trabot.business.service.PriceReader;
import com.trabot.business.service.WindowTimePriceManager;
import com.trabot.persistance.model.entities.InstantPrice;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PriceReaderProcessImpl implements PriceReaderProcess {

    private final OperationalConfigurationService operationalConfigurationService;
    private final PriceReaderFactory priceReaderFactory;
    private final WindowTimePriceManager windowTimePriceManager;
    private final PriceQueueForDb priceQueueForDb;

    private PriceReader priceReader;
    private Set<String> symbols;
    private int analyserPeriod;

    public PriceReaderProcessImpl(OperationalConfigurationService operationalConfigurationService,
	    PriceReaderFactory priceReaderFactory, WindowTimePriceManager windowTimePriceManager,
	    PriceQueueForDb priceQueueForDb) {

	this.operationalConfigurationService = operationalConfigurationService;
	this.priceReaderFactory = priceReaderFactory;
	this.windowTimePriceManager = windowTimePriceManager;
	this.priceQueueForDb = priceQueueForDb;

//	init();
    }

    @Override
    public void ingestPrice(long timestamp) {

	Set<CryptoPrice> prices = priceReader.readPriceList(symbols);

	Set<InstantPrice> instantPrices = windowTimePriceManager.pushLastPrices(timestamp, prices);

	priceQueueForDb.append(instantPrices);
	
	log.info("read prices {}", prices);

    }

    private void init() {
	
	try {
	    
	    priceReader = priceReaderFactory.getPriceReader();
	    
	    symbols = operationalConfigurationService.getPriceReaderSymbols();
	    
	    analyserPeriod = operationalConfigurationService.getAnalyserPeriod();
	    
	    windowTimePriceManager.init(symbols, analyserPeriod);
	    
	}catch(Exception e) {
	    
	    log.error(e.getMessage(), e);
	}


    }

}
