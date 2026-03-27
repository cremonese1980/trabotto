package com.trabot.business.scheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.trabot.business.exchange.okx.OkxManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TMode;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHINUListingOkx 
{
//    
//    private static final String CRON_TWELVE_O_CLOCK = "0 0 14 * * ?";
//    private static final String LHINU_USDT = "ORDI-USDT";
//    private static final BigDecimal LHINU_PRICE = new BigDecimal(2);
//    
////    private static final boolean TEST = false;
//    
////    private static final String BITCOIN_USDT_TEST = "BTC-USDT";
////    private static final BigDecimal BITC_PRICE_TEST = new BigDecimal(20000);
////    private static final String CRON_TEST_O_CLOCK = "0 30 11 * * ?";
//    
//    
//    private final OkxManager okxManager;
//    
//    private final List<Position> positions;
//    
//    
//    public LHINUListingOkx(OkxManager okxManager) {
//	this.okxManager = okxManager;
//	positions = initPositions();
//	
//	
//    }
//
//
//    private List<Position> initPositions() {
//	BigDecimal price = new BigDecimal(2);
//	price = price.setScale(6, RoundingMode.HALF_UP);
//	Position position = getLhinuPosition(getLhinuVolume(new BigDecimal(10)), TradeType.POST_ONLY);
//	Position position2 = getLhinuPosition(getVolume(new BigDecimal(10), price), TradeType.POST_ONLY, price);
//	Position position3 = getLhinuPosition(getLhinuVolume(new BigDecimal(100)), TradeType.POST_ONLY);
//	Position position4 = getLhinuPosition(getVolume(new BigDecimal(100), price), TradeType.POST_ONLY, price);
//	
//	Position position5 = getLhinuPosition(getLhinuVolume(new BigDecimal(10)), TradeType.LIMIT);
//	Position position6 = getLhinuPosition(getLhinuVolume(new BigDecimal(100)), TradeType.LIMIT);
//	Position position7 = getLhinuPosition(getVolume(new BigDecimal(100), price), TradeType.LIMIT, price);
//	
//	Position position8 = getLhinuPosition(getLhinuVolume(new BigDecimal(10)), TradeType.IOC);
//	Position position9 = getLhinuPosition(getLhinuVolume(new BigDecimal(100)), TradeType.IOC);
//	Position position10 = getLhinuPosition(getVolume(new BigDecimal(100), price), TradeType.IOC, price);
//	
//	price = new BigDecimal(0.0003);
//	price = price.setScale(6, RoundingMode.HALF_UP);
//	Position position11 = getLhinuPosition(getVolume(new BigDecimal(10), price), TradeType.LIMIT, price);
//	Position position12 = getLhinuPosition(getVolume(new BigDecimal(10), price), TradeType.IOC, price);
//	price = new BigDecimal(0.0004);
//	price = price.setScale(6, RoundingMode.HALF_UP);
//	Position position13 = getLhinuPosition(getVolume(new BigDecimal(10), price), TradeType.IOC, price);
//	Position position14 = getLhinuPosition(getLhinuVolume(new BigDecimal(300)), TradeType.IOC);
//	
//	Position position15 = getLhinuPosition(getLhinuVolume(new BigDecimal(1000)), TradeType.IOC);
//	price = new BigDecimal(0.0003);
//	price = price.setScale(6, RoundingMode.HALF_UP);
//	Position position16 = getLhinuPosition(getVolume(new BigDecimal(1000), price), TradeType.IOC, price);
//	
//	price = new BigDecimal(0.0004);
//	price = price.setScale(6, RoundingMode.HALF_UP);
//	Position position17 = getLhinuPosition(getVolume(new BigDecimal(1000), price), TradeType.IOC, price);
//	
//	
//	List<Position> positions = new ArrayList<Position>();
//	positions.add(position);
//	positions.add(position2);
//	positions.add(position3);
//	positions.add(position4);
//	positions.add(position5);
//	positions.add(position6);
//	positions.add(position7);
//	positions.add(position8);
//	positions.add(position9);
//	positions.add(position10);
//	positions.add(position11);
//	positions.add(position12);
//	positions.add(position13);
//	positions.add(position14);
//	positions.add(position15);
//	positions.add(position16);
//	positions.add(position17);
//	
//	return positions;
//    }
//
//
////    @Async
////    @Scheduled(cron = CRON_TWELVE_O_CLOCK)
//    public void order_100_0_000145() {
//	
//	long timestamp = System.currentTimeMillis();
////	log.info("A {}", timestamp);
//	placeOrderBatch();
//
//	long diff = System.currentTimeMillis() - timestamp;
//	log.info("A {}ms" , diff );
//	
//    }
//    
////    @Async
////    @Scheduled(cron = CRON_TWELVE_O_CLOCK)
//    public void marketData() throws IOException {
//	
//	long timestamp = System.currentTimeMillis();
////	log.info("B {}", timestamp);
//	okxManager.getMarketData(LHINU_USDT);
//
//	long diff = System.currentTimeMillis() - timestamp;
//	log.info("B {}ms" , diff );
//	
//    }
//    
//    
//    private void placeOrder() {
//	BigDecimal bitcoinPrice = new BigDecimal(20000);
//	int totalUSDTtoInvest = 10;
//	String symbol = LHINU_USDT;
//	BigDecimal volume = new BigDecimal(totalUSDTtoInvest).divide(bitcoinPrice, 10, RoundingMode.HALF_UP);
//	Position position = new Position(Side.LONG, Action.OPEN, TradeType.POST_ONLY, symbol, TMode.CASH, bitcoinPrice,
//		volume, null, null);
//	try {
//	    okxManager.placeOrder(position);
//	    System.out.println("\nBITCOIN 5 USDT order placed on okx  check it ************");
//	} catch (ClientProtocolException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//    }
//    
//    private void placeOrderBatch() {
//	
//	
//	
//	
//	try {
//	    okxManager.placeOrdersBatch(positions);
//	    System.out.println("\nBITCOIN 5 USDT order placed on okx  check it ************");
//	} catch (ClientProtocolException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//    }
//    
//    private Position getLhinuPosition(BigDecimal volume, TradeType tradeType) {
//	
//	BigDecimal lhinuPrice = 
////		TEST ? BITC_PRICE_TEST :  
//		    LHINU_PRICE.setScale(6, RoundingMode.HALF_UP);
//	
//	return getLhinuPosition(volume, tradeType, lhinuPrice);
//    }  
//    
//    private Position getLhinuPosition(BigDecimal volume, TradeType tradeType, BigDecimal price) {
//	
//	String symbol = 
////		TEST ? BITCOIN_USDT_TEST : 
//		    LHINU_USDT;
//	BigDecimal lhinuPrice = price;
//	
//	return new Position(Side.LONG, Action.OPEN, tradeType, symbol, TMode.CASH, lhinuPrice,
//		volume, null, null);
//	
//    }
//    
//    private BigDecimal getVolume(BigDecimal usdtSize, BigDecimal price) {
//	return usdtSize.divide(price, 10, RoundingMode.HALF_UP);
//
//    }
//    
//    private BigDecimal getLhinuVolume(BigDecimal usdtSize) {
//	return getVolume(usdtSize, 
////		TEST ? BITC_PRICE_TEST :  
//		    LHINU_PRICE);
//
//    }
//    
//    public static void main(String[] args) {
//	
//	BigDecimal price = new BigDecimal(0.000145);
//	price = price.setScale(6, RoundingMode.HALF_UP);
//	System.out.print(price);
//    }
//
}
