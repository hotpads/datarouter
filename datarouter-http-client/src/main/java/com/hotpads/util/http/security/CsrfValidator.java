package com.hotpads.util.http.security;

public interface CsrfValidator{

	public boolean check(String csrfToken, String cipherIv, String apiKey);

	public Long getRequestTimeMs(String csrfToken, String csrfIv);
}
