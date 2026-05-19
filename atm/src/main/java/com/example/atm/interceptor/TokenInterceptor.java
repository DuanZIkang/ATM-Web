package com.example.atm.interceptor;


import com.example.atm.Utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
	public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler){

		// 从请求头中提取令牌
		String token = request.getHeader("token");
		if (token == null || token.isEmpty()) {
			// 如果令牌为空，记录事件并返回未授权状态
			log.info("令牌为空，用户未登录");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		try {
			// 解析并验证令牌
			JwtUtils.parseToken(token);
		} catch (JwtException e) {
			// 如果令牌无效，记录事件并返回未授权状态
			log.info("令牌校验失败");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
		// 如果令牌有效，记录事件并允许请求继续
		log.info("令牌合法，放行");
		return true;
	}

}