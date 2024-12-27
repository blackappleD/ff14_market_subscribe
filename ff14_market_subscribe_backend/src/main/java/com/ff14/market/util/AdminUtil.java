package com.ff14.market.util;

import cn.hutool.extra.spring.SpringUtil;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.service.FF14UserService;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/27 15:23
 */
public class AdminUtil {

	public static FF14UserPO getCurrentUser() {

		return SpringUtil.getBean(FF14UserService.class).findById(AuthUtil.getLoginId());
	}
}
