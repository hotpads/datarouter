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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ObjectTool;

@Singleton
public class ExceptionHandlingFilter implements Filter {

	private static Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	private static final String SERVER_EXCEPTION_NOTIFICATION_TYPE = "com.hotpads.notification.type.ServerExceptionNotificationType";
	private static final String CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL = "cguillaume@hotpads.com";
	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	private IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode;
	private String serverName;
	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private NotificationApiClient notificationApiClient;
	private ParallelApiCalling pac;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (notificationApiClient == null) {//no spring here
			notificationApiClient = new NotificationApiClient();
		}
		pac = new ParallelApiCalling(notificationApiClient);
		initiateIfNeed(filterConfig.getServletContext());
	}

	@SuppressWarnings("unchecked")
	private void initiateIfNeed(ServletContext sc) {
		if (serverName == null) {
			serverName = (String) sc.getAttribute("serverName");
		}
		if (exceptionRecordNode == null) {
			exceptionRecordNode = (IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) sc.getAttribute("recordNode");//FIXME no null only sur site app and cannot inject EventRouter here
		}
		if (exceptionHandlingConfig == null) {
			exceptionHandlingConfig = (ExceptionHandlingConfig) sc.getAttribute("exceptionHandlingConfig");
		}
		if (ObjectTool.anyNull(serverName, exceptionRecordNode, exceptionHandlingConfig)) {
			logger.warn("Missing attribute in ServletContext for ExceptionHandlingFilter initialization");
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {	
		try {
			fc.doFilter(req, res);
		} catch (Exception e) {
			pac.warmupApiClient();//to be sure than the first request take less than 1s
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			logger.warn(ExceptionTool.getStackTraceAsString(e));
			try {
				PrintWriter out = response.getWriter();
				if (exceptionHandlingConfig.shouldDisplayStackTrace(request, e)) {
					out.println("<pre>");
					out.println(ExceptionTool.getStackTraceStringForHtmlPreBlock(e));
					out.println("</pre>");
				} else {
					out.println(exceptionHandlingConfig.getHtmlErrorMessage(e));
				}
			} catch (Exception ex) {
				logger.error("Exception while writing html output");
				ex.printStackTrace();
			}

			if (exceptionHandlingConfig.shouldLogException(request, e)) {
				initiateIfNeed(request.getServletContext());
				try {
					ExceptionRecord exceptionRecord = new ExceptionRecord(
							serverName,
							ExceptionUtils.getStackTrace(e));
					exceptionRecordNode.put(exceptionRecord, null);
					if (exceptionHandlingConfig.shouldRepportError(request, e)) {
						pac.add(new NotificationRequest(
								new NotificationUserId(
										NotificationUserType.EMAIL,
										CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL), //TODO only for dev
								SERVER_EXCEPTION_NOTIFICATION_TYPE,
								exceptionRecord.getKey().getId(),
								e.getClass().getName()));
					}

				} catch (Exception ex) {
					logger.error("Exception while loging");
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public void destroy() {

	}

}
