package com.trabot.business;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;

import com.trabot.business.exchange.bingx.BingXManager;
import com.trabot.business.exchange.kukoin.KukoinManager;
import com.trabot.business.exchange.okx.OkxManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TradeType;

public class ProcessorSUI implements Runnable {

    private static final boolean TEST = false;

    private static final int OKX = 1;
    private static final int KUKOIN = 2;

    private static final long TEN_SECONDS_MS = 10000;
    private static final long HUNDRED_MS = 100;

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
//    private static final String AUCTION_DATE_OKX 	= "2023-05-03 13:40:00.000"; 
    private static final String TRADE_CHEEMS_DATE_BINGX 	= "2023-05-06 08:00:00.000";
//    private static final String TRADE_SUI_DATE_OKX 	= "2023-05-03 14:10:00.000";
    private static final int USDT_TO_INVEST_OKX = 16000;
    private static final int USDT_TO_INVEST_KUKOIN = 2000;
    private static final int USDT_TO_INVEST_BINGX = 100;

//    private static final String AUCTION_DATE_OKX = "2023-05-03 13:24:00.000";
//    private static final String TRADE_SUI_DATE_KUKOIN = "2023-05-03 13:24:30.000";
//    private static final String TRADE_SUI_DATE_OKX = "2023-05-03 13:24:30.000";
//
//    private static final int USDT_TO_INVEST_OKX = 8;
//    private static final int USDT_TO_INVEST_KUKOIN = 6;

    private static final String CHEEMS_USDT = "CHEEMS-USDT"; 
    private static final String CGPT_USDT = "CGPT-USDT"; // 0.2 su bingx 1 maggio
    private static final String EDU_USDT = "EDU-USDT"; // 1.2 su bingx 1 maggio

    private static final String BITCOIN_USDT = "BTC-USDT";
    private static final String PEPE_USDT = "PEPE-USDT";

    private DateFormat dateFormat;
    private Date auctionDate;
    private Date tradeSuiDateOkx;
    private Date tradeCheemsDateBingx;
    private long auctionTimestamp;
    private long tradecheemsTimestampBingx;
    private long tradeSuiTimestampKukoin;
    private OkxManager okxManager;
    private KukoinManager kukoinManager;
    private Position suiOrderOkx;
    private Position cheemsOrderBingx;
    private BingXManager bingXManager;

    private final int id;

    public ProcessorSUI(int id) {
	this.id = id;
    }

