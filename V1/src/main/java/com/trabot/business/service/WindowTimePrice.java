package com.trabot.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.trabot.persistance.model.entities.InstantPrice;
import com.trabot.persistance.model.pojo.CryptoPrice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WindowTimePrice {

    private final List<InstantPrice> prices;
    private final int maxSize;

    private BigDecimal sumVelocityPercent;
    private BigDecimal sumAccelerationPercent;
    private BigDecimal sumDerivativeAccelerationPercent;

    public WindowTimePrice(int maxSize) {
	this.maxSize = maxSize;
	this.prices = Collections.synchronizedList(new ArrayList<InstantPrice>());
	init();
    }

    public InstantPrice addPrice(CryptoPrice cryptoPrice, long timestamp) throws InterruptedException {

	if (prices.size() == maxSize) {
	    InstantPrice first = prices.remove(0);
	    sumVelocityPercent.subtract(first.getVelocityPercent()!=null?first.getVelocityPercent():BigDecimal.ZERO);
	    sumAccelerationPercent.subtract(first.getAccelerationPercent()!=null?first.getAccelerationPercent():BigDecimal.ZERO);
	    sumDerivativeAccelerationPercent.subtract(first.getDerivativeAccelerationPercent()!=null?first.getDerivativeAccelerationPercent():BigDecimal.ZERO);
	    // TODO remove log
	    log.info("Removed from window time the first price {}", first);
	}

	InstantPrice instantPrice = buildInstantPrice(cryptoPrice, timestamp);
	sumVelocityPercent.add(instantPrice.getVelocityPercent()!=null?instantPrice.getVelocityPercent():BigDecimal.ZERO);
	sumAccelerationPercent.add(instantPrice.getAccelerationPercent()!=null?instantPrice.getAccelerationPercent():BigDecimal.ZERO);
	sumDerivativeAccelerationPercent.add(instantPrice.getDerivativeAccelerationPercent()!=null?instantPrice.getDerivativeAccelerationPercent():BigDecimal.ZERO);

	calculateValues(instantPrice);

	prices.add(instantPrice);

	return instantPrice;
    }

    public boolean isFull() {
	return maxSize - 1 == prices.size();
    }

    private InstantPrice buildInstantPrice(CryptoPrice cryptoPrice, long timestamp) {

	InstantPrice instantPrice = new InstantPrice();
	instantPrice.setPair(cryptoPrice.getSymbol());
	instantPrice.setPrice(cryptoPrice.getPrice());
	instantPrice.setTimestamp(timestamp);

	return instantPrice;
    }

    private void init() {
	sumAccelerationPercent = BigDecimal.ZERO;
	sumDerivativeAccelerationPercent = BigDecimal.ZERO;
	sumVelocityPercent = BigDecimal.ZERO;
    }
    
//    Media mobile esponenziale (EMA): Questo indicatore pone maggior peso sui dati più recenti. Puoi impostare un EMA a breve termine (ad esempio, 5 periodi) e un EMA a lungo termine (ad esempio, 20 periodi). Quando l'EMA a breve termine incrocia al di sopra dell'EMA a lungo termine, potrebbe essere un segnale di acquisto. Quando incrocia al di sotto, potrebbe essere un segnale di vendita.
//    Band di Bollinger: Questo indicatore utilizza una media mobile e la deviazione standard per creare un "canale" attorno al prezzo. Quando il prezzo si muove verso l'estremità superiore del canale, potrebbe essere un segnale di vendita. Quando si muove verso l'estremità inferiore, potrebbe essere un segnale di acquisto.
//    Relative Strength Index (RSI): Questo è un indicatore di momentum che può aiutare a identificare quando un asset è sovracomprato o ipervenduto. Se l'RSI supera 70, l'asset potrebbe essere sovracomprato e potrebbe essere il momento di vendere. Se l'RSI scende sotto 30, l'asset potrebbe essere ipervenduto e potrebbe essere il momento di comprare.

    private void calculateValues(InstantPrice instantPrice) {

	if (!isFull()) {
	    return;
	}

	InstantPrice last = prices.get(prices.size() - 1);

	instantPrice.setVelocity(last.getPrice()!=null ? instantPrice.getPrice().subtract(last.getPrice()!=null?last.getPrice():BigDecimal.ZERO) : null);
	instantPrice.setAcceleration(last.getVelocity()!=null ? instantPrice.getVelocity().subtract(last.getVelocity()!=null?last.getVelocity():BigDecimal.ZERO): null);
	instantPrice.setDerivativeAcceleration(last.getAcceleration() != null ? instantPrice.getAcceleration().subtract(last.getAcceleration()!=null?last.getAcceleration():BigDecimal.ZERO) : null);

	instantPrice.setAverageVelocityPercent(sumVelocityPercent.divide(new BigDecimal(prices.size()), 10, RoundingMode.HALF_UP));
	
//	List<BigDecimal> lastPrices = prices.subList(startIndex, prices.size()).stream()
//	.map(InstantPrice::getVelocityPercent).collect(Collectors.toList());
//
//	BigDecimal averageVelocity = getAverage(lastPrices);
//
//	BigDecimal averageVelocityLastTenSeconds = getAverage(
//	lastPrices.subList(lastPrices.size() - 10, lastPrices.size()));
//
//	BigDecimal standardDeviation = calculateStandardDeviation(startIndex, averageVelocity, prices);

    }
}
