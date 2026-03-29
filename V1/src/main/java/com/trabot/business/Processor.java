package com.trabot.business;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.trabot.business.exchange.bingx.BingXManager;
import com.trabot.persistance.model.entities.InstantPrice;
import com.trabot.persistance.model.entities.Position;
import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TMode;
import com.trabot.persistance.model.enums.TradeType;

import lombok.extern.java.Log;

@Log
public class Processor {

    private static final int OBSERVATION_TIME_IN_SECONDS = 30;
    private static final long TOTAL_DURATION_IN_MILLIS = 8 * 60 * 60 * 1000;

    private static final BigDecimal LEVERAGE_150 = new BigDecimal(150);
    private static final BigDecimal TAKE_PROFIT_PERC = new BigDecimal(10).divide(LEVERAGE_150, 10,
	    RoundingMode.HALF_UP);
    private static final BigDecimal STOP_LOSS_PERC = new BigDecimal(20).divide(LEVERAGE_150, 10, RoundingMode.HALF_UP);

    private static final BigDecimal VELOCITY_TRIGGER = new BigDecimal("0.001");
    private static final BigDecimal STANDARD_DEVIATION_TRIGGER = new BigDecimal("0.0035");
    private static final BigDecimal LAST_TEN_SECONDS_INCREMENT_PERC = new BigDecimal("0.005");
    private static final BigDecimal ZEROS_ACCEPTED_PERC = new BigDecimal(70);
    private static final BigDecimal MONOTONIC_TREND_PERC = new BigDecimal(60);

    private static final BigDecimal ALMOST_ZERO = new BigDecimal(0.0000001);
    private static final long ONE_SECOND = 1000;
    private static final long HALF_SECOND = 500;

    private static final long NO_ORDER_PLACED = -1;

    private final List<InstantPrice> trend = new ArrayList<>();
    private long lastOrderTime = NO_ORDER_PLACED;
    private Position openPosition;

    private BingXManager bingXManager = new BingXManager();

