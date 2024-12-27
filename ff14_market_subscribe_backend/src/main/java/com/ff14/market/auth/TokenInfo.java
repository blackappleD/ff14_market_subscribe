package com.ff14.market.auth;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenInfo {
    private String tokenValue;

    private String userId;

    private LocalDateTime expireAt;
}
