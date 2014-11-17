package com.hotpads.handler.admin;

import com.hotpads.datarouter.util.callsite.CallsiteAnalyzer;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.CodeMav;

public class CallsiteHandler extends BaseHandler {

	private static final String
			P_logPath = "logPath",
			P_maxResults = "maxResults",
			DEFAULT_logPath = "/mnt/logs/callsite.log";
	
	@Handler
	protected Mav handleDefault() {
		String logPath = params.optional(P_logPath, DEFAULT_logPath);
		Integer maxResults = params.optionalInteger(P_maxResults, 100);
		CallsiteAnalyzer callsiteAnalyzer = new CallsiteAnalyzer(logPath, maxResults);
		String report = callsiteAnalyzer.call();
		return new CodeMav(report);
	}
}
