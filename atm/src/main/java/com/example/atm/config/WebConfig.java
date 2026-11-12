package com.example.atm.config;

import com.example.atm.interceptor.AdminTokenInterceptor;
import com.example.atm.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	private final TokenInterceptor tokenInterceptor;
	private final AdminTokenInterceptor adminTokenInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tokenInterceptor)
				.addPathPatterns("/api/**")
				.excludePathPatterns("/api/atm/login", "/api/atm/register");
		registry.addInterceptor(adminTokenInterceptor)
				.addPathPatterns("/api/admin/**");
	}
}