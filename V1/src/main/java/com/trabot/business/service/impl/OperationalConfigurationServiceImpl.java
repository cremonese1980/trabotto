package com.trabot.business.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trabot.business.service.OperationalConfigurationService;
import com.trabot.persistance.model.entities.OperationalConfiguration;
import com.trabot.persistance.repository.OperationalConfigurationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperationalConfigurationServiceImpl implements OperationalConfigurationService {
    

    private static final String PRICE_SYMBOLS = "price.symbols";
    private static final String ANALYSER_PERIOD = "price.analyser.period";
    private static final String SEMICOLON = ";";

    private final OperationalConfigurationRepository operationalConfigurationRepository;

    private List<OperationalConfiguration> table;
    private Set<String> priceReaderSymbols;
    private int analyserPeriod = 0;

    @Override
    public String getValue(String key) {

	if (table == null) {
	    refreshFromDB();
	}

	return table.get(table.indexOf(new OperationalConfiguration(key))).getValue();
    }

    @Override
    public void refreshFromDB() {
	table = operationalConfigurationRepository.findAll();

    }

    @Override
    public Set<String> getPriceReaderSymbols() {

	if (priceReaderSymbols == null) {

	    String[] symbolArray = getValue(PRICE_SYMBOLS).split(SEMICOLON);

	    priceReaderSymbols = Arrays.stream(symbolArray).collect(Collectors.toSet());
	}

	return priceReaderSymbols;
    }
    

    @Override
    public int getAnalyserPeriod() {
	if(analyserPeriod == 0) {
	    String value = getValue(ANALYSER_PERIOD);
	    analyserPeriod = Integer.parseInt(value);
	}
	
	return analyserPeriod;
    }

}
