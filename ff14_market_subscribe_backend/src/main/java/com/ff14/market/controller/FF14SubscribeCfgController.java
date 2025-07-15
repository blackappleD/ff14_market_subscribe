package com.ff14.market.controller;

import com.ff14.market.annotations.FF14ResponseBody;
import com.ff14.market.dto.SubscribeCfgReqDTO;
import com.ff14.market.dto.SubscribeCfgResDTO;
import com.ff14.market.service.FF14SubscribeCfgService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 9:13
 */
@RestController
@RequestMapping("/ff14/sub_cfg")
@FF14ResponseBody
public class FF14SubscribeCfgController {

	@Resource
	private FF14SubscribeCfgService ff14SubscribeCfgService;

	@PutMapping("/notify")
	public void modifyNotify(@RequestParam Boolean notify) {
		ff14SubscribeCfgService.modifyNotify(notify);
	}

	@PutMapping("/max_listings")
	public void updateMaxListings(@RequestParam Integer maxListings) {
		ff14SubscribeCfgService.updateMaxListings(maxListings);
	}

	@GetMapping
	public SubscribeCfgResDTO get() {
		return ff14SubscribeCfgService.get();
	}

	@PutMapping
	public void updateConfig(@RequestBody SubscribeCfgReqDTO reqDTO) {
		ff14SubscribeCfgService.update(reqDTO);
	}

}
