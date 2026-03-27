package com.trabot.persistance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeType {
    
    MARKET("Market","market", "market", "Market"),
    LIMIT("Limit", "limit", "limit", "Limit"),
    POST_ONLY("post_only", "post_only", "", ""),
    FOK("fok", "fok", "FOK", ""),
    IOC("ioc", "ioc", "IOC", "IOC"),
    GTC("", "", "GTC", "GTC");
    
    private String value;
    private String okxValue;
    private String kukoinValue;
    private String bybitValue;

}
