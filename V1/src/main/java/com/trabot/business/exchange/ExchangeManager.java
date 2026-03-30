package com.trabot.business.exchange;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.pojo.CryptoPrice;

public interface ExchangeManager {
    
    Set<CryptoPrice> readPriceList(Set<String> symbols);
    
    Map<String, String> buildGetParamsFromSymbols(Set<String> symbols);
    
    String getBaseUrl();
    
    String getApiUrl();
    
    default String buildUrl(String endpointUrl) {
	
	return String.format("%s/%s/%s", getBaseUrl(), getApiUrl(), endpointUrl);
	
    }

    void placeOrdersBatch(List<Position> positions) throws ClientProtocolException, IOException;
    
    void placeOrder(Position position) throws ClientProtocolException, IOException;

    void getMarketData(String stockUsdt)throws IOException;

    void history(String stockUsdt);
    
    default String nowAsIso() {
	return String.valueOf(Instant.now().toEpochMilli());
    }
    

}
