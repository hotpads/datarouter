package com.hotpads.handler.exception;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.WebAppName;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.exception.analysis.HttpRequestRecord;
import com.hotpads.exception.analysis.HttpRequestRecordKey;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.profile.count.collection.Counters;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.collections.Pair;

@Singleton
public class ExceptionHandlingFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

	public static final String ATTRIBUTE_EXCEPTION_RECORD_NODE = "exceptionRecordNode";
	public static final String ATTRIBUTE_REQUEST_RECORD_NODE = "requestRecordNode";
	public static final String ATTRIBUTE_EXCEPTION_HANDLING_CONFIG = "exceptionHandlingConfig";
	public static final String ATTRIBUTE_PARALLEL_API_CALLER = "parallelApiCaller";
	public static final String ATTRIBUTE_WEBAPP_NAME = "webAppName";

	public static final String REQUEST_RECEIVED_AT = "receivedAt";

	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode;
	@Inject
	private IndexedSortedMapStorageNode<HttpRequestRecordKey, HttpRequestRecord> httpRequestRecordNode;
	@Inject
	private ParallelApiCaller apiCaller;
	@Inject
	private WebAppName webAppName;

	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig filterConfig) throws ServletException{
		if(exceptionRecordNode == null){
			ServletContext sc = filterConfig.getServletContext();
			exceptionRecordNode = (SortedMapStorageNode<ExceptionRecordKey,ExceptionRecord>)sc
					.getAttribute(ATTRIBUTE_EXCEPTION_RECORD_NODE);
			httpRequestRecordNode = (IndexedSortedMapStorageNode<HttpRequestRecordKey,HttpRequestRecord>)sc
					.getAttribute(ATTRIBUTE_REQUEST_RECORD_NODE);
			exceptionHandlingConfig = (ExceptionHandlingConfig)sc.getAttribute(ATTRIBUTE_EXCEPTION_HANDLING_CONFIG);
			apiCaller = (ParallelApiCaller)sc.getAttribute(ATTRIBUTE_PARALLEL_API_CALLER);
			webAppName = (WebAppName)sc.getAttribute(ATTRIBUTE_WEBAPP_NAME);
		}
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		HttpServletResponse response = (HttpServletResponse) res;
		Date receivedAt = new Date();
		req.setAttribute(REQUEST_RECEIVED_AT, receivedAt);
		try {
			fc.doFilter(req, res);
		}catch(OutOfMemoryError error){
			logger.error("The current number of threads at OOM are:" + ManagementFactory.getThreadMXBean().getThreadCount());
			dumpAllStackTraces();
			throw error;
		}catch (Exception e) {
			ExceptionCounters.inc("Filter");
			ExceptionCounters.inc(e.getClass().getName());
			ExceptionCounters.inc("Filter " + e.getClass().getName());
			HttpServletRequest request = (HttpServletRequest) req;
			logger.warn("ExceptionHandlingFilter caught an exception:", e);
			writeExceptionToResponseWriter(response, e, request);
			if(exceptionHandlingConfig.shouldPersistExceptionRecords(request, e)) {
				recordExceptionAndRequestNotification(request, e, receivedAt);
			}
		}
		Counters.inc(webAppName + " response " + response.getStatus());
	}

	private static void dumpAllStackTraces() throws IOException{
		long timeMiliSec = System.currentTimeMillis();
		BufferedWriter out = new BufferedWriter(new FileWriter("/tmp/StackTrace" + timeMiliSec + ".log"));
		Map<Thread,StackTraceElement[]> liveThreads = Thread.getAllStackTraces();
		for(Entry<Thread,StackTraceElement[]> thread : liveThreads.entrySet()){
			out.append("Thread " + thread.getKey().getName() + "\n");
			for(StackTraceElement element : thread.getValue()){
				out.append("\tat " + element + "\n");
			}
		}
		out.close();
	}

	private void recordExceptionAndRequestNotification(HttpServletRequest request, Exception e, Date receivedAt) {
		try {
			ExceptionRecord exceptionRecord = new ExceptionRecord(
					exceptionHandlingConfig.getServerName(),
					ExceptionTool.getStackTraceAsString(e),
					e.getClass().getName());
			exceptionRecordNode.put(exceptionRecord, null);
			String domain = exceptionHandlingConfig.isDevServer() ? "localhost:8443" : "hotpads.com";
			logger.warn("Exception recorded (https://" + domain + "/analytics/exception/details?exceptionRecord="
					+ exceptionRecord.getKey().getId() + ")");
			String place = null;
			Integer lineNumber = null;
			Pair<String, Integer> pair = searchJspName(e);
			if (pair.getLeft() == null) {
				pair = searchClassName(e);
			}
			place = pair.getLeft();
			lineNumber = pair.getRight();
			ExceptionCounters.inc(place);
			ExceptionCounters.inc("Filter " + place);
			ExceptionCounters.inc(e.getClass().getName() + " " + place);
			ExceptionCounters.inc("Filter " + e.getClass().getName() + " " + place);
			HttpRequestRecord httpRequestRecord = new HttpRequestRecord(
					receivedAt,
					exceptionRecord.getKey().getId(),
					place,
					null,
					lineNumber == null ? -1 : lineNumber,
							request,
							"unknown user roles",
							-1l
					);
			httpRequestRecordNode.put(httpRequestRecord, null);
			addNotificationRequestToQueue(request, e, exceptionRecord, place);
		} catch (Exception ex) {
			logger.error("Exception while logging", ex);
		}
	}

	private Pair<String, Integer> searchClassName(Exception e) {
		String place;
		Integer lineNumber = null;
		Throwable cause = e;
		do {
			for (StackTraceElement element : cause.getStackTrace()) {
				if (element.getClassName().contains("com.hotpads")) {
					lineNumber = element.getLineNumber();
					place = element.getClassName();
					return Pair.create(place, lineNumber);
				}
			}
			cause = cause.getCause();
		} while (cause != null);
		return Pair.create(null, null);
	}

	private Pair<String, Integer> searchJspName(Throwable e) {
		String place;
		Integer lineNumber = null;
		Throwable cause = e;
		do {
			String key = "An exception occurred processing JSP page ";
			if (cause.getMessage() == null) {
				cause = cause.getCause();
				continue;
			}
			int indexOfBegin = cause.getMessage().indexOf(key);
			if (indexOfBegin > -1) {
				String key2 = " at line ";
				int i = cause.getMessage().indexOf(key2);
				int endLine = cause.getMessage().indexOf("\n");
				place = cause.getMessage().substring(indexOfBegin + key.length(), i);
				try {
					lineNumber = Integer.parseInt(cause.getMessage().substring(i + key2.length(), endLine));
				} catch(NumberFormatException ex) {}
				return Pair.create(place, lineNumber);
			}
			/* An error occurred at line: 3 in the jsp file: /WEB-INF/jsp/jspError.jsp */
			String keyInTheJspFile = " in the jsp file: ";
			indexOfBegin = cause.getMessage().indexOf(keyInTheJspFile);
			if (indexOfBegin > -1) {
				String key2 = " at line: ";
				int i = cause.getMessage().indexOf(key2);
				int endLine = cause.getMessage().indexOf('\n', indexOfBegin);
				place = cause.getMessage().substring(indexOfBegin + keyInTheJspFile.length(), endLine);
				try {
					lineNumber = Integer.parseInt(cause.getMessage().substring(i + key2.length(), indexOfBegin));
				} catch(NumberFormatException ex) {}
				return Pair.create(place, lineNumber);
			}
			place = getJSPName(cause.getMessage());
			if (place != null) {
				return Pair.create(place, lineNumber);
			}
			for (StackTraceElement element : cause.getStackTrace()) {
				place = getJSPName(element.getClassName());
				if (place != null) {
					return Pair.create(place, lineNumber);
				}
			}
			cause = cause.getCause();
		} while (cause != null);
		return Pair.create(null, null);
	}

	private String getJSPName(String string) {
		if (string == null) {
			return null;
		}
		String key = "WEB_002dINF";
		int i;
		i = string.indexOf(key);
		if (i > -1) {
			String jspName = string.substring(i);
			jspName = jspName.replaceAll("\\.", "/");
			jspName = jspName.replaceAll("_002d", "-");
			jspName = jspName.replaceAll("_", ".");
			return "/" + jspName;
		}
		return null;
	}

	private void addNotificationRequestToQueue(HttpServletRequest request, Exception exception,
			ExceptionRecord exceptionRecord, String exceptionPlace){
		if (exceptionHandlingConfig.shouldReportError(request, exception)) {
			apiCaller.add(new NotificationRequest(
					new NotificationUserId(
							NotificationUserType.EMAIL,
							exceptionHandlingConfig.getRecipientEmail()),
							exceptionHandlingConfig.getServerErrorNotificationType(),
							exceptionRecord.getKey().getId(),
							exceptionPlace),
							exceptionRecord);
		}
	}

	private void writeExceptionToResponseWriter(HttpServletResponse response, Exception exception,
			HttpServletRequest request) {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.setContentType("text/html");
		try {
			PrintWriter out = response.getWriter();
			if (exceptionHandlingConfig.shouldDisplayStackTrace(request, exception)) {
				out.println("<html><body><pre>");
				out.println(ExceptionTool.getStackTraceStringForHtmlPreBlock(exception));
				out.println("</pre></body></html>");
			} else {
				out.println(exceptionHandlingConfig.getHtmlErrorMessage(exception));
			}
		} catch (Exception ex) {
			logger.error("Exception while writing html output");
			ex.printStackTrace();
		}
	}

}
