package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.HotPadsHttpResponse;
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
		private static boolean done = false;

		public void run() {
			startServer();
		}

		public void startServer() {
			System.out.println("opening socket on port " + PORT);
			try {
				server = new ServerSocket(PORT);
				while (!done) {
					try {
						Socket s = server.accept();
						String response = "Hello World";
						PrintWriter out = new PrintWriter(s.getOutputStream());
						out.println("HTTP/1.1 200 OK");
						out.println("Content-Type: text/plain");
						out.println("Content-Length: " + response.length());
						out.println();
						out.println(response);
						out.flush();
						s.close();
					} catch (IOException e) {
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(149);
			}
		}

		public void stopServer() {
			done = true;
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
		try {
			System.out.println("\nTEST - successful request");
			HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
			HotPadsHttpResponse response = client.executeChecked(request);
			System.out.println(response.getEntity());
		} catch (HotPadsHttpException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
