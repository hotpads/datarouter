package com.hotpads.handler.exception;

import static com.hotpads.handler.exception.NotificationApiConstants.NOTIFICATION_RECIPENT_TYPE_EMAIL;
import static com.hotpads.handler.exception.NotificationApiConstants.SERVER_EXCEPTION_NOTIFICATION_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

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
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ObjectTool;

@Singleton
public class ExceptionHandlingFilter implements Filter {

	private static Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	private IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> node;
	private String serverName;
	private NotificationApiCaller notificationApiCaller;

	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	public static final String CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL = "cguillaume@hotpads.com";

	// private static final String ERROR = "/error";

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

	private void initiate(ServletContext sc) {
		if (serverName == null) {
			serverName = (String) sc.getAttribute("serverName");
			node = (IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) sc.getAttribute("recordNode");
			NotificationApiConfig notificationApiConfig = (NotificationApiConfig) sc.getAttribute("notificationApiConfig");

			if (ObjectTool.anyNull(serverName, node)) {
				logger.warn("Missing attribute in ServletContext for ExceptionHandlingFilter initialization");
			} 
			notificationApiCaller = new NotificationApiCaller(notificationApiConfig);
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		try {
			fc.doFilter(req, res);
		} catch (Exception e) {
//			System.out.println("Filter catch it");
//			System.out.println("contextPath" + request.getServletContext().getContextPath());
			ServletContext sc = request.getServletContext();
			initiate(sc);
//			Enumeration<String> an = sc.getAttributeNames();
			
//			while (an.hasMoreElements()) {
//				String string = an.nextElement();
//				System.out.println(string);
//			}
			
//			System.out.println("serverName");
//			System.out.println(serverName);
//			System.out.println(serverName);
			
//			System.out.println("node");
//			System.out.println(node);
			
//			System.out.println(node);
			
//			HttpException httpException;
//			if (e instanceof HttpException) {
//				httpException = (HttpException) e;
//			} else {
//				httpException = new Http500InternalServerErrorException(null, e);
//			}
			logger.warn(ExceptionTool.getStackTraceAsString(e));
			// HttpSession session = request.getSession();
			// session.setAttribute("statusCode",
			// httpException.getStatusCode());

			// something else needs to set this, like an AuthenticationFilter
			// Object displayExceptionInfo =
			// request.getAttribute(PARAM_DISPLAY_EXCEPTION_INFO);
			// if(displayExceptionInfo != null &&
			// ((Boolean)displayExceptionInfo)){
			// String message = httpException.getClass().getSimpleName() + ": "
			// + e.getMessage();
			// session.setAttribute("message", message);
			//
			// session.setAttribute("stackTrace",
			// httpException.getStackTrace());
			// session.setAttribute("stackTraceString", ExceptionTool
			// .getStackTraceStringForHtmlPreBlock(httpException));
			// }
			// RequestDispatcher dispatcher =
			// request.getRequestDispatcher("/jsp/generic/exception.jsp");
			// dispatcher.forward(request, response);
			// response.sendRedirect(request.getContextPath() + ERROR);

			try {
				// Log the exception in database
				ExceptionRecord exceptionRecord = new ExceptionRecord(
						serverName,
						ExceptionUtils.getStackTrace(e));
				node.put(exceptionRecord, null);

				notificationApiCaller.call(
						NOTIFICATION_RECIPENT_TYPE_EMAIL,
						CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL,
						System.currentTimeMillis(),
						SERVER_EXCEPTION_NOTIFICATION_TYPE,
						exceptionRecord.getKey().getId());

			} catch (Exception ex) {
				logger.error("Exception while loging and requesting notification API");
				ex.printStackTrace();
			}

//			if (CustomExceptionResolver.isInternal()) {
//
//			} else {
//
//			}
			try {
				PrintWriter out = response.getWriter();
				File f = new File("workspace/error.html");
				if (f.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(f));
					String line;
					while ((line = br.readLine()) != null) {
						out.println(line);
					}
					br.close();
				}
			} catch (Exception ex) {
				logger.error("Exception while writing html output");
				ex.printStackTrace();
			}
		} finally {

		}
	}

	@Override
	public void destroy() {

	}
}