    public void run() {

	try {

//	    int direction = 1;
//
	    BigDecimal price = new BigDecimal(bingXManager.getPrice(BingXManager.BITCOIN_USDT));
//	    BigDecimal volume = calculateVolume(BingXManager.ORDER_SIZE, LEVERAGE_150, price);
//	    BigDecimal increaseTarget = new BigDecimal(direction).multiply(price).multiply(TAKE_PROFIT_PERC)
//		    .divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
//
//	    BigDecimal decreaseLimit = new BigDecimal(-1).multiply(new BigDecimal(direction)).multiply(price)
//		    .multiply(STOP_LOSS_PERC).divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
//	    BigDecimal slPrice = price.add(decreaseLimit);
//	    BigDecimal tpPrice = price.add(increaseTarget);
//	    Position position = new Position(Side.LONG, Action.OPEN, TradeType.MARKET, BingXManager.BITCOIN_USDT, price,
//		    volume, tpPrice, slPrice);
//	    System.out.println("Position: " + position);

//	    bingXManager.placeOrder(position);

//	    String orderId = bingXManager.placeOrder(BingXManager.BITCOIN_USDT, Side.LONG.getValue(), price.toString(),
//		    volume.toString(), TradeType.MARKET.getValue(), Action.OPEN.getValue(), slPrice, tpPrice);
//	    System.out.println("Order created: " + orderId); 
//	    Thread.sleep(HALF_SECOND);

//	    String orderId = bingXManager.placeOrder(BingXManager.BITCOIN_USDT, Side.SHORT.getValue(), "29221.7",
//		    volume.toString(), TradeType.MARKET.getValue(), Action.CLOSE.getValue(), slPrice, tpPrice);
//
//	    System.out.println("Order created: " + orderId);
//	    
//	    bingXManager.getMarginMode();

	    // order 1652436653106753536
//	    bingXManager.getBalance();
//	    
//	    bingXManager.getLeverage();
//	    bingXManager.setLeverage(BingXManager.LONG,LEVERAGE_150.toString());

//	    bingXManager.placeStopOrder(orderId, volume.toString(), TradeType.MARKET.getValue(), slPrice, tpPrice);
//	    bingXManager.getOrder("1652593642776522752");
	    Thread.sleep(HALF_SECOND);

//	    bingXManager.getBalance();
//	    bingXManager.setLeverage(BingXManager.SHORT,LEVERAGE_150.toString());
//	    bingXManager.getBalance();

	} catch ( InterruptedException e2) {
	    // TODO Auto-generated catch block
	    e2.printStackTrace();
	}

	
	System.out.println("Start process");

	long id = 0;

	long startTime = System.currentTimeMillis();
	long currentTime = System.currentTimeMillis();

	while (currentTime - startTime < TOTAL_DURATION_IN_MILLIS) {

	    try {
		InstantPrice instantPrice = getPrice(id, BingXManager.BITCOIN_USDT, currentTime);

		InstantPrice last = trend.size() >= 1 ? trend.get(trend.size() - 1) : null;
		InstantPrice lastBeforeLast = trend.size() >= 2 ? trend.get(trend.size() - 2) : null;
		InstantPrice lastBeforeLastBeforeLast = trend.size() >= 3 ? trend.get(trend.size() - 3) : null;

		instantPrice.setVelocity(last != null ? instantPrice.getPrice().subtract(last.getPrice()) : null);
		instantPrice.setAcceleration(
			lastBeforeLast != null ? instantPrice.getVelocity().subtract(last.getVelocity()) : null);
		instantPrice.setDerivativeAcceleration(lastBeforeLastBeforeLast != null
			? instantPrice.getAcceleration().subtract(last.getAcceleration())
			: null);

		trend.add(instantPrice);
		System.out.println(instantPrice);

		if (trend.size() > 2 * OBSERVATION_TIME_IN_SECONDS + 10) {
		    trend.remove(0);
		}
		boolean openPosition = checkOpenPosition(instantPrice);
		if (!openPosition) {

		    checkGoodPosition();
		}

	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    try {

		Thread.sleep(ONE_SECOND);

		id++;
		currentTime = System.currentTimeMillis();

	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

    }

    private boolean checkOpenPosition(InstantPrice lastPrice) {
	
	if(openPosition == null) {
	    return false;
	}
	
	
	if(lastPrice.getPrice().compareTo(new BigDecimal( Math.max(openPosition.getSlPrice().doubleValue(), openPosition.getTpPrice().doubleValue())))<0 &&
		lastPrice.getPrice().compareTo(new BigDecimal( Math.min(openPosition.getSlPrice().doubleValue(), openPosition.getTpPrice().doubleValue())))>0) {
	    
	    System.out.println("Keep opened the order because last price is between sl and tp for position " + openPosition);
	    return true;
	    
	}
	
	Side side = openPosition.getSide() == Side.LONG ? Side.SHORT : Side.LONG;
	
//	String orderId = bingXManager.placeOrder(BingXManager.BITCOIN_USDT, side.getValue(), lastPrice.getPrice().toString(),
//		openPosition.getVolume().toString(), TradeType.MARKET.getValue(), Action.CLOSE.getValue(), null, null);

	System.out.println("Order created to close position previously opened: " 
//	+ orderId
	);
	
	openPosition = null;
	return false;
	
    }

    private BigDecimal calculateVolume(BigDecimal orderSize, BigDecimal leverage, BigDecimal price) {

	return orderSize.multiply(leverage).divide(price, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal getAverage(List<BigDecimal> input) {

	BigDecimal totalVelocity = input.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

	return totalVelocity.divide(new BigDecimal(input.size()), 10, RoundingMode.HALF_UP);

    }

    private void checkGoodPosition() throws IOException {

	if (trend.size() < OBSERVATION_TIME_IN_SECONDS + 5) {
	    return;
	}

	int startIndex = Math.max(0, trend.size() - OBSERVATION_TIME_IN_SECONDS);

	List<BigDecimal> lastPrices = trend.subList(startIndex, trend.size()).stream()
		.map(InstantPrice::getVelocityPercent).collect(Collectors.toList());

	BigDecimal averageVelocity = getAverage(lastPrices);

	BigDecimal averageVelocityLastTenSeconds = getAverage(
		lastPrices.subList(lastPrices.size() - 10, lastPrices.size()));

	BigDecimal standardDeviation = calculateStandardDeviation(startIndex, averageVelocity, trend);
	int countZeros = countZeros(lastPrices);
	boolean countZerosOk = countZerosOk(lastPrices, countZeros);
	boolean checkLastOrderTime = checkLastOrderTimeOk();

	String type = averageVelocity.compareTo(BigDecimal.ZERO) > 0 ? "LONG" : "SHORT";
	int direction = type.equals("LONG") ? 1 : -1;

	int monotonicTrendSizeLastTenSeconds = countMonotonic(trend.subList(trend.size() - 10, trend.size()).stream()
		.map(InstantPrice::getVelocity).collect(Collectors.toList()), direction);
	boolean countMonotonicOk = countMonotonicOk(monotonicTrendSizeLastTenSeconds, 10);

	System.out.println("averageVelocity last " + OBSERVATION_TIME_IN_SECONDS + "s : " + averageVelocity
		+ "%, standard deviation " + standardDeviation + ", countZeros " + countZeros + ", countZerosOK "
		+ countZerosOk + ", averageVelocityLastTenSeconds " + averageVelocityLastTenSeconds
		+ ", checkLastOrderTime " + checkLastOrderTime + ", monotonicTrendSizeLastTenSeconds "
		+ monotonicTrendSizeLastTenSeconds+ ", countMonotonicOk "
		+ countMonotonicOk);
	
	

	if (averageVelocity.abs().compareTo(VELOCITY_TRIGGER) > 0 && countZerosOk
		&& standardDeviation.abs().compareTo(STANDARD_DEVIATION_TRIGGER) > 0
		&& averageVelocityLastTenSeconds.abs().compareTo(LAST_TEN_SECONDS_INCREMENT_PERC) > 0
		&& checkLastOrderTime && countMonotonicOk) {

	    BigDecimal weight = BigDecimal.ONE;
	    
	    if(averageVelocityLastTenSeconds.abs().compareTo(LAST_TEN_SECONDS_INCREMENT_PERC.multiply(new BigDecimal(3))) >0) {
		weight = weight.add(weight);
	    }
	    
	    openPosition(direction, type, weight);

	}

    }

    private boolean countMonotonicOk(int monotonicTrendSizeLastTenSeconds, int size) {

	return new BigDecimal(monotonicTrendSizeLastTenSeconds).divide(new BigDecimal(size), 2, RoundingMode.HALF_UP)
		.multiply(new BigDecimal(100)).compareTo(MONOTONIC_TREND_PERC) > 0;
    }

    private int countMonotonic(List<BigDecimal> lastVelocities, int direction) {

	return (int) lastVelocities.stream()
		.filter(velocity -> velocity.multiply(new BigDecimal(direction)).compareTo(BigDecimal.ZERO) > 0)
		.count();
    }

    private boolean checkLastOrderTimeOk() {

	return lastOrderTime == NO_ORDER_PLACED
		|| (System.currentTimeMillis() - lastOrderTime) / 1000 > OBSERVATION_TIME_IN_SECONDS;
    }

    // TODO write tests to verify this behavior
    private boolean countZerosOk(List<BigDecimal> lastPrices, int zeros) {

	return new BigDecimal(zeros).divide(new BigDecimal(lastPrices.size()), 10, RoundingMode.HALF_UP)
		.multiply(new BigDecimal(100)).compareTo(ZEROS_ACCEPTED_PERC) < 0;
    }

    private int countZeros(List<BigDecimal> lastPrices) {

	return (int) lastPrices.stream().filter(val -> val.abs().compareTo(ALMOST_ZERO) < 0).count();

    }

    private InstantPrice getPrice(long id, String pair, long currentTime) throws IOException {

	double price = bingXManager.getPrice(pair);

	return null;
		//new InstantPrice(id, currentTime, pair, new BigDecimal(price).setScale(10, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateStandardDeviation(int startIndex, BigDecimal averageVelocity,
	    List<InstantPrice> trend) {

	if (trend.size() < OBSERVATION_TIME_IN_SECONDS) {
	    System.out.println("Not enough data to calculate standard deviation");
	    return BigDecimal.ZERO;
	}

	// Calcola la somma delle differenze al quadrato negli ultimi 30 secondi
	BigDecimal sumOfSquaredDifferences = BigDecimal.ZERO;
	for (int i = startIndex; i < trend.size(); i++) {
	    BigDecimal difference = trend.get(i).getVelocityPercent().subtract(averageVelocity);
	    BigDecimal squaredDifference = difference.multiply(difference);
	    sumOfSquaredDifferences = sumOfSquaredDifferences.add(squaredDifference);
	}

	// Calcola la media delle differenze al quadrato negli ultimi 30 secondi
	BigDecimal meanOfSquaredDifferences = sumOfSquaredDifferences
		.divide(BigDecimal.valueOf(trend.size() - startIndex), 10, RoundingMode.HALF_UP);

	// Trova la radice quadrata del valore calcolato
	BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(meanOfSquaredDifferences.doubleValue()));

	return standardDeviation;
    }

    private void openPosition(int direction, String type, BigDecimal weight) throws IOException {


	BigDecimal lastPrice = trend.get(trend.size() - 1).getPrice();

	// TODO check impulso. Con acceleration? Con derivata accelerazione? Se impulso
	// forte, aumenta TP
	BigDecimal increaseTarget = new BigDecimal(direction).multiply(lastPrice).multiply(TAKE_PROFIT_PERC)
		.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).multiply(weight);

	BigDecimal decreaseLimit = new BigDecimal(-1).multiply(new BigDecimal(direction)).multiply(lastPrice)
		.multiply(STOP_LOSS_PERC).divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);

	System.out.println("******************************************************\n" + "OPEN " + type
		+ ", order price " + lastPrice + ", TP price " + lastPrice.add(increaseTarget) + ", SL price "
		+ lastPrice.add(decreaseLimit));
	// TODO open thread is it worth?

	Side side = direction == 1 ? Side.LONG : Side.SHORT;
	BigDecimal volume = calculateVolume(BingXManager.ORDER_SIZE, LEVERAGE_150, lastPrice);
	BigDecimal slPrice = lastPrice.add(decreaseLimit);
	BigDecimal tpPrice = lastPrice.add(increaseTarget);
	Position position = new Position(side, Action.OPEN, TradeType.MARKET, BingXManager.BITCOIN_USDT, TMode.CASH,lastPrice,
		volume, tpPrice, slPrice);

//	String orderId = bingXManager.placeOrder(position.getSymbol(), position.getSide().getValue(),
//		lastPrice.toString(), volume.toString(), TradeType.MARKET.getValue(), Action.OPEN.getValue(), slPrice,
//		tpPrice);
	
	lastOrderTime = System.currentTimeMillis();
	openPosition = position;
	System.out.println("Order created: " 
//	+ orderId
	);

    }


}
