package com.hotpads.util.http.response.exception;

import com.hotpads.util.http.response.HotPadsHttpResponse;

@SuppressWarnings("serial")
public class HotPadsHttp5xxResponseException extends HotPadsHttpResponseException {

	public HotPadsHttp5xxResponseException(HotPadsHttpResponse response) {
		super(response);
	}

}
