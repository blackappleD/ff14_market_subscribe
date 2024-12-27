package com.ff14.market.dto;

import lombok.Data;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/17 14:35
 */
@Data
public class ItemPriceInfo {
	private String id;
	private String name;
	private long lastReviewTime;
	private long pricePerUnit;
	private int quantity;
	private int stainId;
	private String worldName;
	private int worldId;
	private String creatorName;
	private String creatorId;
	private boolean hq;
	private boolean isCrafted;
	private String listingId;
	private boolean onMannequin;
	private int retainerCity;
	private String retainerId;
	private String retainerName;
	private String sellerId;
	private long total;
	private int tax;


	private boolean lowerThreshold;
}

