package com.example.atm.interceptor;


import com.example.atm.Utils.CurrentUser;
import com.example.atm.Utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import java.io.IOException;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
	/**
	 * 在请求处理之前进行拦截，验证请求的令牌。
	 *
	 * @param request 当前HTTP请求对象
	 * @param response 当前HTTP响应对象
	 * @param handler 被调用的处理器对象
	 * @return 如果请求被允许继续处理则返回true，否则返回false
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
	                         @NonNull Object handler) throws IOException {
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;   // 跨域预检不带 token

		String token = request.getHeader("token");
		if (token == null || token.isEmpty()) {
			return reject(response, "未登录");
		}
		try {
			Claims claims = JwtUtils.parseToken(token);
			request.setAttribute(CurrentUser.ATTR, claims.get("card", String.class));  // 后面比对要用
		} catch (JwtException e) {
			return reject(response, "登录已失效，请重新登录");
		}
		return true;
	}

	private boolean reject(HttpServletResponse response, String msg) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
		return false;
	}

}