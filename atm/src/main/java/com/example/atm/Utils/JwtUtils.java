package com.example.atm.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌工具类
 * 基于 jjwt 0.13.0
 * <p>
 * 签名密钥从配置项 atm.jwt.secret 读取：
 * 线上由环境变量 ATM_JWT_SECRET 提供（Spring 会把 ATM_JWT_SECRET 映射为 atm.jwt.secret），
 * 本地开发写在 config/application-dev.yaml 中。密钥不进仓库。
 */
@Component
public class JwtUtils {

	/**
	 * 过期时间：15 分钟（毫秒）
	 */
	private static final long EXPIRATION_MS = 15 * 60 * 1000L;

	/**
	 * HS256 要求密钥至少 256 位，即 32 字节
	 */
	private static final int MIN_SECRET_BYTES = 32;

	private final SecretKey secretKey;

	/**
	 * 启动时读取并校验密钥。密钥缺失、为空或过短都直接让应用启动失败，
	 * 不会带着弱密钥运行。异常信息里不输出密钥内容。
	 */
	public JwtUtils(@Value("${atm.jwt.secret}") String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("未配置 JWT 密钥：请设置环境变量 ATM_JWT_SECRET");
		}
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("JWT 密钥过短：至少需要 " + MIN_SECRET_BYTES + " 个字符");
		}
		this.secretKey = Keys.hmacShaKeyFor(bytes);
	}

	/**
	 * 生成 JWT 令牌
	 *
	 * @param claims 自定义载荷数据（键值对）
	 * @return JWT 令牌字符串
	 */
	public String generateToken(Map<String, Object> claims) {
		Date now = new Date();
		Date expireTime = new Date(now.getTime() + EXPIRATION_MS);

		return Jwts.builder()
				.claims(claims)
				.issuedAt(now)
				.expiration(expireTime)
				.signWith(secretKey)
				.compact();
	}

	/**
	 * 解析 JWT 令牌，获取载荷中的所有声明
	 *
	 * @param token JWT 令牌字符串
	 * @return 载荷声明（Claims）
	 * @throws io.jsonwebtoken.JwtException 令牌无效或已过期时抛出
	 */
	public Claims parseToken(String token) throws JwtException {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}