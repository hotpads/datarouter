package com.hotpads.handler.exception;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
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
	public static final String NOTIFICATION_API_ENDPOINT = "https://localhost:8443/job/api/notification";
	public static final String NOTIFICATION_API_REQUESTS = "requests";
	public static final String NOTIFICATION_API_USERTOKEN = "usertoken";
	public static final String NOTIFICATION_API_TIME = "time";
	public static final String NOTIFICATION_API_TYPE = "type";
	public static final String NOTIFICATION_API_DATA = "data";

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
				String userToken = null;// = ????
				String type = "com.hotpads.notification.type.ServerExceptionNotification";
				String data = exceptionRecord.getKey().getId();
				String time = Long.toString(System.currentTimeMillis());
				callNotificationApi(userToken, time, type, data);

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
				logger.info("Notification request sent");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} finally {

		}
	}

	private void callNotificationApi(String userToken, String time, String type, String data)
			throws ClientProtocolException, IOException, NoSuchAlgorithmException, KeyManagementException {

		SSLContext sslContext = SSLContext.getInstance("SSL");
		
		sslContext.init(null, new TrustManager[] {new X509TrustManager() {
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				
			}
		}}, new SecureRandom());
		
		SSLSocketFactory sf = new SSLSocketFactory(sslContext);
		Scheme httpsScheme = new Scheme("https", 8443, sf);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(httpsScheme);

		// apache HttpClient version >4.2 should use BasicClientConnectionManager
		ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(NOTIFICATION_API_ENDPOINT);
		List<NameValuePair> params = ListTool.create();

		JSONArray requests = new JSONArray();
		JSONObject notification = new JSONObject();
		notification.put(NOTIFICATION_API_USERTOKEN, userToken);
		notification.put(NOTIFICATION_API_TIME, time);
		notification.put(NOTIFICATION_API_TYPE, type);
		notification.put(NOTIFICATION_API_DATA, data);

		params.add(new BasicNameValuePair(NOTIFICATION_API_REQUESTS, requests.toString()));
		post.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse response = client.execute(post);
		System.out.println(Arrays.toString(response.getAllHeaders()));
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
	}

	@Override
	public void destroy() {
	}
}