    public void run() {

	try {

	    initTrabotto();

//	    waitAuctionTime();
//
//	    placeBitcoingOrderToWarm();
//
//	    lookForSuiTickerOkx();

	    waitTradingSuiTimeOkx();
	    
	    tradeSuiAndCrossFingersOkx();

//	    waitTradingSuiTimeOkx();
//
//	    tradeSuiAndCrossFingersOkx();

	    // misura quanto ci mette tra una request e l'altra. Occhio che se il portatile
	    // �
	    // sovraccarico allora cambier� prestazioni.

	    // Contatore requests to okx. Rate Limit: 60 requests per 2 seconds

	    // IMPORTANTE
	    // SPEGNI gli acquisti APPENA HAI COMPRATO,
	    // SOPRATTUTTO assicurati di spegnere gli aquisti prima di rivendere
	    // che si fermi solo?

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    private void tradeSuiAndCrossFingersOkx() {
	

	placeSuiOrder();

//	int requests = 1;
//
//	while (now - refTimestamp < TEN_SECONDS_MS) {
//
//	    placeSuiOrder(exchange, requests>=2);
//
//	    requests++;
//	    now = System.currentTimeMillis();
//	    System.out.println("Thread " + id + " - " + requests + " attempt sent at " + now + "********************");
//	    // 280 ms tra request e request
//
//	    if (TEST || requests >= 10) {
//		System.out.println("Interrupted after 10 requests");
//		return;
//	    }
//	}

    }

//    private void tradeSuiAndCrossFingersKukoin() {
//
//	placeSuiOrder(KUKOIN, false);
//	System.out.println("Thread " + id + " - First attempt sent at " + System.currentTimeMillis()
//		+ " *********************************************");
//
//	long now = System.currentTimeMillis();
//	int requests = 1;
//
//	while (now - tradeSuiTimestampKukoin < TEN_SECONDS_MS) {
//
//	    placeSuiOrder(KUKOIN, true);
//
//	    requests++;
//	    now = System.currentTimeMillis();
//	    System.out.println("Thread " + id + " - " + requests + " attempt sent at " + now + "********************");
//	    // 280 ms tra request e request
//
//	    if (TEST || requests >= 10) {
//		System.out.println("Interrupted after 10 requests");
//		return;
//	    }
//	}
//
//    }

    private void placeSuiOrder() {
	
	String orderId = bingXManager.placeOrder(CHEEMS_USDT, Side.LONG.getValue(),
	new BigDecimal(0.00004).toString(), "1000", TradeType.MARKET.getValue(), Action.OPEN.getValue(), null,
	null);

    }

    private void lookForSuiTickerOkx() throws IOException {
	lookForTicker(CHEEMS_USDT);

    }

    private void lookForTicker(String symbol) throws IOException {
	System.out.println("Looking for " + symbol + " on Okx");
	okxManager.getTikcer(symbol);
    }

    private void placeBitcoingOrderToWarm() {

	if (TEST) {
	    System.out.println("TEST mode, skipping step placeBitcoingOrderToWarmUp");
	    return;
	}

	int totalUSDTtoInvest = 1;
	String symbol = BITCOIN_USDT;
	BigDecimal volume = new BigDecimal(totalUSDTtoInvest);
	Position position = new Position(symbol, volume);
	try {
	    okxManager.placeOrder(position);
	    kukoinManager.placeOrder(position);
	    System.out.println("\nBITCOIN 5 USDT order placed on okx and kukoin, check it ************");
	} catch (ClientProtocolException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void initTrabotto() throws ParseException {

	initDates();

//	BigDecimal volumeOkx = new BigDecimal(USDT_TO_INVEST_OKX);
//	suiOrderOkx = new Position(CHEEMS_USDT, volumeOkx);
//	okxManager = new OkxManager();

	BigDecimal volumeBingx = new BigDecimal(USDT_TO_INVEST_BINGX);
	cheemsOrderBingx = new Position(CHEEMS_USDT, volumeBingx);
	bingXManager = new BingXManager();

	System.out.println("\nTrabotto initialized ************");
	
    }

    private void initDates() throws ParseException {

	dateFormat = new SimpleDateFormat(DATE_PATTERN);
//	auctionDate = dateFormat.parse(AUCTION_DATE_OKX);
//	tradeSuiDateOkx = dateFormat.parse(TRADE_SUI_DATE_OKX);
	tradeCheemsDateBingx= dateFormat.parse(TRADE_CHEEMS_DATE_BINGX);
	tradecheemsTimestampBingx = tradeCheemsDateBingx.getTime();
//	auctionTimestamp = auctionDate.getTime() + id;
//	tradeSuiTimestampOkx = tradeSuiDateOkx.getTime() + id;
//	tradeSuiTimestampKukoin = tradeSuiDateKukoin.getTime() + id;

	System.out.println("\nInitializing Dates ************");
	System.out.println("Auction date " + auctionDate);
	System.out.println("Trade SUI date Okx" + tradeSuiDateOkx);
//	System.out.println("Trade SUI date Kukoin" + tradeSuiDateKukoin);
	System.out.println("Auction timestamp " + auctionTimestamp);
//	System.out.println("Trade SUI timestamp Okx " + tradeSuiTimestampOkx);
	System.out.println("Trade SUI timestamp Kukoin " + tradeSuiTimestampKukoin);
	System.out.println("Dates initialized **************\n");

    }

    private void waitAuctionTime() {
	System.out.println("\nWaiting Auction, countdown starting ************");
	waitTargetTimestamp(auctionTimestamp);
	System.out.println("\nThread " + id + "Auction Time STARTED! ************");
    }

    private void waitTradingSuiTimeOkx() {
	System.out.println("\nWaiting SUI okx, countdown starting ************");
	waitTargetTimestamp(tradecheemsTimestampBingx);
	System.out.println("\nThread " + id + " - Trading SUI okx Time STARTED at " + System.currentTimeMillis()
		+ " ************");

    }

    private void waitTradingSuiTimeKukoin() {
	System.out.println("\nWaiting SUI kukoin and okx, countdown starting ************");
	waitTargetTimestamp(tradeSuiTimestampKukoin);
	System.out.println("\nThread " + id + " - Trading SUI kukoin and okx Time STARTED at " + System.currentTimeMillis()
		+ " ************");

    }

    private void waitTargetTimestamp(long targetTimestamp) {

	long now = System.currentTimeMillis();
	long diff = targetTimestamp - now;
	int count = 0;
	while (now < targetTimestamp) {

	    if (count % 50 == 0) {

		System.out.println("Thread " + id + " Waiting in " + diff / 1000.0 + "s");
	    }

	    if (diff > HUNDRED_MS) {

		try {
		    long sleepTime = Math.min(diff, HUNDRED_MS);
		    Thread.sleep(sleepTime);
		    count++;
		} catch (Exception e) {
		}
	    }

	    now = System.currentTimeMillis();
	    diff = targetTimestamp - now;

	}

    }
    
    public static void main(String[] args) {
	
	BigDecimal bitcoinPrice = new BigDecimal(26830);
	OkxManager okxManager = new OkxManager();
	int totalUSDTtoInvest = 10;
	String symbol = BITCOIN_USDT;
//	instantPrice.setAverageVelocityPercent(sumVelocityPercent.divide(new BigDecimal(prices.size()), 10, RoundingMode.HALF_UP)
	BigDecimal volume = new BigDecimal(totalUSDTtoInvest).divide(bitcoinPrice, 10, RoundingMode.HALF_UP);
	Position position = new Position(symbol, volume);
	position.setPrice(bitcoinPrice);
	try {
	    okxManager.placeOrder(position);
	    System.out.println("\nBITCOIN 5 USDT order placed on okx  check it ************");
	} catch (ClientProtocolException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
