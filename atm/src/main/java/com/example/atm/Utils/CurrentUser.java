package com.example.atm.Utils;

import com.example.atm.common.exception.ForbiddenException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CurrentUser {
	public static final String ATTR = "tokenCard";
	private CurrentUser() {}

	/** 当前登录用户的卡号（来自 token，不是客户端传的） */
	public static String card() {
		var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		Object c = attrs.getRequest().getAttribute(ATTR);
		return c == null ? null : c.toString();
	}

	/** 要操作的卡号必须就是自己的，否则 403 */
	public static void assertSelf(String card) {
		String me = card();
		if (me == null || !me.equals(card)) {
			throw new ForbiddenException("无权操作其他账户");
		}
	}
}