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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.google.inject.BindingAnnotation;
import com.google.inject.Singleton;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.exception.analysis.HttpRequestRecord;
import com.hotpads.exception.analysis.HttpRequestRecordKey;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.notification.DatabaseInsertionPersister;
import com.hotpads.notification.NotificationApiClient;
import com.hotpads.notification.NotificationRequestDtoTool;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
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
	
	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	private static final String SERVER_EXCEPTION_NOTIFICATION_TYPE = "com.hotpads.notification.type.ServerExceptionNotificationType";
	private static final String ERROR = "/error";
	private static final boolean NOTIFICATION_ENABLED = true; //TODO only for dev

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
	private DatabaseInsertionPersister<ExceptionRecord, ExceptionRecordKey> persister;
	private DatabaseInsertionPersister<HttpRequestRecord, HttpRequestRecordKey> requestPersister;

	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (NOTIFICATION_ENABLED) {
			if (exceptionRecordNode == null) {
				ServletContext sc = filterConfig.getServletContext();
				exceptionRecordNode = (SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) sc.getAttribute(ATTRIBUTE_EXCEPTION_RECORD_NODE);
				httpRequestRecordNode = (MapStorageNode<HttpRequestRecordKey, HttpRequestRecord>) sc.getAttribute(ATTRIBUTE_REQUEST_RECORD_NODE);
				exceptionHandlingConfig = (ExceptionHandlingConfig) sc.getAttribute(ATTRIBUTE_EXCEPTION_HANDLING_CONFIG);
				notificationApiClient = new NotificationApiClient(new NotificationRequestDtoTool() ,exceptionHandlingConfig);
			}
			persister = new DatabaseInsertionPersister<ExceptionRecord, ExceptionRecordKey>(exceptionRecordNode);
			requestPersister = new DatabaseInsertionPersister<HttpRequestRecord, HttpRequestRecordKey>(httpRequestRecordNode);
			apiCaller = new ParallelApiCaller(notificationApiClient);
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

			if(NOTIFICATION_ENABLED){
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
		if(persister == null){ return; }
		try {
			ExceptionRecord exceptionRecord = new ExceptionRecord(exceptionHandlingConfig.getServerName(),
					ExceptionUtils.getStackTrace(e));
			persister.addToQueue(exceptionRecord);
			StringBuilder paramString = new StringBuilder("[");
			for (Entry<String, String[]> param : request.getParameterMap().entrySet()) {
				paramString.append(param.getKey());
				paramString.append(":");
				paramString.append(Arrays.toString(param.getValue()));
				paramString.append(",");
			}
			paramString.append("]");
			StringBuilder cookieString = new StringBuilder("[");
			for (Cookie c : request.getCookies()) {
				cookieString.append(c.getName());
				cookieString.append(":");
				cookieString.append(c.getValue());
				cookieString.append(",");
			}
			cookieString.append("]");
			String jspName = null;
			int lineNumber = 0;
			Throwable next;
			next = e;
			whileLoop: do {
				String key = "An exception occurred processing JSP page ";
				if (next.getMessage().contains(key)) {
					String key2 = " at line ";
					int i = next.getMessage().indexOf(key2);
					int endLine = next.getMessage().indexOf("\n");
					jspName = next.getMessage().substring(key.length(), i);
					lineNumber = Integer.parseInt(next.getMessage().substring(i + key2.length(), endLine));
					break;
				}				
				jspName = getJSPName(next.getMessage());
				if (jspName != null) {
					break;
				}
				for (StackTraceElement element : next.getStackTrace()) {
					jspName = getJSPName(element.getClassName());
					if (jspName != null) {
						break whileLoop;
					}
				}
				next = next.getCause();
			} while (next != null);
			HttpRequestRecord httpRequestRecord = new HttpRequestRecord(
					new HttpRequestRecordKey(exceptionRecord.getKey().getId()),
					jspName,
					"",
					lineNumber,
					request.getRequestURL().toString() + "?" + request.getQueryString(),
					request.getMethod(),
					paramString.toString(),
					RequestTool.getIpAddress(request),
					request.getHeader("user-agent"),
					"XMLHttpRequest".equals(request.getHeader("x-requested-with")),
					request.getHeader("referer"),
					cookieString.toString(),
					"unkown roles"
					);
			requestPersister.addToQueue(httpRequestRecord);
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
							SERVER_EXCEPTION_NOTIFICATION_TYPE,
							exceptionRecord.getKey().getId(),
							exception.getClass().getName()));
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
