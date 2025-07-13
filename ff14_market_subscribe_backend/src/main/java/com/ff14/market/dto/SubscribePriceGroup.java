package com.ff14.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/27 17:48
 */
@Data
public class SubscribePriceGroup {

    private Long id;

    private String worldName;
    
    private WorldDTO world;

    private List<ItemPriceGroup> itemPriceGroups;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemPriceGroup {

        private Long id;

        private String name;

        private List<ItemPriceInfo> itemPriceInfoList;
    }
}
