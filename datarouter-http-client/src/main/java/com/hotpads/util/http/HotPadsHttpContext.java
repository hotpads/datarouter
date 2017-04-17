package com.hotpads.util.http;

import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.response.HotPadsHttpResponse;
import com.hotpads.util.http.response.exception.HotPadsHttpException;
import com.hotpads.util.http.response.exception.HotPadsHttpResponseException;

/**
 * Holds http request and resulting response/exception
 */
public class HotPadsHttpContext{

	public final HotPadsHttpRequest request;
	public final HotPadsHttpResponse response;
	public final HotPadsHttpException exception;

	public HotPadsHttpContext(HotPadsHttpRequest request, HotPadsHttpResponse response, HotPadsHttpException exception){
		this.request = request;
		this.exception = exception;
		if(exception instanceof HotPadsHttpResponseException){
			this.response = ((HotPadsHttpResponseException)exception).getResponse();
		}else{
			this.response = response;
		}
	}

}
