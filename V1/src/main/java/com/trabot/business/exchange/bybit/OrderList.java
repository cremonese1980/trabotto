package com.trabot.business.exchange.bybit;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrderList {
    
    
    private String category;
    private List<Map<String, Object>> request;
    


}
