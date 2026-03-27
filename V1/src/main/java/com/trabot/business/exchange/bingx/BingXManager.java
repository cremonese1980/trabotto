package com.trabot.business.exchange.bingx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.trabot.persistance.model.entities.Position;

import lombok.extern.java.Log;

@Log
@Service
public class BingXManager {
    
    public static final String LITERAL_KEY = "bingx";

    /*
     * Public
     */
    public static final String BITCOIN_USDT = "BTC-USDT";
    public static final String ETH_USDT = "ETH-USDT";
    
    public static final String LONG = "Long";
    public static final String SHORT = "Short";
    public static final BigDecimal ORDER_SIZE = new BigDecimal(1);

    /*
     * Private
     */
    private static final String POST = "POST";
    private static final String GET = "GET";

    private static final String API_KEY = "iLLMWFFPeesYGvUDQWAbhsc1uWSPNedVg25jg4NwdyXeL8XXyAGotaRSK246CDpjtki94VsOGCXCfxRg";
    private static final String SECRET_KEY = "aRqeflg4h6U8vID398fciiAySnMS0TAFd8ngFGgwlDpjmSOeL7Mz74H4l2l0EowkD4o1FQ0joPcdE9cIZhg";

    private static final String BASE_URL = "https://api-swap-rest.bingbon.pro";
    private static final String API_SUBPATH = "/api/v1";
    private static final String GET_BALANCE = "/user/getBalance";
    private static final String SET_LEVERAGE = "/user/setLeverage";
    private static final String GET_LEVERAGE = "/user/getLeverage";
    private static final String ORDER_DETAILS = "/user/queryOrderStatus";
    private static final String PLACE_ORDER = "/user/trade";
    private static final String GET_MARGIN = "/user/getMarginMode";

    private static final String GETP_PRICE_URL = "https://api-swap-rest.bingbon.pro/api/v1/market/getLatestPrice?symbol=";

