package com.hotpads.handler.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.google.inject.BindingAnnotation;
import com.google.inject.Singleton;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.exception.analysis.HeadersWrapper;
import com.hotpads.exception.analysis.HttpRequestRecord;
import com.hotpads.exception.analysis.HttpRequestRecordKey;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.notification.NotificationApiClient;
import com.hotpads.notification.NotificationRequestDtoTool;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.setting.NotificationSettings;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.exception.http.HttpException;
import com.hotpads.util.core.exception.http.imp.Http500InternalServerErrorException;

@Singleton
public class ExceptionHandlingFilter implements Filter {
	private static Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	@BindingAnnotation
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ExceptionRecordNode {}

	@BindingAnnotation
	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface HttpRecordRecordNode {}

	public static final String ATTRIBUTE_EXCEPTION_RECORD_NODE = "exceptionRecordNode";
	public static final String ATTRIBUTE_REQUEST_RECORD_NODE = "requestRecordNode";
	public static final String ATTRIBUTE_EXCEPTION_HANDLING_CONFIG = "exceptionHandlingConfig";
	public static final String ATTRIBUTE_NOTIFICATION_SETTINGS = "notificationSettings";
	
	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	private static final String ERROR = "/error";

	@Inject
	private NotificationSettings notificationSettings;
	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private NotificationApiClient notificationApiClient;
	@Inject
	@ExceptionRecordNode
	@SuppressWarnings("rawtypes")
	private SortedMapStorageNode exceptionRecordNode;
	@Inject
	@HttpRecordRecordNode
	@SuppressWarnings("rawtypes")
	private MapStorageNode httpRequestRecordNode;
	
	private ParallelApiCaller apiCaller;

	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (exceptionRecordNode == null) {
			ServletContext sc = filterConfig.getServletContext();
			exceptionRecordNode = (SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) sc.getAttribute(ATTRIBUTE_EXCEPTION_RECORD_NODE);
			httpRequestRecordNode = (MapStorageNode<HttpRequestRecordKey, HttpRequestRecord>) sc.getAttribute(ATTRIBUTE_REQUEST_RECORD_NODE);
			notificationSettings = (NotificationSettings) sc.getAttribute(ATTRIBUTE_NOTIFICATION_SETTINGS);
			exceptionHandlingConfig = (ExceptionHandlingConfig) sc.getAttribute(ATTRIBUTE_EXCEPTION_HANDLING_CONFIG);
			notificationApiClient = new NotificationApiClient(new NotificationRequestDtoTool() ,exceptionHandlingConfig, notificationSettings);
		}
		apiCaller = new ParallelApiCaller(notificationApiClient, notificationSettings);
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

			if(notificationSettings.getExceptionHandling().getValue()){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				writeExceptionToResponseWriter(response, e, request);
				if(exceptionHandlingConfig.shouldPersistExceptionRecords(request, e)) {
					recordExceptionAndRequestNotification(request, e);
				}
			} else {//old redirect code we should delete
				HttpException httpException;
				if(e instanceof HttpException){
					httpException = (HttpException)e;
				}else{
					httpException = new Http500InternalServerErrorException(null, e);
				}
				logger.warn(ExceptionTool.getStackTraceAsString(httpException));
				HttpSession session = request.getSession();
				session.setAttribute("statusCode", httpException.getStatusCode());

				// something else needs to set this, like an AuthenticationFilter
				//				Object displayExceptionInfo = request.getAttribute(PARAM_DISPLAY_EXCEPTION_INFO);
				//				if(displayExceptionInfo != null && ((Boolean)displayExceptionInfo)){
				String message = httpException.getClass().getSimpleName() + ": " + e.getMessage();
				session.setAttribute("message", message);

				session.setAttribute("stackTrace", httpException.getStackTrace());
				session.setAttribute("stackTraceString", ExceptionTool
						.getStackTraceStringForHtmlPreBlock(httpException));
				//				}
				// RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/generic/exception.jsp");
				// dispatcher.forward(request, response);
				response.sendRedirect(request.getContextPath() + ERROR);
			}
		}
	}

	private void recordExceptionAndRequestNotification(HttpServletRequest request, Exception e) {
		try {
			StringBuilder paramStringBuilder = new StringBuilder();
			for (Entry<String, String[]> param : request.getParameterMap().entrySet()) {
				paramStringBuilder.append(param.getKey());
				paramStringBuilder.append(":");
				paramStringBuilder.append(Arrays.toString(param.getValue()));
				paramStringBuilder.append(",");
			}
			String paramString = paramStringBuilder.toString();
			//search for jsp error
			String place = null;
			int lineNumber = -1;
			Throwable cause;
			cause = e;
			whileLoop: do {
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
					break;
				}				
				place = getJSPName(cause.getMessage());
				if (place != null) {
					break;
				}
				for (StackTraceElement element : cause.getStackTrace()) {
					place = getJSPName(element.getClassName());
					if (place != null) {
						break whileLoop;
					}
				}
				cause = cause.getCause();
			} while (cause != null);
			if (place == null) {
				//search for other error in com.hotpads
				cause = e;
				whileLoop: do {
					for (StackTraceElement element : cause.getStackTrace()) {
						if (element.getClassName().contains("com.hotpads")) {
							lineNumber = element.getLineNumber();
							place = element.getClassName();
							break whileLoop;
						}
					}
					cause = cause.getCause();
				} while (cause != null);
			}
			ExceptionRecord exceptionRecord = new ExceptionRecord(
					exceptionHandlingConfig.getServerName(),
					ExceptionUtils.getStackTrace(e),
					place);
			exceptionRecordNode.put(exceptionRecord, null);
			HttpRequestRecord httpRequestRecord = new HttpRequestRecord(
					exceptionRecord.getKey().getId(),
					place,
					null,
					lineNumber,
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
					new HeadersWrapper(request)
					);
			httpRequestRecordNode.put(httpRequestRecord, null);
			addNotificationRequestToQueue(request, e, exceptionRecord);
		} catch (Exception ex) {
			logger.error("Exception while logging");
			ex.printStackTrace();
		}
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
			return jspName;
		}
		return null;
	}

	private void addNotificationRequestToQueue(HttpServletRequest request, Exception exception,
			ExceptionRecord exceptionRecord){
		if (exceptionHandlingConfig.shouldReportError(request, exception)) {
			apiCaller.add(new NotificationRequest(
					new NotificationUserId(
							NotificationUserType.EMAIL,
							exceptionHandlingConfig.getRecipientEmail()),
					exceptionHandlingConfig.getNotificationType(),
					exceptionRecord.getKey().getId(),
					exception.getClass().getName()),
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
