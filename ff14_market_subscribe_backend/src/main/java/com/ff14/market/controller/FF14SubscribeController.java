package com.ff14.market.controller;

import com.ff14.market.annotations.FF14ResponseBody;
import com.ff14.market.dto.SubscribeGroupResDTO;
import com.ff14.market.dto.UserSubscribeGroupReqDTO;
import com.ff14.market.service.FF14SubscribeGroupService;
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
@FF14ResponseBody
public class FF14SubscribeController {

	@Resource
	private FF14SubscribeGroupService ff14SubscribeGroupService;

	@GetMapping
	public List<SubscribeGroupResDTO> get() {
		return ff14SubscribeGroupService.get();
	}

	@PostMapping
	public void modify(@RequestBody @Valid List<UserSubscribeGroupReqDTO> dto) {
		ff14SubscribeGroupService.modify(dto);
	}


}
