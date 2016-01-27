package com.hotpads.util.http.client;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRedirectStrategy extends LaxRedirectStrategy{
	private static final Logger logger = LoggerFactory.getLogger(LoggingRedirectStrategy.class);

	@Override
	public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
	throws ProtocolException{
		HttpUriRequest redirect = super.getRedirect(request, response, context);
		logger.warn("Got a {}, will redirect {} to {}", response.getStatusLine(), request.getRequestLine(), redirect
				.getRequestLine());
		return redirect;
	}

}
