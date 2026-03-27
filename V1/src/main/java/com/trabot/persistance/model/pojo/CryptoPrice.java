package com.trabot.persistance.model.pojo;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.Data;

@Data
public class CryptoPrice {
    
    private String symbol;
    private BigDecimal price;
    
    
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	CryptoPrice other = (CryptoPrice) obj;
	return Objects.equals(symbol, other.symbol);
    }
    @Override
    public int hashCode() {
	return Objects.hash(symbol);
    }
   

}
