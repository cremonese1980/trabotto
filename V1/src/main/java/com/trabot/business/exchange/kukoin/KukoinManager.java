package com.trabot.business.exchange.kukoin;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabot.business.exchange.ExchangeManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TMode;
import com.trabot.persistance.model.enums.TradeType;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KukoinManager implements ExchangeManager {// usa

    private static final String SECRET_KEY = "a6d33e25-8d25-4351-91a1-c8036fd9a91a";
    private static final String API_KEY = "64516df1fcb07a0001e2bafd";
    private static final String PASS_PHRASE = "L4f0rz4d3lqu4d3rn0!";

    private static final String BASE_URL = "https://api.kucoin.com";
    private static final String API_URL = "/api/v1";
    private static final String ORDER_URL = "/api/v1/orders";
    private static final String ORDER_BATCH_URL = "/api/v1/orders/multi";

    public void placeOrder(Position position) throws ClientProtocolException, IOException {

	Order order = positionToOrder(position);

	ObjectMapper objectMapper = new ObjectMapper();
	String jsonBody = objectMapper.writeValueAsString(order);

	String nowAsISO = nowAsIso();

	String valueToDigest = nowAsISO + "POST" + ORDER_URL + jsonBody;
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + ORDER_URL;

	Map<String, String> headers = new HashMap<>();
	headers.put("KC-API-KEY", API_KEY);
	headers.put("KC-API-SIGN", messageDigest);
	headers.put("KC-API-TIMESTAMP", nowAsISO);
	headers.put("KC-API-PASSPHRASE", generateHmac256(PASS_PHRASE));
	headers.put("KC-API-KEY-VERSION", "2");

	callApiWithPost(targetURL, jsonBody, headers);
    }

    public void placeOrdersBatch(List<Position> positions) throws ClientProtocolException, IOException {

	List<Order> orderList = positions.stream().map(this::positionToOrder).collect(Collectors.toList());

	Orders orders = new Orders(positions.get(0).getSymbol(), orderList);

	ObjectMapper objectMapper = new ObjectMapper();
	String jsonBody = objectMapper.writeValueAsString(orders);

	String nowAsISO = nowAsIso();

	String valueToDigest = nowAsISO + "POST" + ORDER_BATCH_URL + jsonBody;
	String messageDigest = generateHmac256(valueToDigest);
	String targetURL = BASE_URL + ORDER_BATCH_URL;

	Map<String, String> headers = new HashMap<>();
	headers.put("KC-API-KEY", API_KEY);
	headers.put("KC-API-SIGN", messageDigest);
	headers.put("KC-API-TIMESTAMP", nowAsISO);
	headers.put("KC-API-PASSPHRASE", generateHmac256(PASS_PHRASE));
	headers.put("KC-API-KEY-VERSION", "2");

	callApiWithPost(targetURL, jsonBody, headers);
    }

    private Order positionToOrder(Position position) {

	TradeType tradeType = null;
	String timeInForce = null;
	switch (position.getTradeType()) {
	case LIMIT:
	    tradeType = TradeType.LIMIT;
	    break;

	case IOC:
	    tradeType = TradeType.LIMIT;
	    timeInForce = TradeType.IOC.getKukoinValue();
	    break;

	default:
	    throw new RuntimeException("Not yet implemented " + position.getTradeType());
	}

	Order order = new Order(UUID.randomUUID().toString(), position.getSide().getKukoinValue(),
		tradeType.getKukoinValue(), position.getPrice().toString(), position.getVolume().toString());

	order.setTimeInForce(timeInForce);
	order.setSymbol(position.getSymbol());

	return order;
    }

    private void callApiWithPost(String url, String body, Map<String, String> headers) throws IOException {

	CloseableHttpClient httpClient = HttpClients.createDefault();
	HttpPost httpPost = new HttpPost(url);

	httpPost.setHeader("Content-Type", "application/json");

	for (Map.Entry<String, String> header : headers.entrySet()) {
	    httpPost.setHeader(header.getKey(), header.getValue());
	}

	StringEntity entity = new StringEntity(body);
	httpPost.setEntity(entity);

	HttpResponse response = httpClient.execute(httpPost);

	int statusCode = response.getStatusLine().getStatusCode();
	HttpEntity responseEntity = response.getEntity();
	String responseBody = EntityUtils.toString(responseEntity);
	
	log.info("Status Code {} - Response body {} - Request body {}", statusCode, responseBody, body);

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

	log.info("Status Code {},  body {}", statusCode, responseBody);

	httpClient.close();
    }

    private String generateHmac256(String message) {
	try {
	    byte[] bytes = hmac("HmacSHA256", SECRET_KEY.getBytes(), message.getBytes());

	    Encoder codec = Base64.getEncoder();
	    return codec.encodeToString(bytes);


	} catch (Exception e) {
	    log.error(e.getMessage(), e);
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
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Map<String, String> buildGetParamsFromSymbols(Set<String> symbols) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getBaseUrl() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getApiUrl() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void getMarketData(String stockUsdt) throws IOException {
	// TODO Auto-generated method stub

    }

    @Override
    public void history(String stockUsdt) {
	// TODO Auto-generated method stub

    }

    public static void main(String[] args) {
	KukoinManager kukoinManager = new KukoinManager();
	int totalUSDTtoInvest = 11;
	String symbol = "BTC-USDT";
	BigDecimal price = new BigDecimal(25000);
	BigDecimal volume = new BigDecimal(totalUSDTtoInvest).divide(price, 12, RoundingMode.HALF_UP);
	Position position2 = new Position(Side.LONG, Action.OPEN, TradeType.LIMIT, symbol, TMode.CASH, price, volume,
		null, null);
	List<Position> positions = new ArrayList<Position>();
	positions.add(position2);
//	positions.add(new Position(symbol, new BigDecimal(12)));
	try {
	    kukoinManager.placeOrdersBatch(positions);
	} catch (ClientProtocolException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
