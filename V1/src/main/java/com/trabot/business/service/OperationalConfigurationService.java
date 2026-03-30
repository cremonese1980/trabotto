package com.trabot.business.service;

import java.util.Set;

public interface OperationalConfigurationService {
    
    String getValue(String key);
    
    void refreshFromDB();
    
    Set<String> getPriceReaderSymbols();

    int getAnalyserPeriod();

}
