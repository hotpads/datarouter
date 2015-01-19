package com.hotpads.redirect;

import javax.servlet.http.HttpServletRequest;

public interface UrlShortenerService{

	String getFullUrlAndRegisterVisit(String token, HttpServletRequest request);

	String shortenUrl(String url);

	String getBeforeToken();

}
