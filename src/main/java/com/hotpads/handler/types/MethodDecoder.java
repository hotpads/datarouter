package com.hotpads.handler.types;

import javax.servlet.http.HttpServletRequest;

public interface MethodDecoder{

	Object[] decode(HttpServletRequest request);
}
