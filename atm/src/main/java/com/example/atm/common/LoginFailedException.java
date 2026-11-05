package com.example.atm.common;

import com.example.atm.common.exception.AccountException;

/**
 * 登录失败（卡号不存在 或 密码错误）。
 * 两种情况必须使用同一个异常、同一句提示、同一个状态码(401)，
 * 否则攻击者可以通过响应差异枚举出哪些卡号是存在的。
 */
public class LoginFailedException extends AccountException {
	public LoginFailedException(String message) {
		super(message);
	}
}
