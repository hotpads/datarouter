package com.hotpads.util.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;

public class HttpHeaders {

	public static String
		ACCEPT_CHARSET = "accept-charset",
		ACCEPT_ENCODING = "accept-encoding",
		ACCEPT_LANGUAGE = "accept-language",
		ACCEPT = "accept",
		CACHE_CONTROL = "cache-control",
		CONNECTION = "connection",
		CONTENT_ENCODING = "content-encoding",
		CONTENT_LANGUAGE = "content-language",
		CONTENT_LENGTH = "content-length",
		CONTENT_TYPE = "content-type",
		COOKIE = "cookie",
		DNT = "dnt",
		HOST = "host",
		IF_MODIFIED_SINCE = "if-modified-since",
		ORIGIN = "origin",
		PRAGMA = "pragma",
		REFERER = "referer",
		UPGRADE = "upgrade",
		USER_AGENT = "user-agent",
		SEC_WEBSOCKET_VERSION = "sec-websocket-version",
		X_CLIENT_IP = "x-client-ip", //custom header that node servers send with the client's ip
		X_FORWARDED_FOR = "x-forwarded-for",
		X_REQUESTED_WITH = "x-requested-with";
		

	private static final String[] recordedHeaders = {
		ACCEPT_CHARSET,
		ACCEPT_ENCODING,
		ACCEPT_LANGUAGE,
		ACCEPT,
		CACHE_CONTROL,
		CONNECTION,
		CONTENT_ENCODING,
		CONTENT_LANGUAGE,
		CONTENT_LENGTH,
		CONTENT_TYPE,
		COOKIE,
		DNT,
		HOST,
		IF_MODIFIED_SINCE,
		ORIGIN,
		PRAGMA,
		REFERER,
		USER_AGENT,
		X_FORWARDED_FOR,
		X_REQUESTED_WITH
	};

	private Map<String, String> headerMap = new HashMap<>();

	public HttpHeaders(HttpServletRequest request) {
		Joiner listJoiner = Joiner.on(", ");
		if (request != null) {
			List<String> tmpHeaders = Collections.list(request.getHeaderNames());
			for (String headerKey : recordedHeaders) {
				if (tmpHeaders.remove(headerKey)) {
					headerMap.put(headerKey, listJoiner.join(Collections.list(request.getHeaders(headerKey))));
				}
			}
			StringBuilder othersBuilder = new StringBuilder();
			for (String header : tmpHeaders) {
				othersBuilder.append(header);
				othersBuilder.append(": ");
				othersBuilder.append(listJoiner.join(Collections.list(request.getHeaders(header))));
				othersBuilder.append(", ");
			}
			String others = othersBuilder.toString();
			headerMap.put("others", others);
		}
	}

	public String getAcceptCharset(){
		return headerMap.get(ACCEPT_CHARSET);
	}

	public String getAcceptEncoding(){
		return headerMap.get(ACCEPT_ENCODING);
	}

	public String getAcceptLanguage(){
		return headerMap.get(ACCEPT_LANGUAGE);
	}

	public String getAccept(){
		return headerMap.get(ACCEPT);
	}

	public String getCacheControl(){
		return headerMap.get(CACHE_CONTROL);
	}

	public String getConnection(){
		return headerMap.get(CONNECTION);
	}

	public String getContentEncoding(){
		return headerMap.get(CONTENT_ENCODING);
	}

	public String getContentLanguage(){
		return headerMap.get(CONTENT_LANGUAGE);
	}

	public String getContentLength(){
		return headerMap.get(CONTENT_LENGTH);
	}

	public String getContentType(){
		return headerMap.get(CONTENT_TYPE);
	}

	public String getCookie(){
		return headerMap.get(COOKIE);
	}

	public String getDnt(){
		return headerMap.get(DNT);
	}

	public String getHost(){
		return headerMap.get(HOST);
	}

	public String getIfModifiedSince(){
		return headerMap.get(IF_MODIFIED_SINCE);
	}

	public String getOrigin(){
		return headerMap.get(ORIGIN);
	}

	public String getPragma(){
		return headerMap.get(PRAGMA);
	}

	public String getReferer(){
		return headerMap.get(REFERER);
	}

	public String getUserAgent(){
		return headerMap.get(USER_AGENT);
	}

	public String getXForwardedFor(){
		return headerMap.get(X_FORWARDED_FOR);
	}

	public String getXRequestedWith() {
		return headerMap.get(X_REQUESTED_WITH);
	}

	public String getOthers() {
		return headerMap.get("others");
	}
}
