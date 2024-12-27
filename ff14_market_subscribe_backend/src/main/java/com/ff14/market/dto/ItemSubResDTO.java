package com.ff14.market.dto;

import lombok.Data;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/26 15:54
 */
@Data
public class ItemSubResDTO {

	private Long id;

	private ItemDTO item;

	private Long notifyThreshold;
}
