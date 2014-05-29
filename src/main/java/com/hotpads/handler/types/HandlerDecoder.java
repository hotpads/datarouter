package com.hotpads.handler.types;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

public interface HandlerDecoder{

	Object[] decode(HttpServletRequest request, Method method);
}
