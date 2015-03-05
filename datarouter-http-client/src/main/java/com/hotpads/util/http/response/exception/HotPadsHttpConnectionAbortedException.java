package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpConnectionAbortedException extends HotPadsHttpException {
	
	public HotPadsHttpConnectionAbortedException(Exception e, long requestExecutedMs) {
		super("HTTP connection aborted after " + (System.currentTimeMillis() - requestExecutedMs) + "ms", e);
	}
}
