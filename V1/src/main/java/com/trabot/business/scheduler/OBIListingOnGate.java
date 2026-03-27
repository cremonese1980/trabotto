package com.trabot.business.scheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trabot.business.exchange.gate.GateManager;
import com.trabot.business.listing.ListingNewCryptoScheduler;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OBIListingOnGate 
extends ListingNewCryptoScheduler
{
//
    private static final int X2_INDEX = 0, X3_INDEX = 1, X4_INDEX = 2, X5_INDEX = 3, X6_INDEX = 4, X7_INDEX = 5,
	    X8_INDEX = 6, X9_INDEX = 7, X10_INDEX = 8;

    private static final String PAIR = "OBI_USDT";// OBI_USDT
    private static final BigDecimal BASE_PRICE = new BigDecimal(0.008);// 0.008
    private static final int[] PRICE_MULTIPLIERS = {2, 3, 4, 5, 6, 7, 8, 9, 10}; // {2, 3, 4, 5, 6, 7, 8, 9, 10}
    private static final String LISTING_CRON = "0 0 14 29 5 ?";// "0 0 14 29 5 ?"
    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 59 13 29 5 ?";// "59 59 13 29 5 ?"
    private static final int PRICE_SCALE = 4;
    private static final int TOTAL_USDT = 2900; // 2900

    public OBIListingOnGate(GateManager gateManager) throws ParseException {
	super(PAIR, BASE_PRICE, LISTING_CRON, PRICE_SCALE, PRICE_MULTIPLIERS, gateManager, true);
	long diffInSeconds = (listingDate.getTime() - System.currentTimeMillis()) / 1000;
//	log.info("trabot9001 check - Scheduler place order  still {}s missing", diffInSeconds);

    }

    @Override
    protected void initPositions() {

	// Max 10 orders on gate bulk create order
	BigDecimal price = new BigDecimal(3).multiply(BASE_PRICE);
	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

	for (int multiplier : PRICE_MULTIPLIERS) {

	    List<Position> positions = positionMap.get(multiplier);
	    price = new BigDecimal(multiplier).multiply(BASE_PRICE);
	    price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

	    positions.add(getBuyPosition(getVolume(new BigDecimal(TOTAL_USDT), price), TradeType.IOC, price));
	}

    }

    // MAX 10 Requests/second to place orders (including both batch and regular)

//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrderBulk1() {

	waitTargetTimestamp(listingDate.getTime() + 1);
	
	List<Position> positions = subMap(2, 4);
	log.info("1st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrderBulk2() {

	waitTargetTimestamp(listingDate.getTime() + 5);
	
	List<Position> positions = subMap(3, 5);
	log.info("2st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrderBulk3() {

	waitTargetTimestamp(listingDate.getTime() + 30);
	
	List<Position> positions = subMap(3, 6);
	log.info("3st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrderBulk4() {

	waitTargetTimestamp(listingDate.getTime() + 60);
	
	List<Position> positions = subMap(3, 6);
	log.info("4st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
    public void scheduledPlaceOrderBulk5() {

	waitTargetTimestamp(listingDate.getTime() + 90);
	
	List<Position> positions = subMap(4, 7);
	log.info("5st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
    public void scheduledPlaceOrderBulk6() {

	waitTargetTimestamp(listingDate.getTime() + 120);
	
	List<Position> positions = subMap(4, 7);
	log.info("6st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
    public void scheduledPlaceOrderBulk7() {

	waitTargetTimestamp(listingDate.getTime() + 150);
	
	List<Position> positions = subMap(5, 8);
	log.info("7st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
    public void scheduledPlaceOrderBulk8() {

	waitTargetTimestamp(listingDate.getTime() + 180);
	
	List<Position> positions = subMap(6, 9);
	log.info("8st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
    public void scheduledPlaceOrderBulk9() {

	waitTargetTimestamp(listingDate.getTime() + 250);
	
	List<Position> positions = subMap(7, 10);
	log.info("9st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
    public void scheduledPlaceOrderBulk10() {

	waitTargetTimestamp(listingDate.getTime() + 400);
	
	List<Position> positions = subMap(7, 10);
	log.info("10st method - bulk order {}", positions);
	placeOrdersBatch(positions);
    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderBulk11() {
//
//	waitTargetTimestamp(listingDate.getTime() + 300);
//	
//	List<Position> positions = subMap(6, 10);
//	log.info("11st method - bulk order {}", positions);
//	placeOrdersBatch(positions);
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderBulk12() {
//
//	waitTargetTimestamp(listingDate.getTime() + 500);
//	
//	List<Position> positions = subMap(6, 10);
//	log.info("12st method - bulk order {}", positions);
//	placeOrdersBatch(positions);
//    }

    protected List<Position> subMap(int minMultiplier, int maxMultipliers) {

	return positionMap.entrySet().stream()
		.filter(entry -> entry.getKey() >= minMultiplier && entry.getKey() <= maxMultipliers)
		.flatMap(entry -> entry.getValue().stream()).collect(Collectors.toList());

    }


    @Scheduled(cron = LISTING_CRON)
    public void marketData() throws IOException {

	exchangeManager.getMarketData(PAIR);

    }

//    @Async
//    @Scheduled(cron = LISTING_CRON)
    public void history() throws IOException {

	exchangeManager.history(PAIR);

    }

    @Override
    public void scheduledPlaceOrder() {
	// TODO Auto-generated method stub

    }

    @Override
    protected int getVolumeDecimals() {
	return 2;
    }

}
