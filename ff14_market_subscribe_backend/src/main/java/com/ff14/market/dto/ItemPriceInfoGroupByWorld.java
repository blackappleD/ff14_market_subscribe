package com.ff14.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

	@Schema(description = "区服名称")
	private String worldName;

	@Schema(description = "物价信息")
	private List<ItemPriceInfo> itemPriceInfoList;

}
