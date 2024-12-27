package com.ff14.market.dto;

import lombok.Data;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/27 17:29
 */
@Data
public class ItemPriceInfoGroupByWorld {

	private String worldName;

	private List<ItemPriceInfo> itemPriceInfoList;

}
