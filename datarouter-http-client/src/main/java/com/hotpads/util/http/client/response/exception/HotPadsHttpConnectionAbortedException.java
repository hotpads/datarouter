package com.hotpads.util.http.client.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpConnectionAbortedException extends HotPadsHttpException {
	
	public HotPadsHttpConnectionAbortedException(Exception e) {
		super("HTTP connection aborted", e);
	}
}
