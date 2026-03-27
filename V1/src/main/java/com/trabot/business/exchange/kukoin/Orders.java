package com.trabot.business.exchange.kukoin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Orders {
    
    private String symbol;
    private List<Order> orderList;

}
