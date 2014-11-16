package com.hotpads.handler.admin;

import com.hotpads.datarouter.util.callsite.CallsiteAnalyzer;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.CodeMav;

public class CallsiteHandler extends BaseHandler {

	private static final String
			P_logPath = "logPath",
			DEFAULT_logPath = "/mnt/logs/callsite.log";
	
	@Handler
	protected Mav handleDefault() {
		String logPath = params.optional(P_logPath, DEFAULT_logPath);
		CallsiteAnalyzer callsiteAnalyzer = new CallsiteAnalyzer(logPath);
		String report = callsiteAnalyzer.call();
		return new CodeMav(report);
	}
}
