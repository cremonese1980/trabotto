package com.trabot.business.exchange.okx;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabot.business.exchange.ExchangeManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OkxManager implements ExchangeManager{//usa

//    Chiave API 69401f97-f199-4a00-a83b-c55a2725c10d
//    Chiave segreta 90722852577ADEEEB90B18DD0BE537A0
    // GET /api/v5/market/tickers
    // HEADERS
    // OK-ACCESS-KEY = API KEY
//    OK-ACCESS-SIGN 
//    OK-ACCESS-TIMESTAMP 
//    OK-ACCESS-PASSPHRASE L4f0rz4d3lqu4d3rn0!

    // sign Create a prehash string of timestamp + method + requestPath + body
    // (where + represents String concatenation).

    // sign=CryptoJS.enc.Base64.stringify(CryptoJS.HmacSHA256(timestamp + 'GET' +
    // '/api/v5/account/balance?ccy=BTC', SecretKey))

    private static final String SECRET_KEY = "90722852577ADEEEB90B18DD0BE537A0";
    private static final String API_KEY = "69401f97-f199-4a00-a83b-c55a2725c10d";
    private static final String PASS_PHRASE = "L4f0rz4d3lqu4d3rn0!";

    private static final String PEPE = "PEPE-USDT";

    private static final String BASE_URL = "https://www.okx.com";
    private static final String ORDER_URL = "/api/v5/trade/order";
    private static final String TICKER_URL = "/api/v5/market/ticker";
    private static final String ORDER_BATCH_URL = "/api/v5/trade/batch-orders";
    private static final String MARKET_DATA_URL = "/api/v5/market/books";
    

    private static final String API_URL = "api/v5";
    private static final String GET_SYMBOL_PRICE_TICKER_URL = "?";
    

    public void placeOrder(Position position) throws ClientProtocolException, IOException {

	Order order = new Order(position.getSymbol(), position.getTMode().getValue(), position.getSide().getOkxValue(),
		position.getTradeType().getOkxValue(), position.getVolume().toString(), position.getPrice().toString());

	ObjectMapper objectMapper = new ObjectMapper();
	String jsonBody = objectMapper.writeValueAsString(order);

	TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no e
									      // offset
	df.setTimeZone(tz);
	String nowAsISO = df.format(new Date());

	String valueToDigest = nowAsISO + "POST" + ORDER_URL + jsonBody;
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + ORDER_URL;

	Map<String, String> headers = new HashMap<>();
	headers.put("OK-ACCESS-KEY", API_KEY);
	headers.put("OK-ACCESS-SIGN", messageDigest);
	headers.put("OK-ACCESS-TIMESTAMP", nowAsISO);
	headers.put("OK-ACCESS-PASSPHRASE", PASS_PHRASE);

//	System.out.println(headers.toString());
	System.out.println("*********************************** body:" + jsonBody);

	callApiWithPost(targetURL, jsonBody, headers);
    }
    
    public void placeOrdersBatch(List<Position> positions) throws ClientProtocolException, IOException {
	
	List<Order> orders = positions.stream()
		.map(position -> new Order(position.getSymbol(), position.getTMode().getValue(),
			position.getSide().getOkxValue(), position.getTradeType().getOkxValue(),
			position.getVolume().toString(), position.getPrice().toString()))
		.collect(Collectors.toList());

	ObjectMapper objectMapper = new ObjectMapper();
	String jsonBody = objectMapper.writeValueAsString(orders);

	TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no e
									      // offset
	df.setTimeZone(tz);
	String nowAsISO = df.format(new Date());

	String valueToDigest = nowAsISO + "POST" + ORDER_BATCH_URL + jsonBody;
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + ORDER_BATCH_URL;

	Map<String, String> headers = new HashMap<>();
	headers.put("OK-ACCESS-KEY", API_KEY);
	headers.put("OK-ACCESS-SIGN", messageDigest);
	headers.put("OK-ACCESS-TIMESTAMP", nowAsISO);
	headers.put("OK-ACCESS-PASSPHRASE", PASS_PHRASE);

//	System.out.println(headers.toString());
	System.out.println("*********************************** body:" + jsonBody);

	callApiWithPost(targetURL, jsonBody, headers);
    }
    
    public void getTikcer(String symbol) throws IOException {

	TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no e
									      // offset
	df.setTimeZone(tz);
	String nowAsISO = df.format(new Date());

	String valueToDigest = nowAsISO + "GET" + TICKER_URL + "?instId=" + symbol;
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + TICKER_URL + "?instId=" + symbol;

	Map<String, String> headers = new HashMap<>();
	headers.put("OK-ACCESS-KEY", API_KEY);
	headers.put("OK-ACCESS-SIGN", messageDigest);
	headers.put("OK-ACCESS-TIMESTAMP", nowAsISO);
	headers.put("OK-ACCESS-PASSPHRASE", PASS_PHRASE);
	
	callApiWithGet(targetURL, headers);
     }
    
    public void getMarketData(String symbol) throws IOException {

	TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no e
									      // offset
	df.setTimeZone(tz);
	String nowAsISO = df.format(new Date());

	String valueToDigest = nowAsISO + "GET" + MARKET_DATA_URL + "?instId=" + symbol + "&sz=100";
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + MARKET_DATA_URL + "?instId=" + symbol + "&sz=100";

	Map<String, String> headers = new HashMap<>();
	headers.put("OK-ACCESS-KEY", API_KEY);
	headers.put("OK-ACCESS-SIGN", messageDigest);
	headers.put("OK-ACCESS-TIMESTAMP", nowAsISO);
	headers.put("OK-ACCESS-PASSPHRASE", PASS_PHRASE);
	
	callApiWithGet(targetURL, headers);
     }

    
    private void callApiWithPost(String url, String body, Map<String, String> headers) throws IOException {

	CloseableHttpClient httpClient = HttpClients.createDefault();
	HttpPost httpPost = new HttpPost(url);

	// Aggiungi headers personalizzati
	httpPost.setHeader("Content-Type", "application/json");
//        httpPost.setHeader("Custom-Header", "custom-value");

	for (Map.Entry<String, String> header : headers.entrySet()) {
	    httpPost.setHeader(header.getKey(), header.getValue());
	}

	// Imposta il body JSON
	StringEntity entity = new StringEntity(body);
	httpPost.setEntity(entity);

	// Esegui la chiamata POST e ottieni la risposta
	HttpResponse response = httpClient.execute(httpPost);

	// Stampa la risposta
	int statusCode = response.getStatusLine().getStatusCode();
	HttpEntity responseEntity = response.getEntity();
	String responseBody = EntityUtils.toString(responseEntity);

	System.out.println("Status Code: " + statusCode);
	System.out.println("Response Body: " + responseBody);

	httpClient.close();
    }
    
    private void callApiWithGet(String url, Map<String, String> headers) throws IOException {

	CloseableHttpClient httpClient = HttpClients.createDefault();
	HttpGet httpPost = new HttpGet(url);

	// Aggiungi headers personalizzati
	httpPost.setHeader("Content-Type", "application/json");
//        httpPost.setHeader("Custom-Header", "custom-value");

	for (Map.Entry<String, String> header : headers.entrySet()) {
	    httpPost.setHeader(header.getKey(), header.getValue());
	}

	// Esegui la chiamata POST e ottieni la risposta
	HttpResponse response = httpClient.execute(httpPost);

	// Stampa la risposta
	int statusCode = response.getStatusLine().getStatusCode();
	HttpEntity responseEntity = response.getEntity();
	String responseBody = EntityUtils.toString(responseEntity);

	System.out.println("Status Code: " + statusCode);
	System.out.println("Response Body: " + responseBody);

	httpClient.close();
    }


    private String generateHmac256(String message) {
	try {
	    byte[] bytes = hmac("HmacSHA256", SECRET_KEY.getBytes(), message.getBytes());

	    // base64 encode
	    Encoder codec = Base64.getEncoder();
	    String b64Str = codec.encodeToString(bytes);

//	    System.out.println("b64str " + b64Str);

	    // url encode
//	    String signature = URLEncoder.encode(b64Str);
//	    System.out.println("signature " + signature);
	    return b64Str;

	} catch (Exception e) {
	    System.out.println("generateHmac256 expection:" + e);
	}
	return "";
    }

    private byte[] hmac(String algorithm, byte[] key, byte[] message)
	    throws NoSuchAlgorithmException, InvalidKeyException {
	Mac mac = Mac.getInstance(algorithm);
	mac.init(new SecretKeySpec(key, algorithm));
	return mac.doFinal(message);
    }

    @Override
    public Set<CryptoPrice> readPriceList(Set<String> symbols) {
	throw new NotYetImplementedException();
    }

    @Override
    public Map<String, String> buildGetParamsFromSymbols(Set<String> symbols) {
	throw new NotYetImplementedException();
    }

    @Override
    public String getBaseUrl() {
	return BASE_URL;
    }

    @Override
    public String getApiUrl() {
	// TODO Auto-generated method stub
	return API_URL;
    }

    @Override
    public void history(String symbol) {

	
	TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no e
									      // offset
	df.setTimeZone(tz);
	String nowAsISO = df.format(new Date());

	String valueToDigest = nowAsISO + "GET" + "/api/v5/market/history-trades" + "?instId=" + symbol;
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + "/api/v5/market/history-trades" + "?instId=" + symbol;

	Map<String, String> headers = new HashMap<>();
	headers.put("OK-ACCESS-KEY", API_KEY);
	headers.put("OK-ACCESS-SIGN", messageDigest);
	headers.put("OK-ACCESS-TIMESTAMP", nowAsISO);
	headers.put("OK-ACCESS-PASSPHRASE", PASS_PHRASE);
	
	try {
	    callApiWithGet(targetURL, headers);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
