package com.trabot.business.scheduler;

import java.io.IOException;
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
public class CAPOCheaperListingOnOkx //sextends ListingNewCryptoScheduler
{

    private static final String STOCK_USDT = "PLS-USDT";
    private static final BigDecimal STOCK_PRICE = new BigDecimal(0.00015);
    private static final String LISTING_CRON = "0 0 8 24 5 ?";
    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 59 7 24 5 ?";
    private static final int PRICE_SCALE = 6;
    private static final int[] PRICE_MULTIPLIERS = {2, 3}; //{2, 3, 4, 5};
    
    public CAPOCheaperListingOnOkx(OkxManager okxManager) throws ParseException {
//	super(STOCK_USDT, STOCK_PRICE, LISTING_CRON, PRICE_SCALE, PRICE_MULTIPLIERS, okxManager);
	
//	long diffInSeconds = (listingDate.getTime() - System.currentTimeMillis())/1000;
	
//	log.info("Scheduler place order  still {}s missing", diffInSeconds);
    }
//
//    @Override
//    @Async
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrder() {
	
	
	log.info("Scheduler place order started");
//	waitTargetTimestamp(listingDate.getTime());
	log.info("The date is here");
	
//	placeOrdersBatch();

    }
    
//    @Async
//    @Scheduled(cron = LISTING_CRON)
    public void marketData() throws IOException {
	
//	exchangeManager.getMarketData(STOCK_USDT);

    }
//    
//    @Async
//    @Scheduled(cron = LISTING_CRON)
    public void history() throws IOException {
	
//	exchangeManager.history(STOCK_USDT);

    }

//    @Override
    protected void initPositions() {
	
	List<Position> positions = new ArrayList<Position>();
	
//	positions.add(getBuyPosition(getVolume(new BigDecimal(10)), TradeType.LIMIT));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(10)), TradeType.IOC));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(100)), TradeType.LIMIT));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(100)), TradeType.IOC));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(200)), TradeType.LIMIT));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(200)), TradeType.IOC));
	
//	return positions;
	
    }

//    @Override
    protected int[] getPriceMultipliers() {
	// TODO Auto-generated method stub
	return null;
    }


}
