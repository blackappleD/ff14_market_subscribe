package com.ff14.market.controller;

import com.ff14.market.dto.UserSubscribeReqDTO;
import com.ff14.market.dto.UserSubscribeResDTO;
import com.ff14.market.service.FF14UserSubscribeService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 9:13
 */
@RestController
@RequestMapping("/ff14/subscribe")
public class FF14SubscribeController {

	@Resource
	private FF14UserSubscribeService ff14UserSubscribeService;

	@GetMapping
	public List<UserSubscribeResDTO> get() {
		return ff14UserSubscribeService.get();
	}

	@PostMapping
	public void modify(@RequestBody @Valid List<UserSubscribeReqDTO> dto) {
		ff14UserSubscribeService.modify(dto);
	}
}
