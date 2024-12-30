package com.ff14.market.auth;

import com.ff14.market.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class AuthInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		AuthUtil.clearThreadCache();
		if (handler instanceof HandlerMethod) {
			AuthUtil.setRequest(request);
			return true;
		}
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		try {
			HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
		} finally {
			// 每次请求完成后，清理token
			AuthUtil.clearThreadCache();
		}
	}
}
