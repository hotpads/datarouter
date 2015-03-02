package com.hotpads.util.http.response.exception;

import com.hotpads.util.http.response.HotPadsHttpResponse;

@SuppressWarnings("serial")
public class HotPadsHttp4xxResponseException extends HotPadsHttpResponseException {

	public HotPadsHttp4xxResponseException(HotPadsHttpResponse response) {
		super(response);
	}

}
