package com.trabot.business.scheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trabot.business.exchange.kukoin.KukoinManager;
import com.trabot.business.listing.ListingNewCryptoScheduler;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TENETListingOnKukoin //extends ListingNewCryptoScheduler 
{
//
//    private static final String STOCK_USDT = "TENET-USDT";//TENET-USDT
//    private static final BigDecimal STOCK_PRICE = new BigDecimal(0.02);//0.02
//    private static final String LISTING_CRON = "0 49 10 25 5 ?";
//    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 48 10 25 5 ?";
//    private static final int PRICE_SCALE = 6;
//    
//    
//    
//    private List<Position> secondPositions;
//    private List<Position> thirdPositions;
//    
//    public TENETListingOnKukoin(KukoinManager kukoinManager) throws ParseException {
////	super(STOCK_USDT, STOCK_PRICE, LISTING_CRON, PRICE_SCALE, null, kukoinManager);
//	
////	long diffInSeconds = (listingDate.getTime() - System.currentTimeMillis())/1000;
//	
////	log.info("Scheduler place order  still {}s missing", diffInSeconds);
//	
//	initOtherPositions();
//	
//    }
//
////    @Override
////    @Async
////    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder() {
//	
//	log.info("Scheduler place order started");
////	waitTargetTimestamp(listingDate.getTime() + 1);
//	log.info("The date is here");
//	
////	placeOrdersBatch();
//
//    }
//    
////    @Async
////    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderSecond() {
//	
//	log.info("Scheduler Second place order started");
////	waitTargetTimestamp(listingDate.getTime() + 10);
//	log.info("The date is here -  second");
//	
////	placeOrdersBatch(secondPositions);
//
//    }
//    
////    @Async
////    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderThird() {
//	
//	log.info("Scheduler Third place order started");
////	waitTargetTimestamp(listingDate.getTime() + 20);
//	log.info("The date is here -  third");
//	
////	placeOrdersBatch(thirdPositions);
//
//    }
//    
////    @Async
////    @Scheduled(cron = LISTING_CRON)
//    public void marketData() throws IOException {
//	
////	exchangeManager.getMarketData(STOCK_USDT);
//
//    }
//    
////    @Async
////    @Scheduled(cron = LISTING_CRON)
//    public void history() throws IOException {
//	
////	exchangeManager.history(STOCK_USDT);
//
//    }
//
////    @Override
//    protected void initPositions() {
//	List<Position> positions = new ArrayList<Position>();
////	positions.add(getBuyPosition(getVolume(new BigDecimal(10)), TradeType.IOC));
////	positions.add(getBuyPosition(getVolume(new BigDecimal(100)), TradeType.IOC));
////	positions.add(getBuyPosition(getVolume(new BigDecimal(1500)), TradeType.IOC));
//////	return positions;
//	
//    }
//    
//    private void initOtherPositions() {
//	
////	BigDecimal price = new BigDecimal(0.03);
////	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
////	secondPositions = new ArrayList<Position>();
////	secondPositions.add(getBuyPosition(getVolume(new BigDecimal(10), price), TradeType.IOC, price));
////	secondPositions.add(getBuyPosition(getVolume(new BigDecimal(100), price), TradeType.IOC, price));
////	secondPositions.add(getBuyPosition(getVolume(new BigDecimal(1500), price), TradeType.IOC, price));
////	
////	BigDecimal thirdprice = new BigDecimal(0.04);
////	thirdprice = thirdprice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
////	thirdPositions = new ArrayList<Position>();
////	thirdPositions.add(getBuyPosition(getVolume(new BigDecimal(10), thirdprice), TradeType.IOC, thirdprice));
////	thirdPositions.add(getBuyPosition(getVolume(new BigDecimal(100), thirdprice), TradeType.IOC, thirdprice));
////	thirdPositions.add(getBuyPosition(getVolume(new BigDecimal(1500), thirdprice), TradeType.IOC, thirdprice));
//	
//    }
//
////    @Override
//    protected int[] getPriceMultipliers() {
//	// TODO Auto-generated method stub
//	return null;
//    }
//
//
}
