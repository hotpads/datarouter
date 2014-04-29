package com.hotpads.handler.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
import com.hotpads.notification.NotificationApiClient;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.ExceptionRecordPersister;
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

	public static final String PARAM_RECORD_NODE = "recordNode";
	public static final String EXCEPTION_HANDLING_CONFIG = "exceptionHandlingConfig";
	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	private static final String SERVER_EXCEPTION_NOTIFICATION_TYPE = "com.hotpads.notification.type.ServerExceptionNotificationType";
	private static final String ERROR = "/error";
	private static final boolean NOTIFICATION_ENABLED = true; //TODO only for dev

	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private NotificationApiClient notificationApiClient;
	@SuppressWarnings("rawtypes")
	@Inject
	@ExceptionRecordNode
	private SortedMapStorageNode exceptionRecordNode;

	private ParallelApiCaller apiCaller;
	private ExceptionRecordPersister persister;

	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (NOTIFICATION_ENABLED) {
			ServletContext sc = filterConfig.getServletContext();
			if (exceptionRecordNode != null) {
				persister = new ExceptionRecordPersister(exceptionRecordNode);
				apiCaller = new ParallelApiCaller(notificationApiClient);
			} else {
				exceptionRecordNode = (SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) sc.getAttribute(PARAM_RECORD_NODE);//FIXME no null only on site and cannot inject EventRouter here
				persister = new ExceptionRecordPersister(exceptionRecordNode);
				exceptionHandlingConfig = (ExceptionHandlingConfig) sc.getAttribute(EXCEPTION_HANDLING_CONFIG);
				notificationApiClient = new NotificationApiClient(exceptionHandlingConfig);
				apiCaller = new ParallelApiCaller(notificationApiClient);
			}
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

			addNotificationRequestToQueue(request, e, exceptionRecord);
		} catch (Exception ex) {
			logger.error("Exception while logging");
			ex.printStackTrace();
		}
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
