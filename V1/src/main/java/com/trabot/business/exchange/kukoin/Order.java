package com.trabot.business.exchange.kukoin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Order {
    
    private String clientOid;
    private String side; //buy
    @Setter
    private String symbol;
    private String type; //market
    private String tradeType; //TRADE
    private String price;
    private String size;
    @Setter
    private String timeInForce;//IOC FOK
    private String funds;
    
    public Order(String clientOid, String side, String type, String price, String size) {

	this.clientOid = clientOid;
	this.side = side;
	this.type = type;
	this.price = price;
	this.size = size;
    }
    
    public Order(String clientOid, String side,  String type, String price, String size,
	    String timeInForce) {

	this.clientOid = clientOid;
	this.side = side;
	this.type = type;
	this.price = price;
	this.size = size;
	this.timeInForce = timeInForce;
    }
    
    
    


}
