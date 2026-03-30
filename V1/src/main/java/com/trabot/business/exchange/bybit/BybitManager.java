package com.trabot.business.exchange.bybit;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.ClientProtocolException;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabot.business.exchange.ExchangeManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TMode;
import com.trabot.persistance.model.enums.TradeType;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class BybitManager implements ExchangeManager {// spain?

    private static final String SECRET_KEY = "VSmzDbCZ7mHqMuR0hHcFgqFGk648x1eGJLMm";
    private static final String API_KEY = "C9vMW1zWYAKGHfr8A4";

    private static final String BASE_URL = "https://api.bybit.com"; // https://api.bytick.com
    private static final String API_URL = "v5";
    private static final String ORDER_URL = 		"/v5/order/create";// "/spot/v3/private/order";
    private static final String ORDER_BATCH_URL = 	"/v5/order/cancel-batch";
    private static final String RECV_WINDOW = "5000";

    public void placeOrdersBatch(List<Position> positions) throws ClientProtocolException, IOException {

	String timestamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
	Map<String, Object> map = positionsToSpotLimitRequest(positions);

	String signature = null;
	try {
	    signature = genPostSign(map, timestamp);
	} catch (InvalidKeyException | NoSuchAlgorithmException | JsonProcessingException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	ObjectMapper objectMapper = new ObjectMapper();
	String jsonMap = objectMapper.writeValueAsString(map);
	
	System.out.println(jsonMap);

	OkHttpClient client = new OkHttpClient().newBuilder().build();
	MediaType mediaType = MediaType.parse("application/json");
	Request request = new Request.Builder().url(BASE_URL + ORDER_BATCH_URL)
		.post(RequestBody.create(mediaType, jsonMap)).addHeader("X-BAPI-API-KEY", API_KEY)
		.addHeader("X-BAPI-SIGN", signature).addHeader("X-BAPI-TIMESTAMP", timestamp)
		.addHeader("X-BAPI-RECV-WINDOW", RECV_WINDOW).addHeader("Content-Type", "application/json").build();
	Call call = client.newCall(request);
	try {
	    Response response = call.execute();
	    assert response.body() != null;
	    System.out.println(response.code() + " - " +  response.body().string());
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    private Map<String, Object> positionsToSpotLimitRequest(List<Position> positions) {

	Map<String, Object> map = new LinkedHashMap<>();
	map.put("category", "option");
	List<Map<String, Object>> orderList = positionsToSpotLimitOrderMapList(positions);
	map.put("request", orderList);

	return map;

    }

    private List<Map<String, Object>> positionsToSpotLimitOrderMapList(List<Position> positions) {

	return positions.stream().map(this::positionToSpotLimitMap).collect(Collectors.toList());

    }

    private Map<String, Object> positionToSpotLimitMap(Position position) {

	String timeInForce = null;
	TradeType tradeType = TradeType.LIMIT;
	switch (position.getTradeType()) {
	case LIMIT:
	    timeInForce = TradeType.GTC.getBybitValue();
	    break;

	case IOC:
	    timeInForce = TradeType.IOC.getBybitValue();
	    break;

	default:
	    throw new NotYetImplementedException(
		    String.format("trade type %s not yet implemented", position.getTradeType()));
	}

	Map<String, Object> map = new HashMap<>();
	map.put("category", "spot");
	map.put("symbol", position.getSymbol());
	map.put("side", position.getSide().getBybitValue());
	map.put("orderType", tradeType.getBybitValue());
	map.put("qty", position.getVolume().toString());
	map.put("price", position.getPrice().toString());
	map.put("timeInForce", timeInForce);
	
	return map;

    }

    public void placeOrder(Position position)
	    {
	String timestamp = Long.toString(ZonedDateTime.now().toInstant().toEpochMilli());
	Map<String, Object> map = positionToSpotLimitMap(position);

	String signature = null;
	try {
	    signature = genPostSign(map, timestamp);
	} catch (InvalidKeyException | NoSuchAlgorithmException | JsonProcessingException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	ObjectMapper objectMapper = new ObjectMapper();
	String jsonMap = null;
	try {
	    jsonMap = objectMapper.writeValueAsString(map);
	} catch (JsonProcessingException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	OkHttpClient client = new OkHttpClient().newBuilder().build();
	MediaType mediaType = MediaType.parse("application/json");
	Request request = new Request.Builder().url(BASE_URL + ORDER_URL).post(RequestBody.create(mediaType, jsonMap))
		.addHeader("X-BAPI-API-KEY", API_KEY).addHeader("X-BAPI-SIGN", signature)
		.addHeader("X-BAPI-TIMESTAMP", timestamp).addHeader("X-BAPI-RECV-WINDOW", RECV_WINDOW)
		.addHeader("Content-Type", "application/json").build();
	Call call = client.newCall(request);
	try {
	    System.out.println(jsonMap);
	    Response response = call.execute();
	    assert response.body() != null;
	    System.out.println(response.body().string());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    /**
     * The way to generate the sign for POST requests
     * 
     * @param params: Map input parameters
     * @return signature used to be a parameter in the header
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws JsonProcessingException
     */
    private static String genPostSign(Map<String, Object> params, String timestamp)
	    throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
	Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
	SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
	sha256_HMAC.init(secret_key);

	ObjectMapper objectMapper = new ObjectMapper();

	String paramJson = objectMapper.writeValueAsString(params);
	String sb = timestamp + API_KEY + RECV_WINDOW + paramJson;
	return bytesToHex(sha256_HMAC.doFinal(sb.getBytes()));
    }

    /**
     * The way to generate the sign for GET requests
     * 
     * @param params: Map input parameters
     * @return signature used to be a parameter in the header
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static String genGetSign(Map<String, Object> params, String timestamp)
	    throws NoSuchAlgorithmException, InvalidKeyException {
	StringBuilder sb = genQueryStr(params);
	String queryStr = timestamp + API_KEY + RECV_WINDOW + sb;

	Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
	SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
	sha256_HMAC.init(secret_key);
	return bytesToHex(sha256_HMAC.doFinal(queryStr.getBytes()));
    }

    /**
     * To convert bytes to hex
     * 
     * @param hash
     * @return hex string
     */
    private static String bytesToHex(byte[] hash) {
	StringBuilder hexString = new StringBuilder();
	for (byte b : hash) {
	    String hex = Integer.toHexString(0xff & b);
	    if (hex.length() == 1)
		hexString.append('0');
	    hexString.append(hex);
	}
	return hexString.toString();
    }

    /**
     * To generate query string for GET requests
     * 
     * @param map
     * @return
     */
    private static StringBuilder genQueryStr(Map<String, Object> map) {
	Set<String> keySet = map.keySet();
	Iterator<String> iter = keySet.iterator();
	StringBuilder sb = new StringBuilder();
	while (iter.hasNext()) {
	    String key = iter.next();
	    sb.append(key).append("=").append(map.get(key)).append("&");
	}
	sb.deleteCharAt(sb.length() - 1);
	return sb;
    }

    @Override
    public void getMarketData(String symbol) throws IOException {

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

    }

    public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException {

	System.out.println(System.currentTimeMillis());
	BybitManager bybitManager = new BybitManager();
	int totalUSDTtoInvest = 7;
	String symbol = "BTCUSDT";
	BigDecimal price = new BigDecimal(21000);
	BigDecimal volume = new BigDecimal(totalUSDTtoInvest).divide(price, 4, RoundingMode.HALF_UP);
	Position position = new Position(Side.LONG, Action.OPEN, TradeType.IOC, symbol, TMode.CASH, price, volume,
		null, null);
	bybitManager.placeOrder(position);
	System.out.println(System.currentTimeMillis());

    }

}
