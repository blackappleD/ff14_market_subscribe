package com.ff14.market.exception;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/19 12:04
 */
public class FF14Exception extends RuntimeException {
	public FF14Exception(String message) {
		super(message);
	}

	public static class WorldException extends RuntimeException {

		public WorldException(String message) {
			super(message);
		}

		public static WorldException notFound(String worldName) {
			return new WorldException(CharSequenceUtil.format("世界 {} 不存在", worldName));
		}

		public static WorldException notFound() {
			return new WorldException("世界不存在");
		}
	}

	public static class UserException extends FF14Exception {

		public static UserException notFound(String account) {
			return new UserException("用户不存在：" + account);
		}

		public static UserException notFound() {
			return new UserException("用户不存在");
		}

		public static UserException passwordError() {
			return new UserException("密码错误");
		}

		public static UserException emailExists(String email) {
			return new UserException("邮箱已存在：" + email);
		}

		public static UserException emailBoundToAnotherUser() {
			return new UserException("邮箱已被其他用户绑定");
		}

		private UserException(String message) {
			super(message);
		}
	}

	public static class LoginException extends UserException {
		public static LoginException loginInvalid() {
			throw new LoginException("登录失败");
		}

		private LoginException(String message) {
			super(message);
		}
	}

}
