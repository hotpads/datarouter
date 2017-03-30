package com.hotpads.handler.user.session;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public interface CurrentUserInfo{

	 String getEmail(HttpServletRequest request);

	 Optional<String> getUserToken(HttpServletRequest request);

}
