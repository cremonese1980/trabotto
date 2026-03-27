package com.trabot.persistance.model.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
@Entity
@Setter
public class InstantPrice {
    
//    private long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  long id;
    
    private long timestamp;

    private  String pair;

    private  BigDecimal price;

    
    private BigDecimal velocity;
    private BigDecimal acceleration;
    private BigDecimal derivativeAcceleration;

    private BigDecimal averageVelocityPercent;
    private BigDecimal averageVelocityPercent30;
    private BigDecimal averageVelocityPercent10;
    
    @Override
    public String toString() {
	return "InstantPrice [timestamp=" + new Date(timestamp) + ", pair=" + pair + ", price=" + price + ", velocity=" + getVelocityPercent()
		+ "%, acceleration=" + getAccelerationPercent() + "%, derivativeAcceleration=" + getDerivativeAccelerationPercent() + "%]";
    }


    public BigDecimal getVelocityPercent() {
        return getPercentage(velocity, price);
    }
    
    public BigDecimal getAccelerationPercent() {
        return getPercentage(acceleration, price);
    }
    
    public BigDecimal getDerivativeAccelerationPercent() {
        return getPercentage(acceleration, price);
    }
    
    @Override
    public int hashCode() {
	return Objects.hash(pair, id);
    }
    
    
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	InstantPrice other = (InstantPrice) obj;
	return Objects.equals(pair, other.pair) && id == other.id;
    }
    
    
    
    private BigDecimal getPercentage(BigDecimal variation, BigDecimal price) {
	
	if(variation == null) {
	    return null;
	}
	
	return variation.divide(price, 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
	
    }


    
    
    

}
