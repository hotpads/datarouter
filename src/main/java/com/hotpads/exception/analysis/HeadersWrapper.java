package com.hotpads.exception.analysis;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;
import com.hotpads.util.core.MapTool;

public class HeadersWrapper {
	
	private static final String[] recordedHeaders = {
			"accept-charset",
			"accept-encoding",
			"accept-language",
			"accept",
			"cache-control",
			"connection",
			"content-encoding",
			"content-language",
			"content-length",
			"content-type",
			"cookie",
			"dnt",
			"host",
			"if-modified-since",
			"origin",
			"pragma",
			"referer",
			"user-agent",
			"x-forwarded-for",
			"x-requested-with"
	};
	
	private Map<String, String> headerMap = MapTool.create();
	
	public HeadersWrapper(HttpServletRequest request) {
		Joiner listJoiner = Joiner.on(", ");
		List<String> tmpHeaders = Collections.list(request.getHeaderNames());
		for (String headerKey : recordedHeaders) {
			tmpHeaders.remove(headerKey);
			headerMap.put(headerKey, listJoiner.join(Collections.list(request.getHeaders(headerKey))));
		}
		StringBuilder othersBuilder = new StringBuilder();
		for (String header : tmpHeaders) {
			othersBuilder.append(header);
			othersBuilder.append(": ");
			othersBuilder.append(listJoiner.join(Collections.list(request.getHeaders(header))));
			othersBuilder.append(", ");
		}
		headerMap.put("others", othersBuilder.toString());
	}

	public String getAcceptCharset(){
		return headerMap.get("accept-charset");
	}

	public String getAcceptEncoding(){
		return headerMap.get("accept-encoding");
	}

	public String getAcceptLanguage(){
		return headerMap.get("accept-language");
	}

	public String getAccept(){
		return headerMap.get("accept");
	}

	public String getCacheControl(){
		return headerMap.get("cache-control");
	}

	public String getConnection(){
		return headerMap.get("connection");
	}

	public String getContentEncoding(){
		return headerMap.get("content-encoding");
	}

	public String getContentLanguage(){
		return headerMap.get("content-language");
	}

	public String getContentLength(){
		return headerMap.get("content-length");
	}

	public String getContentType(){
		return headerMap.get("content-type");
	}

	public String getCookie(){
		return headerMap.get("cookie");
	}

	public String getDnt(){
		return headerMap.get("dnt");
	}

	public String getHost(){
		return headerMap.get("host");
	}

	public String getIfModifiedSince(){
		return headerMap.get("if-modified-since");
	}

	public String getOrigin(){
		return headerMap.get("origin");
	}

	public String getPragma(){
		return headerMap.get("pragma");
	}

	public String getReferer(){
		return headerMap.get("referer");
	}

	public String getUserAgent(){
		return headerMap.get("user-agent");
	}

	public String getXForwardedFor(){
		return headerMap.get("x-forwarded-for");
	}

	public String getXRequestedWith() {
		return headerMap.get("x-requested-with");
	}

	public String getOthers() {
		return headerMap.get("others");
	}
}
