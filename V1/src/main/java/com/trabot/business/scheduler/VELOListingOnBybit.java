package com.trabot.business.scheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trabot.business.exchange.bybit.BybitManager;
import com.trabot.business.listing.ListingNewCryptoScheduler;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class VELOListingOnBybit extends ListingNewCryptoScheduler {

    private static final int X2_INDEX = 0, X3_INDEX = 1, X4_INDEX = 2, X5_INDEX = 3;

    private static final String PAIR = "VELOUSDT";// VELOUSDT
    private static final BigDecimal BASE_PRICE = new BigDecimal(0.0025);// 0.003
    private static final double[] PRICE_MULTIPLIERS = {1, 1.2, 1.44}; // {1, 1.2};
    private static final String LISTING_CRON = "0 6 10 30 5 ?";// "0 0 10 30 5 ?"
    private static final String LISTING_CRON_ONE_SECOND_BEFORE = "59 5 10 30 5 ?";// "59 59 9 30 5 ?"
    private static final int PRICE_SCALE = 4;
    private static final int TOTAL_USDT = 10; // ?
    private static final int TEN_USDT = 10, HUNDRED_USDT = 100, FIVEHUNDREDS_USDT = 500; // ?
    private static final int[] SIZE_LIST = { TEN_USDT, HUNDRED_USDT, FIVEHUNDREDS_USDT };

    private Map<Integer, Map<Double, Map<TradeType, Position>>> positionMapByUsdtByPriceMultiplierByTradeType;

    public VELOListingOnBybit(BybitManager bybitManager) throws ParseException {
	super(PAIR, BASE_PRICE, LISTING_CRON, PRICE_SCALE, new int[]{1}, bybitManager, false);
	long diffInSeconds = (listingDate.getTime() - System.currentTimeMillis()) / 1000;
//	log.info("trabot9000 Scheduler place order  still {}s missing. Position map {}", diffInSeconds, positionMapByUsdtByPriceMultiplierByTradeType);
    }

    @Override
    protected void initPositions() {
	positionMapByUsdtByPriceMultiplierByTradeType = new HashMap<Integer, Map<Double, Map<TradeType, Position>>>();
	BigDecimal price = null;
	for (int size : SIZE_LIST) {

	    positionMapByUsdtByPriceMultiplierByTradeType.put(size, new HashMap<Double, Map<TradeType, Position>>());

	    for (double multiplier : PRICE_MULTIPLIERS) {

		price = new BigDecimal(multiplier).multiply(BASE_PRICE);
		price = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

		Position positionLimit = getBuyPosition(getVolume(new BigDecimal(size), price), TradeType.LIMIT, price);
		Position positionIoc = getBuyPosition(getVolume(new BigDecimal(size), price), TradeType.IOC, price);
		
		positionMapByUsdtByPriceMultiplierByTradeType.get(size).put(multiplier, new HashMap<TradeType, Position>());
		positionMapByUsdtByPriceMultiplierByTradeType.get(size).get(multiplier).put(TradeType.LIMIT, positionLimit);
		positionMapByUsdtByPriceMultiplierByTradeType.get(size).get(multiplier).put(TradeType.IOC, positionIoc);
		
	    }
	}
    }

    private Position getPosition(int size, double multiplier, TradeType tradeType) {

	return positionMapByUsdtByPriceMultiplierByTradeType.get(size).get(multiplier).get(tradeType);

    }
    
    //20 requests/s rate limit for /v5/order/create
    
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
    public void scheduledPlaceOrder_1() {

	waitTargetTimestamp(listingDate.getTime() - 1);
	Position position = getPosition(TEN_USDT, 1, TradeType.IOC);
	log.info("1st method - {}", position);
	placeOrder(position);
    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_2() {
//
//	waitTargetTimestamp(listingDate.getTime() - 1);
//	Position position = getPosition(TEN_USDT, 1, TradeType.LIMIT);
//	log.info("2nd method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_3() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(HUNDRED_USDT, 1, TradeType.IOC);
//	log.info("3rd method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_4() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(HUNDRED_USDT, 1, TradeType.LIMIT);
//	log.info("4th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_5() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(FIVEHUNDREDS_USDT, 1, TradeType.IOC);
//	log.info("5th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_6() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(FIVEHUNDREDS_USDT, 1, TradeType.LIMIT);
//	log.info("6th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_7() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(TEN_USDT, 1.2, TradeType.LIMIT);
//	log.info("7th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_8() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.IOC);
//	log.info("8th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_9() {
//
//	waitTargetTimestamp(listingDate.getTime() + 5);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("9th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_10() {
//
//	waitTargetTimestamp(listingDate.getTime() + 5);
//	Position position = getPosition(FIVEHUNDREDS_USDT, 1.2, TradeType.LIMIT);
//	log.info("10th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_11() {
//
//	waitTargetTimestamp(listingDate.getTime() + 15);
//	Position position = getPosition(FIVEHUNDREDS_USDT, 1.2, TradeType.LIMIT);
//	log.info("11th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_12() {
//
//	waitTargetTimestamp(listingDate.getTime());
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("12th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_13() {
//
//	waitTargetTimestamp(listingDate.getTime() + 10);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("13th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_14() {
//
//	waitTargetTimestamp(listingDate.getTime() + 20);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("14th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_15() {
//
//	waitTargetTimestamp(listingDate.getTime() + 40);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("15th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON_ONE_SECOND_BEFORE)
//    public void scheduledPlaceOrder_16() {
//
//	waitTargetTimestamp(listingDate.getTime() + 60);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("16th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrder_17() {
//
//	waitTargetTimestamp(listingDate.getTime() + 80);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("17th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrder_18() {
//
//	waitTargetTimestamp(listingDate.getTime() + 100);
//	Position position = getPosition(HUNDRED_USDT, 1.2, TradeType.LIMIT);
//	log.info("18th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrder_19() {
//
//	waitTargetTimestamp(listingDate.getTime() + 200);
//	Position position = getPosition(HUNDRED_USDT, 1.44, TradeType.LIMIT);
//	log.info("19th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrder_20() {
//
//	waitTargetTimestamp(listingDate.getTime() + 400);
//	Position position = getPosition(HUNDRED_USDT, 1.44, TradeType.LIMIT);
//	log.info("20th method - {}", position);
//	placeOrder(position);
//    }
//    @Scheduled(cron = LISTING_CRON)
//    public void scheduledPlaceOrder_21() {
//
//	waitTargetTimestamp(listingDate.getTime() + 500);
//	Position position = getPosition(HUNDRED_USDT, 1.44, TradeType.LIMIT);
//	log.info("21th method - {}", position);
//	placeOrder(position);
//    }


    
    public void scheduledPlaceOrder() {
	
    }

    @Override
    protected int getVolumeDecimals() {
	return 2;
    }

}
