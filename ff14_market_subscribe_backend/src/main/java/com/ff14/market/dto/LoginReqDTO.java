package com.ff14.market.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReqDTO {
	@NotBlank(message = "用户名/邮箱不能为空")
	private String userAccount;

	@NotBlank(message = "密码不能为空")
	private String password;
}