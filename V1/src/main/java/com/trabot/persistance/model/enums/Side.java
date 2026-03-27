package com.trabot.persistance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Side {
    
    LONG("Bid", "buy", "buy","Buy"),
    SHORT("Ask", "sell", "sell","Sell");
    
    private String value;
    private String okxValue;
    private String kukoinValue;
    private String bybitValue;

}
