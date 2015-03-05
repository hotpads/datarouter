package com.hotpads.util.http.response.exception;

@SuppressWarnings("serial")
public class HotPadsHttpConnectionAbortedException extends HotPadsHttpException {
	
	public HotPadsHttpConnectionAbortedException(Exception ex, long requestStartTimeMs) {
		super("HTTP connection aborted after " + (System.currentTimeMillis() - requestStartTimeMs) + "ms", ex);
	}
}
