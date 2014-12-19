package com.hotpads.util.http.client;

import java.net.ServerSocket;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.exception.HotPadsHttpConnectionAbortedException;
import com.hotpads.util.http.response.exception.HotPadsHttpException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestExecutionException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestTimeoutException;
import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;

public class HotPadsHttpClientTester {

	private static final int PORT = 9091;
	private static final String URL = "http://localhost:" + PORT + "/";
	private final HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();

	private static final class ServSocket extends Thread {
		private static ServerSocket server;

		public void run() {
			startServer();
		}

		public void startServer() {
			System.out.println("opening socket on port " + PORT);
			try {
				server = new ServerSocket(PORT);
				while(true) {
					server.accept();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public void stopServer() {
			System.out.println("closing socket");
			try {
				server.close();
				this.join();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static final ServSocket SOCKET = new ServSocket();

	@BeforeClass
	public static void setUp() {
		SOCKET.start();
	}

	@AfterClass
	public static void tearDown() throws InterruptedException {
		SOCKET.stopServer();
	}

	@Test(expected = HotPadsHttpRequestExecutionException.class)
	public void testCheckedException() throws HotPadsHttpException {
		System.out.println("\nTEST - checked exception - requesting invalid location");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "invalidLocation", false);
		try {
			client.executeChecked(request);
		} catch (HotPadsHttpException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test(expected = HotPadsHttpRuntimeException.class)
	public void testUncheckedException() throws HotPadsHttpRuntimeException {
		System.out.println("\nTEST - unchecked exception - requesting invalid location");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "invalidLocation", false);
		try {
			client.execute(request);
		} catch (HotPadsHttpRuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test(expected = HotPadsHttpRequestExecutionException.class)
	public void testEmptyHostCheckedException() throws HotPadsHttpException {
		System.out.println("\nTEST - unchecked exception from checked call - requesting empty host");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, "", false);
		try {
			client.executeChecked(request);
		} catch (HotPadsHttpException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class)
	public void testHttpConnectionAbortedException() throws HotPadsHttpException {
		System.out.println("\nTEST - connection aborted exception - invalid host URI");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "localhost:" + PORT, false);
		try {
			client.executeChecked(request);
		} catch (HotPadsHttpException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test(expected = HotPadsHttpRequestTimeoutException.class)
	public void testRequestTimeoutException() throws HotPadsHttpException {
		System.out.println("\nTEST - http request timeout");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).setTimeoutMs(0);
		try {
			client.executeChecked(request);
		} catch (HotPadsHttpException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testSuccessfulRequest() throws HotPadsHttpException {
		System.out.println("\nTEST - http request timeout");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		client.executeChecked(request);
	}
}
