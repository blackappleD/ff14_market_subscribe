package com.ff14.market.controller;

import com.ff14.market.annotations.FF14ResponseBody;
import com.ff14.market.service.FF14MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2025/1/27 16:48
 */
@RestController
@RequestMapping("/ff14/notify")
@FF14ResponseBody
public class FF14NotifyController {

	private FF14MailService ff14MailService;

	/**
	 * 精炼度100通知
	 *
	 * @param window
	 */
	@GetMapping("/refine")
	public void test(@RequestParam String window) {
		System.out.println("接收到了notify请求:" + window);
	}

}
