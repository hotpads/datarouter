package com.hotpads.handler.exception;

import static com.hotpads.handler.exception.NotificationApiConstants.EMAIL_NOTIFICATION_RECIPENT_TYPE;
import static com.hotpads.handler.exception.NotificationApiConstants.SERVER_EXCEPTION_NOTIFICATION_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
import com.hotpads.util.core.exception.http.HttpException;
import com.hotpads.util.core.exception.http.imp.Http500InternalServerErrorException;

@Singleton
public class ExceptionHandlingFilter implements Filter {

	Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	private static IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> node;
	private static String serverName;

//	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	public static final String CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL = "cguillaume@hotpads.com";

	// private static final String ERROR = "/error";

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		try {
			fc.doFilter(req, res);
		} catch (Exception e) {
			HttpException httpException;
			if (e instanceof HttpException) {
				httpException = (HttpException) e;
			} else {
				httpException = new Http500InternalServerErrorException(null, e);
			}
			logger.warn(ExceptionTool.getStackTraceAsString(httpException));
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
				// create the databean and logit
				if (ObjectTool.anyNull(serverName)) {
					serverName = (String) request.getServletContext().getAttribute("serverName");
				}
				if (ObjectTool.anyNull(node)) {
					node = (IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord>) request
							.getServletContext().getAttribute("recordNode");
				}

				ExceptionRecord exceptionRecord = new ExceptionRecord(
						serverName,
						ExceptionUtils.getStackTrace(httpException));
				node.put(exceptionRecord, null);

				new NotificationApiCaller().call(
						EMAIL_NOTIFICATION_RECIPENT_TYPE,
						CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL,
						System.currentTimeMillis(),
						SERVER_EXCEPTION_NOTIFICATION_TYPE,
						exceptionRecord.getKey().getId());

			} catch (Exception ex) {
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
				ex.printStackTrace();
			}
		} finally {

		}
	}

	@Override
	public void destroy() {

	}
}