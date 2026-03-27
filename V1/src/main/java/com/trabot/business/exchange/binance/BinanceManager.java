package com.trabot.business.exchange.binance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabot.business.exchange.ExchangeManager;
import com.trabot.business.httpclient.HttpClient;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BinanceManager implements ExchangeManager {

    // VBZUZGUDZXKP3VTU

    // API_KEY zcMHtAue65ca9Z6HJ1OD9y6vJpb7xBRX2n6744PMh64cMgDAoluPcOetg3UGjVCN
    // secret 07IjPG9Jd69aTw51Tftp1JY7TB46MBPnJxIfudqMhNDm6Xsroguevw42Ubu0Ral5

    public static final String LITERAL_KEY = "binance";

    private static final String BASE_URL = "https://api.binance.com";
    private static final String API_URL = "api/v3";
    private static final String GET_SYMBOL_PRICE_TICKER_URL = "ticker/price";
    
    private static final String COMMA = ",";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String OPEN_SQUARE_BRACKET = "[";
    private static final String CLOSE_SQUARE_BRACKET = "]";
    private static final String SYMBOLS = "symbols";


    private final HttpClient httpClient;

    @Override
    public Set<CryptoPrice> readPriceList(Set<String> symbols) {
//    https://api.binance.com/api/v3/ticker/price?symbols=["BTCUSDT","ETHUSDT"]

	Map<String, String> params = buildGetParamsFromSymbols(symbols);

	String url = buildUrl(GET_SYMBOL_PRICE_TICKER_URL);

	String response = httpClient.getRequest(url, params);
	
	ObjectMapper mapper = new ObjectMapper();
        Set<CryptoPrice> result = null;
	try {
	    result = mapper.readValue(response, new TypeReference<Set<CryptoPrice>>(){});
	} catch (JsonProcessingException e) {
	    log.error(e.getMessage(),e);
	}

	return result;
    }

    @Override
    public String getBaseUrl() {
	return BASE_URL;
    }

    @Override
    public String getApiUrl() {
	return API_URL;
    }

    @Override
    public Map<String, String> buildGetParamsFromSymbols(Set<String> symbols) {

	Map<String, String> params = new HashMap<String, String>();

	String symbolsValue = symbols.stream()
		.map(symbol -> String.format("%s%s%s", DOUBLE_QUOTE, symbol, DOUBLE_QUOTE))
		.collect(Collectors.joining(COMMA, OPEN_SQUARE_BRACKET, CLOSE_SQUARE_BRACKET));

	params.put(SYMBOLS, symbolsValue);
	return params;

    }

    @Override
    public void placeOrdersBatch(List<Position> positions) throws ClientProtocolException, IOException {
	throw new NotYetImplementedException();
    }

    @Override
    public void getMarketData(String stockUsdt) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void history(String stockUsdt) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void placeOrder(Position position) throws ClientProtocolException, IOException {
	// TODO Auto-generated method stub
	
    }

}