    private String generateHmac256(String message) {
	try {
	    byte[] bytes = hmac("HmacSHA256", SECRET_KEY.getBytes(), message.getBytes());

	    // base64 encode
	    Encoder codec = Base64.getEncoder();
	    String b64Str = codec.encodeToString(bytes);

	    // url encode
	    String signature = URLEncoder.encode(b64Str);
	    return signature;

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

    private String getMessageToDigest(String method, String path, TreeMap<String, String> parameters) {
	Boolean first = true;
	String valueToDigest = method + path;
	for (Map.Entry<String, String> e : parameters.entrySet()) {
	    if (!first) {
		valueToDigest += "&";
	    }
	    first = false;
	    valueToDigest += e.getKey() + "=" + e.getValue();
	}
	return valueToDigest;
    }

    private String getRequestUrl(String path, TreeMap<String, String> parameters) {
	String urlStr = BASE_URL + path + "?";

	Boolean first = true;
	for (Map.Entry<String, String> e : parameters.entrySet()) {
	    if (!first) {
		urlStr += "&";
	    }
	    first = false;
	    urlStr += e.getKey() + "=" + e.getValue();
	}

	return urlStr;
    }

    public String placeOrder(String symbol, String side, String price, String volume, String tradeType, String action,
	    BigDecimal slPrice, BigDecimal tpPrice) {
	String method = "POST";
	String path = "/api/v1/user/trade";
	String timestamp = "" + new Timestamp(System.currentTimeMillis()).getTime();

	TreeMap<String, String> parameters = new TreeMap<String, String>();
	parameters.put("action", action);
	parameters.put("apiKey", API_KEY);
	parameters.put("entrustPrice", price);
	parameters.put("entrustVolume", volume);
	parameters.put("side", side);
//	parameters.put("stopLossPrice", slPrice.toString());
	parameters.put("symbol", symbol);
//	parameters.put("takeProfitPrice", tpPrice.toString());
	parameters.put("tradeType", tradeType);
	parameters.put("timestamp", timestamp);

	String valueToDigest = getMessageToDigest(method, path, parameters);
	String messageDigest = generateHmac256(valueToDigest);
	parameters.put("sign", messageDigest);
	String requestUrl = getRequestUrl(path, parameters);

	String response = post(requestUrl);

	Gson gson = new Gson();
	JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

	JsonObject dataObject = jsonResponse.getAsJsonObject("data");
	return dataObject.get("orderId").getAsString();
    }

    public void placeStopOrder(String orderId, String volume, String tradeType, BigDecimal slPrice,
	    BigDecimal tpPrice) {
	String method = "POST";
	String path = "/api/v1/user/stopOrder";
	String timestamp = "" + new Timestamp(System.currentTimeMillis()).getTime();

	TreeMap<String, String> parameters = new TreeMap<String, String>();
	parameters.put("apiKey", API_KEY);
	parameters.put("timestamp", timestamp);
	parameters.put("positionId", orderId);
	parameters.put("stopLossPrice", slPrice.toString());
	parameters.put("takeProfitPrice", tpPrice.toString());
	parameters.put("entrustVolume", volume);

	String valueToDigest = getMessageToDigest(method, path, parameters);
	String messageDigest = generateHmac256(valueToDigest);
	parameters.put("sign", messageDigest);
	String requestUrl = getRequestUrl(path, parameters);

	post(requestUrl);
    }
    
    public void oneClickClosePosition(String orderId, String volume, String tradeType, BigDecimal slPrice,
	    BigDecimal tpPrice) {
	String method = "POST";
	String path = "/api/v1/user/oneClickClosePosition";
	String timestamp = "" + new Timestamp(System.currentTimeMillis()).getTime();

	TreeMap<String, String> parameters = new TreeMap<String, String>();
	parameters.put("apiKey", API_KEY);
	parameters.put("timestamp", timestamp);
	parameters.put("positionId", orderId);
	parameters.put("stopLossPrice", slPrice.toString());
	parameters.put("takeProfitPrice", tpPrice.toString());
	parameters.put("entrustVolume", volume);

	String valueToDigest = getMessageToDigest(method, path, parameters);
	String messageDigest = generateHmac256(valueToDigest);
	parameters.put("sign", messageDigest);
	String requestUrl = getRequestUrl(path, parameters);

	post(requestUrl);
    }

    private String post(String requestUrl) {

	String result = "";
	try {

	    URL url = new URL(requestUrl);
	    URLConnection conn = url.openConnection();
	    HttpURLConnection http = (HttpURLConnection) conn;
	    http.setRequestMethod("POST"); // PUT is another valid option
	    http.setDoOutput(true);
	    conn.setDoOutput(true);
	    conn.setDoInput(true);

	    String line = "";
	    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    while ((line = in.readLine()) != null) {
		result += line;
	    }

	    System.out.println("\t" + result);

	} catch (Exception e) {
	    System.out.println("expection:" + e);
	}

	return result;
    }

    public double getPrice(String pair) {
	try {
	// Create a URL object from the API URL
	String getPriceUrl = GETP_PRICE_URL + pair;
	URL url = new URL(getPriceUrl);

	// Open a connection to the API
	HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	// Set the request method to GET
	connection.setRequestMethod("GET");

	// Get the response code
	int responseCode = connection.getResponseCode();
	if (responseCode != HttpURLConnection.HTTP_OK) {
	    throw new IOException("Error connecting to the API. HTTP response code: " + responseCode);
	}

	// Read the response from the API
	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	StringBuilder response = new StringBuilder();
	String line;
	while ((line = in.readLine()) != null) {
	    response.append(line);
	}

	// Close the input stream and disconnect from the API
	in.close();
	connection.disconnect();

	Gson gson = new Gson();
	JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

	JsonObject dataObject = jsonResponse.getAsJsonObject("data");

	// Access the "tradePrice" field in the "data" object
	String fairPrice = dataObject.get("tradePrice").getAsString();

	return Double.valueOf(fairPrice);
	}catch(IOException e) {
	    return 0;
	}
    }

    public void getBalance() throws UnsupportedEncodingException {

	String baseUrl = BASE_URL + API_SUBPATH + GET_BALANCE;
	// POST/api/v1/user/getBalanceapiKey=iLLMWFFPeesYGvUDQWAbhsc1uWSPNedVg25jg4NwdyXeL8XXyAGotaRSK246CDpjtki94VsOGCXCfxRg&currency=USDT&timestamp=1682791100120
	// "POST/api/v1/user/getBalanceapiKey=Zsm4DcrHBTewmVaElrdwA67PmivPv6VDK6JAkiECZ9QfcUnmn67qjCOgvRuZVOzU&currency=USDT&timestamp=1615272721001"
	String params = "apiKey=" + API_KEY + "&currency=USDT&timestamp=" + System.currentTimeMillis();
	String inputToSign = POST + API_SUBPATH + GET_BALANCE + params;

	String signature = signStringHmacSHA256(inputToSign, SECRET_KEY);
	System.out.println("signature before decode " + signature);

	signature = URLEncoder.encode(signature, "UTF-8");

	String urlString = baseUrl + "?" + params + "&sign=" + signature;

	System.out.println("Url get balance " + urlString);
	System.out.println("input to sign " + inputToSign);
	System.out.println("signature after decode " + signature);

	try {
	    URL url = new URL(urlString);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");

	    int responseCode = connection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();

		System.out.println("JSON Response: " + response.toString());
	    } else {
		System.out.println("GET request failed. Response Code: " + responseCode);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la connessione all'API", e);
	}

    }

    public void setLeverage(String side, String leverage) throws UnsupportedEncodingException {

	// symbol side leverage apikey timestamp
	// apikey leverage side symbol timestamp

	String baseUrl = BASE_URL + API_SUBPATH + SET_LEVERAGE;
	// POST/api/v1/user/getBalanceapiKey=iLLMWFFPeesYGvUDQWAbhsc1uWSPNedVg25jg4NwdyXeL8XXyAGotaRSK246CDpjtki94VsOGCXCfxRg&currency=USDT&timestamp=1682791100120
	// "POST/api/v1/user/getBalanceapiKey=Zsm4DcrHBTewmVaElrdwA67PmivPv6VDK6JAkiECZ9QfcUnmn67qjCOgvRuZVOzU&currency=USDT&timestamp=1615272721001"
	String params = "apiKey=" + API_KEY + "&leverage=" + leverage + "&side=" + side + "&symbol=" + BITCOIN_USDT
		+ "&timestamp=" + System.currentTimeMillis();
	String inputToSign = POST + API_SUBPATH + SET_LEVERAGE + params;

	String signature = signStringHmacSHA256(inputToSign, SECRET_KEY);
	System.out.println("signature before decode " + signature);

	signature = URLEncoder.encode(signature, "UTF-8");

	String urlString = baseUrl + "?" + params + "&sign=" + signature;

	System.out.println("Url get balance " + urlString);
	System.out.println("input to sign " + inputToSign);
	System.out.println("signature after decode " + signature);

	try {
	    URL url = new URL(urlString);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");

	    int responseCode = connection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();

		System.out.println("JSON Response: " + response.toString());
	    } else {
		System.out.println("GET request failed. Response Code: " + responseCode);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la connessione all'API", e);
	}
    }

    public void getLeverage() throws UnsupportedEncodingException {

	String baseUrl = BASE_URL + API_SUBPATH + GET_LEVERAGE;
	// POST/api/v1/user/getBalanceapiKey=iLLMWFFPeesYGvUDQWAbhsc1uWSPNedVg25jg4NwdyXeL8XXyAGotaRSK246CDpjtki94VsOGCXCfxRg&currency=USDT&timestamp=1682791100120
	// "POST/api/v1/user/getBalanceapiKey=Zsm4DcrHBTewmVaElrdwA67PmivPv6VDK6JAkiECZ9QfcUnmn67qjCOgvRuZVOzU&currency=USDT&timestamp=1615272721001"
	String params = "apiKey=" + API_KEY + "&symbol=" + BITCOIN_USDT + "&timestamp=" + System.currentTimeMillis();
	String inputToSign = POST + API_SUBPATH + GET_LEVERAGE + params;

	String signature = signStringHmacSHA256(inputToSign, SECRET_KEY);
	System.out.println("signature before decode " + signature);

	signature = URLEncoder.encode(signature, "UTF-8");

	String urlString = baseUrl + "?" + params + "&sign=" + signature;

	System.out.println("Url " + urlString);
	System.out.println("input to sign " + inputToSign);
	System.out.println("signature after decode " + signature);

	try {
	    URL url = new URL(urlString);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");

	    int responseCode = connection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();

		System.out.println("JSON Response: " + response.toString());
	    } else {
		System.out.println("GET request failed. Response Code: " + responseCode);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la connessione all'API", e);
	}
    }

    public void getMarginMode() throws UnsupportedEncodingException {

	String baseUrl = BASE_URL + API_SUBPATH + GET_MARGIN;
	// POST/api/v1/user/getBalanceapiKey=iLLMWFFPeesYGvUDQWAbhsc1uWSPNedVg25jg4NwdyXeL8XXyAGotaRSK246CDpjtki94VsOGCXCfxRg&currency=USDT&timestamp=1682791100120
	// "POST/api/v1/user/getBalanceapiKey=Zsm4DcrHBTewmVaElrdwA67PmivPv6VDK6JAkiECZ9QfcUnmn67qjCOgvRuZVOzU&currency=USDT&timestamp=1615272721001"
	String params = "apiKey=" + API_KEY + "&symbol=" + BITCOIN_USDT + "&timestamp=" + System.currentTimeMillis();
	String inputToSign = POST + API_SUBPATH + GET_MARGIN + params;

	String signature = signStringHmacSHA256(inputToSign, SECRET_KEY);
	System.out.println("signature before decode " + signature);

	signature = URLEncoder.encode(signature, "UTF-8");

	String urlString = baseUrl + "?" + params + "&sign=" + signature;

	System.out.println("Url " + urlString);
	System.out.println("input to sign " + inputToSign);
	System.out.println("signature after decode " + signature);

	try {
	    URL url = new URL(urlString);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");

	    int responseCode = connection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();

		System.out.println("JSON Response: " + response.toString());
	    } else {
		System.out.println("GET request failed. Response Code: " + responseCode);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la connessione all'API", e);
	}
    }

    public void getOrder(String orderId) throws UnsupportedEncodingException {

	// apikey orderId symbol timestamp

	String baseUrl = BASE_URL + API_SUBPATH + ORDER_DETAILS;
	String params = "apiKey=" + API_KEY + "&orderId=" + orderId + "&symbol=" + BITCOIN_USDT + "&timestamp="
		+ System.currentTimeMillis();
	String inputToSign = POST + API_SUBPATH + ORDER_DETAILS + params;

	String signature = signStringHmacSHA256(inputToSign, SECRET_KEY);
	System.out.println("signature before decode " + signature);

	signature = URLEncoder.encode(signature, "UTF-8");

	String urlString = baseUrl + "?" + params + "&sign=" + signature;

	System.out.println("Url get balance " + urlString);
	System.out.println("input to sign " + inputToSign);
	System.out.println("signature after decode " + signature);

	try {
	    URL url = new URL(urlString);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");

	    int responseCode = connection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();

		System.out.println("JSON Response: " + response.toString());
	    } else {
		System.out.println("GET request failed. Response Code: " + responseCode);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la connessione all'API", e);
	}
    }

    public void placeOrder(Position position) throws UnsupportedEncodingException {

	// https://github.com/BingX-API/BingX-swap-api-doc/blob/master/Perpetual_Swap_API_Documentation.md#1-place-a-new-order
//	action	
//	apiKey	
//	entrustPrice
//	entrustVolume	
//	side	
//	stopLossPrice	
//	symbol
//	takerProfitPrice	
//	timestamp	
//	tradeType	

//	parameters.put("symbol", symbol);
//    	parameters.put("apiKey", apiKey);
//    	parameters.put("side", side);
//    	parameters.put("entrustPrice", price);
//    	parameters.put("entrustVolume", volume);
//    	parameters.put("tradeType", tradeType);
//    	parameters.put("action", action);
//    	parameters.put("timestamp", timestamp);

	String baseUrl = BASE_URL + API_SUBPATH + PLACE_ORDER;
	String params = "action=" + position.getAction().getValue() + "&apiKey=" + API_KEY + "&entrustPrice="
		+ position.getEntrustPriceAsString() + "&entrustVolume=" + position.getEntrustVolumeAsString()
		+ "&side=" + position.getSide().getValue()
//		+ "&stopLossPrice=" + position.getStopLossPriceAsString()
		+ "&symbol=" + position.getSymbol()
//		+ "&takeProfitPrice=" + position.getTakerProfitPriceAsString()
		+ "&timestamp=" + System.currentTimeMillis() + "&tradeType=" + position.getTradeType().getValue();
	String inputToSign = POST + API_SUBPATH + PLACE_ORDER + params;

	String signature = signStringHmacSHA256(inputToSign, SECRET_KEY);
	System.out.println("signature before decode " + signature);

	signature = URLEncoder.encode(signature, "UTF-8");

	String urlString = baseUrl + "?" + params + "&sign=" + signature;

	System.out.println("Url " + urlString);
	System.out.println("input to sign " + inputToSign);
	System.out.println("signature after decode " + signature);

	try {
	    URL url = new URL(urlString);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");

	    connection.setDoOutput(true);
	    connection.setDoInput(true);

	    int responseCode = connection.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK) {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}
		in.close();

		System.out.println("JSON Response: " + response.toString());
	    } else {
		System.out.println("GET request failed. Response Code: " + responseCode);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la connessione all'API", e);
	}
    }

    private String signStringHmacSHA256(String input, String secretKey) {
	try {
	    Mac mac = Mac.getInstance("HmacSHA256");
	    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	    mac.init(secretKeySpec);
	    byte[] signature = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
	    return Base64.getEncoder().encodeToString(signature);
	} catch (Exception e) {
	    throw new RuntimeException("Errore durante la firma della stringa con HmacSHA256", e);
	}
    }

    private String bytesToHex(byte[] bytes) {
	StringBuilder sb = new StringBuilder();
	for (byte b : bytes) {
	    sb.append(String.format("%02x", b));
	}
	return sb.toString();
    }

    // "POST/api/v1/user/getBalanceapiKey=Zsm4DcrHBTewmVaElrdwA67PmivPv6VDK6JAkiECZ9QfcUnmn67qjCOgvRuZVOzU&currency=USDT&timestamp=1615272721001"
    private String getBaseStringToSign(String method, String apitSubpath, String endpoint) {
	return null;
    }
}
