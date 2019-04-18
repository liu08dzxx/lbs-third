package com.vtradex.ehub.third.lbsthird.jtt809.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginResponse {
    private int result;
    
    private int verifyCode;   
}

