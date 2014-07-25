package com.hotpads.handler.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.exception.analysis.HttpHeaders;
import com.hotpads.exception.analysis.HttpRequestRecord;
import com.hotpads.exception.analysis.HttpRequestRecordKey;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.collections.Pair;

@Singleton
public class ExceptionHandlingFilter implements Filter {
	private static Logger logger = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

	public static final String ATTRIBUTE_EXCEPTION_RECORD_NODE = "exceptionRecordNode";
	public static final String ATTRIBUTE_REQUEST_RECORD_NODE = "requestRecordNode";
	public static final String ATTRIBUTE_EXCEPTION_HANDLING_CONFIG = "exceptionHandlingConfig";
	public static final String ATTRIBUTE_PARALLEL_API_CALLER = "parallelApiCaller";
	
	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode;
	@Inject
	private IndexedSortedMapStorageNode<HttpRequestRecordKey, HttpRequestRecord> httpRequestRecordNode;
	@Inject
	private ParallelApiCaller apiCaller;
	
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
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		try {
			fc.doFilter(req, res);
		} catch (Exception e) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			logger.warn("ExceptionHandlingFilter caught an exception: ", e);
			writeExceptionToResponseWriter(response, e, request);
			if(exceptionHandlingConfig.shouldPersistExceptionRecords(request, e)) {
				recordExceptionAndRequestNotification(request, e);
			}
		}
	}

	private void recordExceptionAndRequestNotification(HttpServletRequest request, Exception e) {
		try {
			ExceptionRecord exceptionRecord = new ExceptionRecord(
					exceptionHandlingConfig.getServerName(),
					ExceptionUtils.getStackTrace(e),
					e.getClass().getName());
			exceptionRecordNode.put(exceptionRecord, null);
			StringBuilder paramStringBuilder = new StringBuilder();
			Joiner listJoiner = Joiner.on("; ");
			for (Entry<String, String[]> param : request.getParameterMap().entrySet()) {
				paramStringBuilder.append(param.getKey());
				paramStringBuilder.append(": ");
				paramStringBuilder.append(listJoiner.join(param.getValue()));
				paramStringBuilder.append(", ");
			}
			String paramString = paramStringBuilder.toString();
			String place = null;
			Integer lineNumber = null;
			Pair<String, Integer> pair = searchJspName(e);
			if (pair.getLeft() == null) {
				pair = searchClassName(e);
			}
			place = pair.getLeft();
			lineNumber = pair.getRight();
			HttpRequestRecord httpRequestRecord = new HttpRequestRecord(
					exceptionRecord.getKey().getId(),
					place,
					null,
					lineNumber == null ? -1 : lineNumber,
					request.getMethod(),
					paramString.length() > 0 ? paramString : null,
					request.getScheme(),
					request.getServerName(),
					request.getServerPort(),
					request.getContextPath(),
					request.getRequestURI().substring(request.getContextPath().length()),
					request.getQueryString(),
					RequestTool.getIpAddress(request),
					"unknown user roles",
					-1l,
					new HttpHeaders(request)
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
				} catch(NumberFormatException ex) {
					
				}
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
