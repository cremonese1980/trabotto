package com.trabot.business.listing;

import static com.cronutils.model.CronType.QUARTZ;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.trabot.business.exchange.ExchangeManager;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TMode;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ListingNewCryptoScheduler {

    public static final String DATE_PATTERN = "dd/MM/yyyy HH:mm";

    protected static final long HUNDRED_MS = 100;
    protected static final long FIFTY_MS = 500;
    
    protected final ExchangeManager exchangeManager;
    protected final String symbol;
    protected final BigDecimal listingPrice;
    protected final Date listingDate;
    protected final int priceScale;
    protected final int[] multipliers;
    protected final Map<Integer, List<Position>> positionMap;

    public ListingNewCryptoScheduler(String symbol, BigDecimal listingPrice, String cronExpression,
	    int priceScale, int[] multipliers, ExchangeManager exchangeManager, boolean disable) throws ParseException {

	this.symbol = symbol;
	this.listingPrice = listingPrice.setScale(priceScale, RoundingMode.HALF_UP);
	this.listingDate = cronExpressionToDate(cronExpression);
	this.exchangeManager = exchangeManager;
	this.priceScale = priceScale;
	this.multipliers = multipliers;
	this.positionMap = initPositionMap();
	initPositions();
	if(!disable) {
	    log.info(toString());
	}

    }

    @Override
    public String toString() {
	return "ListingNewCryptoScheduler [exchangeManager=" + exchangeManager + ", symbol=" + symbol
		+ ", listingPrice=" + listingPrice + ", listingDate=" + listingDate + ", priceScale=" + priceScale
		+ ", multipliers=" + Arrays.toString(multipliers) + ", positionMap=" + positionMap + "]";
    }

    public abstract void scheduledPlaceOrder();
    
    protected abstract void initPositions();
    
    protected int[] getPriceMultipliers() {
	return multipliers;
    }
    
    protected Map<Integer, List<Position>> initPositionMap(){
	
	Map<Integer, List<Position>> positionMap = new LinkedHashMap<Integer, List<Position>>();
	
	for (int multiplier : getPriceMultipliers()) {
	    positionMap.put(multiplier, new ArrayList<Position>());
	}
	
	return positionMap;
    }

    protected void placeOrdersBatch(int multiplier) {

	try {
	    exchangeManager.placeOrdersBatch(positionMap.get(multiplier));
	} catch (IOException e) {
	    log.error(e.getMessage(), e);
	}
    }
    
    protected void placeOrdersBatch(List<Position> positions) {

	try {
	    exchangeManager.placeOrdersBatch(positions);
	} catch (IOException e) {
	    log.error(e.getMessage(), e);
	}
    }
    
    protected void placeOrder(Position position) {

	try {
	    exchangeManager.placeOrder(position);
	} catch (IOException e) {
	    log.error(e.getMessage(), e);
	}
    }

    protected BigDecimal getVolume(BigDecimal usdtSize) {
	return usdtSize.divide(listingPrice, getVolumeDecimals(), RoundingMode.HALF_UP);

    }
    
    protected BigDecimal getVolume(BigDecimal usdtSize, BigDecimal price) {
	return usdtSize.divide(price, getVolumeDecimals(), RoundingMode.HALF_UP);

    }
    
    protected abstract int getVolumeDecimals();
    
    protected Position getBuyPosition(BigDecimal volume, TradeType tradeType) {
	
	return new Position(Side.LONG, Action.OPEN, tradeType, symbol, TMode.CASH, listingPrice,
		volume, null, null);
    }  
    
    protected Position getBuyPosition(BigDecimal volume, TradeType tradeType, BigDecimal price) {
	
	return new Position(Side.LONG, Action.OPEN, tradeType, symbol, TMode.CASH, price,
		volume, null, null);
    }  
    

    protected static void waitTargetTimestamp(long targetTimestamp) {

	long now = System.currentTimeMillis();
	long diff = targetTimestamp - now;
	int count = 0;
	while (now < targetTimestamp) {

	    if (count % 50 == 0) {
		log.debug("Thread {} waiting. {} s to go", Thread.currentThread().getId(), diff / 1000.0);
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

    private Date cronExpressionToDate(String cronExpression) throws ParseException {
	String dateAsString = cronExpressionToDateAsString(cronExpression);
	DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
	return dateFormat.parse(dateAsString);
    }

    private String cronExpressionToDateAsString(String cronExpression) {

	CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
	ExecutionTime execTime = ExecutionTime.forCron(parser.parse(cronExpression));
	ZonedDateTime nextExecution = execTime.nextExecution(ZonedDateTime.now()).get();

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
	return nextExecution.format(formatter);

    }

}
