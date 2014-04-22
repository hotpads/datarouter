package com.hotpads.handler.exception;

import java.io.IOException;
import java.io.PrintWriter;

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

import com.google.inject.Singleton;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.notification.NotificationApiClient;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.RecordingAttempter;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.exception.http.HttpException;
import com.hotpads.util.core.exception.http.imp.Http500InternalServerErrorException;

@Singleton
public class ExceptionHandlingFilter implements Filter {

	private static Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	private static final String SERVER_EXCEPTION_NOTIFICATION_TYPE = "com.hotpads.notification.type.ServerExceptionNotificationType";
	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";
	private static final String ERROR = "/error";
	private static final boolean NOTIFICATION_ENABLED = true; //TODO only for dev

	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private NotificationApiClient notificationApiClient;
	private ParallelApiCaller pac;
	private RecordingAttempter ra;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (NOTIFICATION_ENABLED) {
			initiateIfNeed(filterConfig.getServletContext());
		}
	}

	@SuppressWarnings("unchecked")
	private void initiateIfNeed(ServletContext sc) {
		if (NOTIFICATION_ENABLED) {
			if (ra == null) {
				SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode = (SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) sc.getAttribute("recordNode");//FIXME no null only on site and cannot inject EventRouter here
				if (exceptionRecordNode != null)
					ra = new RecordingAttempter(exceptionRecordNode);
			}
			if (exceptionHandlingConfig == null) {
				exceptionHandlingConfig = (ExceptionHandlingConfig) sc.getAttribute("exceptionHandlingConfig");	
				if (exceptionHandlingConfig != null) {
					if (notificationApiClient == null) {
						notificationApiClient = new NotificationApiClient(exceptionHandlingConfig);
					}
					pac = new ParallelApiCaller(notificationApiClient);
				}
			}
			if (ObjectTool.anyNull(exceptionHandlingConfig, ra)) {
				logger.warn("Missing attribute in ServletContext for ExceptionHandlingFilter initialization");
			}
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		try {
			fc.doFilter(req, res);
		} catch (Exception e) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;

			if (NOTIFICATION_ENABLED) {
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				writeExceptionToResponseWriter(response, e, request);
				initiateIfNeed(request.getServletContext());
				if (exceptionHandlingConfig.shouldLogException(request, e)) {
					trySendingExceptionToNotificationService(request, e);
				}
			} else {
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

	private void trySendingExceptionToNotificationService(HttpServletRequest request, Exception e) {
		try {
			ExceptionRecord exceptionRecord = new ExceptionRecord(
					exceptionHandlingConfig.getServerName(),
					ExceptionUtils.getStackTrace(e));
			ra.rec(exceptionRecord);

			addNotificationRequestToQueue(request, e, exceptionRecord);

		} catch (Exception ex) {
			logger.error("Exception while loging");
			ex.printStackTrace();
		}
	}

	private void addNotificationRequestToQueue(HttpServletRequest request, Exception exception, ExceptionRecord exceptionRecord) {
		if (exceptionHandlingConfig.shouldRepportError(request, exception)) {
			pac.add(new NotificationRequest(
					new NotificationUserId(
							NotificationUserType.EMAIL,
							exceptionHandlingConfig.getRecipientEmail()),
					SERVER_EXCEPTION_NOTIFICATION_TYPE,
					exceptionRecord.getKey().getId(),
					exception.getClass().getName()));
		}
	}

	private void writeExceptionToResponseWriter(HttpServletResponse response, Exception exception, HttpServletRequest request) {
		try {
			PrintWriter out = response.getWriter();
			if (exceptionHandlingConfig.shouldDisplayStackTrace(request, exception)) {
				out.println("<pre>");
				out.println(ExceptionTool.getStackTraceStringForHtmlPreBlock(exception));
				out.println("</pre>");
			} else {
				out.println(exceptionHandlingConfig.getHtmlErrorMessage(exception));
			}
		} catch (Exception ex) {
			logger.error("Exception while writing html output");
			ex.printStackTrace();
		}
	}

	@Override
	public void destroy() {

	}

}
