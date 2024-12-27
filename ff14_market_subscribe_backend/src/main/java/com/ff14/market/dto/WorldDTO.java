package com.ff14.market.dto;

import com.ff14.market.enums.WorldLevel;
import lombok.Data;

@Data
public class WorldDTO {
    private Long id;
    private Long worldId;
    private WorldLevel level;
    private String name;
}