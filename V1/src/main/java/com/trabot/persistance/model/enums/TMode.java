package com.trabot.persistance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TMode {
    
    CROSS("cross"),
    ISOLATED("isolated"),
    CASH("cash");
    
    private String value;

}
