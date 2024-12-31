package com.ff14.market.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.ff14.market.dto.WorldDTO;
import com.ff14.market.service.FF14WorldService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ff14/world")
public class FF14WorldController {

	@Resource
	private FF14WorldService ff14WorldService;

	@GetMapping("/search")
	public List<WorldDTO> searchWorlds(@RequestParam String name) {
		if (CharSequenceUtil.isBlank(name)) {
			return new ArrayList<>();
		}
		return ff14WorldService.searchWorlds(name);
	}
}