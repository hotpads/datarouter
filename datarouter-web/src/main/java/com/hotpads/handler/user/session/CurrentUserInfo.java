package com.hotpads.handler.user.session;

import javax.servlet.http.HttpServletRequest;

public interface CurrentUserInfo{

	 String getEmail(HttpServletRequest request);

}
