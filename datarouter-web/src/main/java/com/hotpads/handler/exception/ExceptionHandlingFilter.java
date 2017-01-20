package com.hotpads.handler.exception;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.InjectorRetriever;
import com.hotpads.datarouter.monitoring.exception.HttpRequestRecord;
import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.handler.user.session.CurrentUserInfo;
import com.hotpads.util.DrExceptionTool;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.http.HttpHeaders;
import com.hotpads.util.http.RequestTool;

public abstract class ExceptionHandlingFilter implements Filter, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

	private static final Set<Integer> statusToLog = new HashSet<>(Arrays.asList(HttpServletResponse.SC_NOT_FOUND));

	public static final String REQUEST_RECEIVED_AT = "receivedAt";

	private ExceptionHandlingConfig exceptionHandlingConfig;
	private ExceptionNodes exceptionNodes;
	private WebAppName webAppName;
	private ExceptionRecorder exceptionRecorder;
	private CurrentUserInfo currentUserInfo;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		exceptionHandlingConfig = injector.getInstance(ExceptionHandlingConfig.class);
		exceptionNodes = injector.getInstance(ExceptionNodes.class);
		webAppName = injector.getInstance(WebAppName.class);
		exceptionRecorder = injector.getInstance(ExceptionRecorder.class);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException{
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;

		Date receivedAt = new Date();
		req.setAttribute(REQUEST_RECEIVED_AT, receivedAt);
		try{
			fc.doFilter(req, res);
		}catch(OutOfMemoryError error){
			logger.error("The current number of threads at OOM are: "
					+ ManagementFactory.getThreadMXBean().getThreadCount());
			dumpAllStackTraces();
			throw error;
		}catch(Exception e){
			logger.error("ExceptionHandlingFilter caught an exception:", e);
			Optional<ExceptionRecordKey> recordKey = tryRecordExceptionAndRequestNotification(request, e, receivedAt);
			if(recordKey.isPresent()){
				response.setHeader(HttpHeaders.X_EXCEPTION_ID, recordKey.get().getId());
			}
			writeExceptionToResponseWriter(response, e, request);
		}
		Counters.inc(webAppName + " response " + response.getStatus());
		if(statusToLog.contains(response.getStatus())){
			logger.warn("{} on {} [{}] {}", response.getStatus(), RequestTool.getRequestURIWithQueryString(request),
					RequestTool.getIpAddress(request), RequestTool.getUserAgent(request));
		}
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

	private Optional<ExceptionRecordKey> tryRecordExceptionAndRequestNotification(HttpServletRequest request,
			Exception exception, Date receivedAt){
		try{
			String place = null;
			Integer lineNumber = null;
			Pair<String, Integer> pair = searchJspName(exception);
			if(pair.getLeft() == null){
				pair = searchClassName(exception);
			}
			place = pair.getLeft();
			lineNumber = pair.getRight();

			Optional<String> userToken = currentUserInfo.getUserToken(request);

			ExceptionRecord exceptionRecord = exceptionRecorder.recordException(exception,
					WebExceptionCategory.HTTP_REQUEST, place);

			HttpRequestRecord httpRequestRecord = new HttpRequestRecord(
					receivedAt,
					exceptionRecord.getKey().getId(),
					null,
					lineNumber == null ? -1 : lineNumber,
					request,
					"unknown user roles",
					userToken.orElse(null));
			exceptionNodes.getHttpRequestRecordNode().put(httpRequestRecord, null);
			return Optional.of(exceptionRecord.getKey());
		}catch(Exception e){
			logger.error("Exception while logging", e);
			return Optional.empty();
		}
	}

	private Pair<String,Integer> searchClassName(Exception exception){
		String place;
		Integer lineNumber = null;
		Throwable cause = exception;
		do{
			for(StackTraceElement element : cause.getStackTrace()){
				if(element.getClassName().contains("com.hotpads")){
					lineNumber = element.getLineNumber();
					place = element.getClassName();
					return new Pair<>(place, lineNumber);
				}
			}
			cause = cause.getCause();
		}while(cause != null);
		return new Pair<>(null, null);
	}

	private Pair<String,Integer> searchJspName(Exception exception){
		String place;
		Integer lineNumber = null;
		Throwable cause = exception;
		do{
			String key = "An exception occurred processing JSP page ";
			if(cause.getMessage() == null){
				cause = cause.getCause();
				continue;
			}
			int indexOfBegin = cause.getMessage().indexOf(key);
			if(indexOfBegin > -1){
				String key2 = " at line ";
				int index = cause.getMessage().indexOf(key2);
				int endLine = cause.getMessage().indexOf("\n");
				place = cause.getMessage().substring(indexOfBegin + key.length(), index);
				try{
					lineNumber = Integer.parseInt(cause.getMessage().substring(index + key2.length(), endLine));
				}catch(NumberFormatException ex){// It's OK if it's not a number
				}
				return new Pair<>(place, lineNumber);
			}
			/* An error occurred at line: 3 in the jsp file: /WEB-INF/jsp/jspError.jsp */
			String keyInTheJspFile = " in the jsp file: ";
			indexOfBegin = cause.getMessage().indexOf(keyInTheJspFile);
			if(indexOfBegin > -1){
				String key2 = " at line: ";
				int index = cause.getMessage().indexOf(key2);
				int endLine = cause.getMessage().indexOf('\n', indexOfBegin);
				place = cause.getMessage().substring(indexOfBegin + keyInTheJspFile.length(), endLine);
				try{
					lineNumber = Integer.parseInt(cause.getMessage().substring(index + key2.length(), indexOfBegin));
				}catch(NumberFormatException ex){// It's OK if it's not a number
				}
				return new Pair<>(place, lineNumber);
			}
			place = getJspName(cause.getMessage());
			if(place != null){
				return new Pair<>(place, lineNumber);
			}
			for(StackTraceElement element : cause.getStackTrace()){
				place = getJspName(element.getClassName());
				if(place != null){
					return new Pair<>(place, lineNumber);
				}
			}
			cause = cause.getCause();
		}while(cause != null);
		return new Pair<>(null, null);
	}

	private String getJspName(String string){
		if(string == null){
			return null;
		}
		String key = "WEB_002dINF";
		int index = string.indexOf(key);
		if(index > -1){
			String jspName = string.substring(index);
			jspName = jspName.replaceAll("\\.", "/");
			jspName = jspName.replaceAll("_002d", "-");
			jspName = jspName.replaceAll("_", ".");
			return "/" + jspName;
		}
		return null;
	}

	private void writeExceptionToResponseWriter(HttpServletResponse response, Exception exception,
			HttpServletRequest request){
		if(response.getStatus() == HttpServletResponse.SC_OK){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		response.setContentType("text/html");
		try{
			PrintWriter out = response.getWriter();
			if(exceptionHandlingConfig.shouldDisplayStackTrace(request, exception)){
				out.println("<html><body><pre>");
				out.println(DrExceptionTool.getStackTraceStringForHtmlPreBlock(exception));
				out.println("</pre></body></html>");
			}else{
				out.println(exceptionHandlingConfig.getHtmlErrorMessage(exception));
			}
		}catch(Exception ex){
			logger.error("Exception while writing html output", ex);
		}
	}

	@Override
	public void destroy() {}

}
