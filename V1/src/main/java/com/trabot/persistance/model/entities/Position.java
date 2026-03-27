package com.trabot.persistance.model.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.trabot.persistance.model.enums.Action;
import com.trabot.persistance.model.enums.Side;
import com.trabot.persistance.model.enums.TMode;
import com.trabot.persistance.model.enums.TradeType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@NoArgsConstructor
public class Position {
    
    private Side side;
    private Action action;
    private TradeType tradeType;
    private String symbol;
    private TMode tMode;
    @Setter
    private BigDecimal price;
    @Setter
    private BigDecimal volume;
    private BigDecimal tpPrice;
    private BigDecimal slPrice;
    
    public Position(String symbol, BigDecimal volume) {
	
	this.symbol = symbol;
	this.volume = volume;
	
    }
    
    public String getEntrustPriceAsString() {
	
	return price.setScale(1, RoundingMode.UP).toString();
    }
    public String getEntrustVolumeAsString() {
	
	return volume.setScale(4, RoundingMode.UP).toString();
    }
    public String getTakerProfitPriceAsString() {
	
	return tpPrice.setScale(1, RoundingMode.UP).toString();
    }
    public String getStopLossPriceAsString() {
	
	return slPrice.setScale(1, RoundingMode.UP).toString();
    }
    

}
