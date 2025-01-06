package com.ff14.market.controller;

import com.ff14.market.annotations.FF14ResponseBody;
import com.ff14.market.dto.SubscribePriceGroup;
import com.ff14.market.service.FF14PriceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 9:13
 */
@RestController
@RequestMapping("/ff14/price")
@FF14ResponseBody
public class FF14PriceController {

	@Resource
	private FF14PriceService ff14PriceService;

	@GetMapping("/on_time")
	public List<SubscribePriceGroup> subscribeItemPriceOnTime() {
		return ff14PriceService.subscribeItemPriceOnTime();
	}


}
