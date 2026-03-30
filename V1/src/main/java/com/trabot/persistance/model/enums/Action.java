package com.trabot.persistance.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Action {
    
    OPEN("Open"),
    CLOSE("Close");
    
    private String value;

}
