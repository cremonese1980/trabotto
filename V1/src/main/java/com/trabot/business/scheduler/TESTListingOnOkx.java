package com.trabot.business.scheduler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trabot.business.exchange.okx.OkxManager;
import com.trabot.business.listing.ListingNewCryptoScheduler;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class TESTListingOnOkx //extends ListingNewCryptoScheduler 
{
//
//    private static final String STOCK_USDT = "BTC-USDT";
//    private static final BigDecimal STOCK_PRICE = new BigDecimal(20000);
//    private static final String LISTING_CRON = "0 46 15 21 5 ?";
//    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 45 15 21 5 ?";
//    private static final int PRICE_SCALE = 2;
//    
//    public TESTListingOnOkx(OkxManager okxManager) throws ParseException {
////	super(STOCK_USDT, STOCK_PRICE, LISTING_CRON, PRICE_SCALE, null, okxManager);
//    }
//
////    @Override
////    @Async
////    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder() {
//	
//	log.info("Scheduler place order started, waiting for the date");
////	waitTargetTimestamp(listingDate.getTime());
//	log.info("The date is here");
//	
////	placeOrdersBatch();
//
//    }
//
////    @Override
//    protected void initPositions() {
//	
//	List<Position> positions = new ArrayList<Position>();
//	
////	positions.add(getBuyPosition(getVolume(new BigDecimal(10)), TradeType.LIMIT));
//	
////	return positions;
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
