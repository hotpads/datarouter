package com.hotpads.websocket;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.exception.analysis.HttpHeaders;

public class WebSocketTool{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketTool.class);

	public static boolean isHandShakeRequest(HttpServletRequest req){
		String upgrade = req.getHeader(HttpHeaders.UPGRADE);
		logger.debug("upgrade header value: {}", upgrade);
		String connection = req.getHeader(HttpHeaders.CONNECTION);
		logger.debug("connection header value: {}", connection);
		String secWbsocketVersionHeaderValue = req.getHeader(HttpHeaders.SEC_WEBSOCKET_VERSION);
		logger.debug("sec_websocket_version header value: {}", secWbsocketVersionHeaderValue);
		return DrStringTool.notEmpty(secWbsocketVersionHeaderValue);
	}

}
