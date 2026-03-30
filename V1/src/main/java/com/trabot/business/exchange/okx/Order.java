package com.trabot.business.exchange.okx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    
    private String instId;
    private String tdMode;
    private String side;
    private String ordType;
    private String sz;
    private String px;
    


}
