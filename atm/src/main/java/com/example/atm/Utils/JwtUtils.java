package com.example.atm.Utils;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌工具类
 * 基于 jjwt 0.13.0
 */
public class JwtUtils {

	/**
	 * JWT 签名密钥
	 */
	private static final String SECRET_KEY_STR = "5q2k6aG555uu77yIVGxpYXPns7vnu5/vvInkuLp2YWVkdeW8gOWPkeeahOWfuuS6jnNwcmluZ2Jvb3QrdnVl55qE5YWo5qCI6aG555uu";

	/**
	 * 过期时间：6 小时（毫秒）
	 */
	private static final long EXPIRATION_MS = 6 * 60 * 60 * 1000L;

	/**
	 * 基于密钥字符串构建 SecretKey
	 */
	private static final SecretKey SECRET_KEY =
			Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes(StandardCharsets.UTF_8));

	/**
	 * 生成 JWT 令牌
	 *
	 * @param claims 自定义载荷数据（键值对）
	 * @return JWT 令牌字符串
	 */
	public static String generateToken(Map<String, Object> claims) {
		Date now = new Date();
		Date expireTime = new Date(now.getTime() + EXPIRATION_MS);

		return Jwts.builder()
				.claims(claims)
				.issuedAt(now)
				.expiration(expireTime)
				.signWith(SECRET_KEY)
				.compact();
	}

	/**
	 * 解析 JWT 令牌，获取载荷中的所有声明
	 *
	 * @param token JWT 令牌字符串
	 * @return 载荷声明（Claims）
	 * @throws io.jsonwebtoken.JwtException 令牌无效或已过期时抛出
	 */
	public static Claims parseToken(String token) throws JwtException {
		return Jwts.parser()
				.verifyWith(SECRET_KEY)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}

