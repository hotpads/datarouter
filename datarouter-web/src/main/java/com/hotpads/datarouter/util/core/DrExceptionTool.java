package com.hotpads.datarouter.util.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrXMLStringTool;

public class DrExceptionTool {
	private static final Logger log = LoggerFactory.getLogger(DrExceptionTool.class);

	/**
	 * This should NOT be used for logging. Instead of this use
	 * <br>
	 * <br>
	 * <code>logger.warn("CustomExceptionResolver caught an exception:", ex)</code>
	 * <br>
	 * <br>
	 * or at least(If you have nothing to say)
	 * <br>
	 * <br>
	 * <code>logger.warn("", ex);</code>
	 */
	public static String getStackTraceAsString(Throwable e){
		ThrowableProxy proxy = new ThrowableProxy(e);
		return proxy.getCauseStackTraceAsString();
	}
	
	public static String getStackTraceStringForHtmlPreBlock(Throwable e){
		String stackTrace = (e != null) ? getStackTraceAsString(e) : "No exception defined.";
		return getColorized(stackTrace);
	}
	
	public static String getColorized(String stackTrace) {
		stackTrace = DrXMLStringTool.escapeXml(stackTrace);
		String highlightOpener = "<span style='color:red;font-weight:bold;font-size:1.5em;'>";
		String highlightCloser = "</span>";
		return stackTrace.replace("hotpads", highlightOpener + "hotpads" + highlightCloser);
	}

	public static String getShortStackTrace(String fullStackTrace) {
		BufferedReader br = new BufferedReader(new StringReader(fullStackTrace));
		String line;
		String key = "com.hotpads";
		boolean none = false;
		int nb = 0;
		StringBuilder builder = new StringBuilder();
		try {
			while ((line = br.readLine()) != null) {
				if (line.contains(key) && nb < 10) {
					none = false;
					nb++;
					builder.append(line);
					builder.append('\n');
				} else {
					if (!none) {
						builder.append("[...]");
						builder.append('\n');
						none = true;
					}
				}
			}
			return getColorized(builder.toString());
		} catch (IOException e) {
			log.warn("Error building short stack trace", e);
			return fullStackTrace;
		}
	}

}
