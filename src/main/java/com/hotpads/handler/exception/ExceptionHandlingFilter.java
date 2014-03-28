package com.hotpads.handler.exception;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.google.inject.Singleton;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.exception.http.HttpException;
import com.hotpads.util.core.exception.http.imp.Http500InternalServerErrorException;

@Singleton
public class ExceptionHandlingFilter implements Filter {

	Logger logger = Logger.getLogger(ExceptionHandlingFilter.class);

	private static IndexedSortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> node;
	private static String serverName;

	public static final String PARAM_DISPLAY_EXCEPTION_INFO = "displayExceptionInfo";
	public static final String NOTIFICATION_API_ENDPOINT = "http://localhost:8080/job/api/notification";
	
	public static final String NOTIFICATION_TYPE = "com.hotpads.notification.type.ServerExceptionNotification";
	public static final String NOTIFICATION_RECIPENT_TYPE = "EMAIL";
	public static final String NOTIFICATION_RECIPENT_EMAIL = "cguillaume@hotpads.com";
	
	public static final String 
			NOTIFICATION_API_PARAM_NAME = "requests",
			NOTIFICATION_API_USER_TYPE = "usertype",
			NOTIFICATION_API_USER_ID = "userid",
			NOTIFICATION_API_TIME = "time",
			NOTIFICATION_API_TYPE = "type",
			NOTIFICATION_API_DATA = "data";

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

				ExceptionRecord exceptionRecord = new ExceptionRecord(serverName,
						ExceptionUtils.getStackTrace(httpException));
				node.put(exceptionRecord, null);

				// call the notification api
				String data = exceptionRecord.getKey().getId();
				String time = Long.toString(System.currentTimeMillis());
				callNotificationApi(time, data);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// if (CustomExceptionResolver.isInternal()) {

			// } else {

			// }
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

	private void callNotificationApi(String time, String data) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(NOTIFICATION_API_ENDPOINT);
		List<NameValuePair> params = ListTool.create();

		JSONArray requests = new JSONArray();
		JSONObject notification = new JSONObject();
		notification.put(NOTIFICATION_API_USER_TYPE, NOTIFICATION_RECIPENT_TYPE);
		notification.put(NOTIFICATION_API_USER_ID, NOTIFICATION_RECIPENT_EMAIL);
		notification.put(NOTIFICATION_API_TIME, time);
		notification.put(NOTIFICATION_API_TYPE, NOTIFICATION_TYPE);
		notification.put(NOTIFICATION_API_DATA, data);
		requests.add(notification);

		params.add(new BasicNameValuePair(NOTIFICATION_API_PARAM_NAME, requests.toString()));
		post.setEntity(new UrlEncodedFormEntity(params));
		client.execute(post);
	}

	@Override
	public void destroy() {

	}
}