package com.ff14.market.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/1/20 8:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubscribeCfgReqDTO extends LongIdDTO {

	private Boolean notify;

	private Integer maxListings;

}
