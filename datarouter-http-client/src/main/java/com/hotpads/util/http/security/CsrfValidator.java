package com.hotpads.util.http.security;

import javax.servlet.http.HttpServletRequest;

public interface CsrfValidator{

	public boolean check(HttpServletRequest request);

	public Long getRequestTimeMs(HttpServletRequest request);
}
