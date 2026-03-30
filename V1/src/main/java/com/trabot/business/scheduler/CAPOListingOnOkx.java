package com.trabot.business.scheduler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.trabot.business.exchange.okx.OkxManager;
import com.trabot.persistance.model.entities.Position;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class CAPOListingOnOkx //extends ListingNewCryptoScheduler 
{

    private static final String STOCK_USDT = "PLS-USDT";
    private static final BigDecimal STOCK_PRICE = new BigDecimal(0.0002);
    private static final String LISTING_CRON = "0 0 8 24 5 ?";
    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 59 7 24 5 ?";
    private static final int PRICE_SCALE = 6;
    
    public CAPOListingOnOkx(OkxManager okxManager) throws ParseException {
//	super(STOCK_USDT, STOCK_PRICE, LISTING_CRON, PRICE_SCALE, null, okxManager);
    }

//    @Override
//    @Async
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrder() {
	
	log.info("Scheduler place order started, waiting for the date");
//	waitTargetTimestamp(listingDate.getTime());
	log.info("The date is here");
	
//	placeOrdersBatch();

    }

//    @Override
    protected void initPositions() {
	
	List<Position> positions = new ArrayList<Position>();
	
//	positions.add(getBuyPosition(getVolume(new BigDecimal(10)), TradeType.LIMIT));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(10)), TradeType.IOC));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(100)), TradeType.LIMIT));
//	positions.add(getBuyPosition(getVolume(new BigDecimal(100)), TradeType.IOC));
	
//	return positions;
	
    }

//    @Override
    protected int[] getPriceMultipliers() {
	// TODO Auto-generated method stub
	return null;
    }


}
