package com.trabot.business.scheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trabot.business.exchange.kukoin.KukoinManager;
import com.trabot.business.listing.ListingNewCryptoScheduler;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class OBIListingOnKukoin 
//extends ListingNewCryptoScheduler 
{

    private static final int X2_INDEX = 0, X3_INDEX = 1, X4_INDEX = 2, X5_INDEX = 3, X6_INDEX = 4, X7_INDEX = 5,
	    X8_INDEX = 6, X9_INDEX = 7, X10_INDEX = 8;

    private static final String PAIR = "OBI-USDT";// OBI-USDT
    private static final BigDecimal BASE_PRICE = new BigDecimal(0.008);// 0.008
    private static final int[] PRICE_MULTIPLIERS = {2, 3, 4, 5, 6, 7, 8, 9, 10}; // {2, 3, 4, 5, 6, 7, 8, 9, 10}
    private static final String LISTING_CRON = "0 0 14 29 5 ?";// "0 0 14 29 5 ?"
    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 59 13 29 5 ?";// "59 59 13 29 5 ?"
    private static final int TOTAL_USDT = 2700; //2700
    private static final int PRICE_SCALE = 4;

    private List<Position> bulkOrderPositions;
    private Position position10_X2;
    private Position position100_X2;
    private Position position10_X3;
    private Position position100_X3;

//    public OBIListingOnKukoin(KukoinManager kukoinManager) throws ParseException {
//	super(PAIR, BASE_PRICE, LISTING_CRON, PRICE_SCALE, PRICE_MULTIPLIERS, kukoinManager);
//	long diffInSeconds = (listingDate.getTime() - System.currentTimeMillis()) / 1000;
//	log.info("trabot9000 check - Scheduler place order  still {}s missing", diffInSeconds);
//
//    }

//    @Override
//    protected void initPositions() {
//
//	bulkOrderPositions = new ArrayList<Position>();
//	BigDecimal price = new BigDecimal(3).multiply(BASE_PRICE);
//	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//	bulkOrderPositions.add(getBuyPosition(getVolume(new BigDecimal(TOTAL_USDT), price), TradeType.IOC, price));
//
//	price = new BigDecimal(5).multiply(BASE_PRICE);
//	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//	bulkOrderPositions.add(getBuyPosition(getVolume(new BigDecimal(TOTAL_USDT), price), TradeType.IOC, price));
//
//	price = new BigDecimal(7).multiply(BASE_PRICE);
//	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//	bulkOrderPositions.add(getBuyPosition(getVolume(new BigDecimal(TOTAL_USDT), price), TradeType.IOC, price));
//
//	price = new BigDecimal(8).multiply(BASE_PRICE);
//	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//	bulkOrderPositions.add(getBuyPosition(getVolume(new BigDecimal(TOTAL_USDT), price), TradeType.IOC, price));
//
//	for (int multiplier : PRICE_MULTIPLIERS) {
//
//	    List<Position> positions = positionMap.get(multiplier);
//	    price = new BigDecimal(multiplier).multiply(BASE_PRICE);
//	    price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//
//	    positions.add(getBuyPosition(getVolume(new BigDecimal(TOTAL_USDT), price), TradeType.IOC, price));
//	}
//
//	
//	price = new BigDecimal(2).multiply(BASE_PRICE);
//	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//	position10_X2 = getBuyPosition(getVolume(new BigDecimal(10), price), TradeType.IOC, price);
//	position100_X2 = getBuyPosition(getVolume(new BigDecimal(100), price), TradeType.IOC, price);
//	
//	price = new BigDecimal(3).multiply(BASE_PRICE);
//	price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
//	position10_X3 = getBuyPosition(getVolume(new BigDecimal(10), price), TradeType.IOC, price);
//	position100_X3 = getBuyPosition(getVolume(new BigDecimal(100), price), TradeType.IOC, price);
//
//    }
//
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderBulk1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 1);
//
//	log.info("1st method - bulk order {}", bulkOrderPositions);
//	placeOrdersBatch(bulkOrderPositions);
//
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderBulk2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 800);
//
//	log.info("1st method bis - bulk order {}", bulkOrderPositions);
//	placeOrdersBatch(bulkOrderPositions);
//
//    }
//    
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX2_10() {
//
//	waitTargetTimestamp(listingDate.getTime() + 1);
//
//	log.info("2nd method - order X2 10 USDT{}", position10_X2);
//	placeOrder(position10_X2);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX2_100() {
//
//	waitTargetTimestamp(listingDate.getTime() + 5);
//
//	log.info("2nd method bis - order X2 100 USDT{}", position100_X2);
//	placeOrder(position100_X2);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX3_10() {
//
//	waitTargetTimestamp(listingDate.getTime() + 10);
//
//	log.info("2nd method ter - order X3 10 USDT{}", position10_X3);
//	placeOrder(position10_X3);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX3_100() {
//
//	waitTargetTimestamp(listingDate.getTime() + 15);
//
//	log.info("2nd method quater - order X3 100 USDT{}", position100_X3);
//	placeOrder(position100_X3);
//    }
//
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX2_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 1);
//
//	log.info("3rd method - order X2 {}", positionMap.get(PRICE_MULTIPLIERS[X2_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X2_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX2_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 10);
//
//	log.info("4th method - order X2 {}", positionMap.get(PRICE_MULTIPLIERS[X2_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X2_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX2_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 20);
//
//	log.info("5th method - order X2 {}", positionMap.get(PRICE_MULTIPLIERS[X2_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X2_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX3_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 30);
//
//	log.info("6th method - order X3 {}", positionMap.get(PRICE_MULTIPLIERS[X3_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X3_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrderX3_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 40);
//
//	log.info("7th method - order X3 {}", positionMap.get(PRICE_MULTIPLIERS[X3_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X3_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX3_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 50);
//
//	log.info("8th method - order X3 {}", positionMap.get(PRICE_MULTIPLIERS[X3_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X3_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX4_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 60);
//
//	log.info("9th method - order X4 {}", positionMap.get(PRICE_MULTIPLIERS[X4_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X4_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX4_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 70);
//
//	log.info("10th method - order X4 {}", positionMap.get(PRICE_MULTIPLIERS[X4_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X4_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX4_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 80);
//
//	log.info("11th method - order X4 {}", positionMap.get(PRICE_MULTIPLIERS[X4_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X4_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX5_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 90);
//
//	log.info("12th method - order X5 {}", positionMap.get(PRICE_MULTIPLIERS[X5_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X5_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX5_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 100);
//
//	log.info("13th method - order X5 {}", positionMap.get(PRICE_MULTIPLIERS[X5_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X5_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX5_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 110);
//
//	log.info("14th method - order X5 {}", positionMap.get(PRICE_MULTIPLIERS[X5_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X5_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX6_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 120);
//
//	log.info("15th method - order X6 {}", positionMap.get(PRICE_MULTIPLIERS[X6_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X6_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX6_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 130);
//
//	log.info("16th method - order X6 {}", positionMap.get(PRICE_MULTIPLIERS[X6_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X6_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX6_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 140);
//
//	log.info("17th method - order X6 {}", positionMap.get(PRICE_MULTIPLIERS[X6_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X6_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX7_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 150);
//
//	log.info("18th method - order X7 {}", positionMap.get(PRICE_MULTIPLIERS[X7_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X7_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX7_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 160);
//
//	log.info("19th method - order X7 {}", positionMap.get(PRICE_MULTIPLIERS[X7_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X7_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX7_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 170);
//
//	log.info("20th method - order X7 {}", positionMap.get(PRICE_MULTIPLIERS[X7_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X7_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX8_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 180);
//
//	log.info("21th method - order X8 {}", positionMap.get(PRICE_MULTIPLIERS[X8_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X8_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX8_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 190);
//
//	log.info("22th method - order X8 {}", positionMap.get(PRICE_MULTIPLIERS[X8_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X8_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX8_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 200);
//
//	log.info("23th method - order X8 {}", positionMap.get(PRICE_MULTIPLIERS[X8_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X8_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX9_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 210);
//
//	log.info("24th method - order X9 {}", positionMap.get(PRICE_MULTIPLIERS[X9_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X9_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX9_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 220);
//
//	log.info("25th method - order X9 {}", positionMap.get(PRICE_MULTIPLIERS[X9_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X9_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX9_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 230);
//
//	log.info("26th method - order X9 {}", positionMap.get(PRICE_MULTIPLIERS[X9_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X9_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX10_1() {
//
//	waitTargetTimestamp(listingDate.getTime() + 240);
//
//	log.info("27th method - order X10 {}", positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX10_2() {
//
//	waitTargetTimestamp(listingDate.getTime() + 250);
//
//	log.info("28th method - order X10 {}", positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//    }
//
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX10_3() {
//
//	waitTargetTimestamp(listingDate.getTime() + 260);
//
//	log.info("29th method - order X10 {}", positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//    }
//    
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX10_4() {
//
//	waitTargetTimestamp(listingDate.getTime() + 350);
//
//	log.info("30th method - order X10 {}", positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX10_5() {
//
//	waitTargetTimestamp(listingDate.getTime() + 450);
//
//	log.info("31th method - order X10 {}", positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrderX10_6() {
//
//	waitTargetTimestamp(listingDate.getTime() + 550);
//
//	log.info("32th method - order X10 {}", positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//	placeOrder(positionMap.get(PRICE_MULTIPLIERS[X10_INDEX]).get(0));
//    }
//
////    @Async
////    @Scheduled(cron = LISTING_CRON)
//    public void marketData() throws IOException {
//
//	exchangeManager.getMarketData(PAIR);
//
//    }
//
////    @Async
////    @Scheduled(cron = LISTING_CRON)
//    public void history() throws IOException {
//
//	exchangeManager.history(PAIR);
//
//    }
//
//    @Override
//    public void scheduledPlaceOrder() {
//	// TODO Auto-generated method stub
//
//    }

}
