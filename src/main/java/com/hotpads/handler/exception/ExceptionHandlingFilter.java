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

	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";

	public static final String CGUILLAUME_NOTIFICATION_RECIPENT_EMAIL = "cguillaume@hotpads.com";


	private IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> node;
	private String serverName;
	private NotificationApiCaller notificationApiCaller;
	
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
			initiate(request.getServletContext());
			logger.warn(ExceptionTool.getStackTraceAsString(e));

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
