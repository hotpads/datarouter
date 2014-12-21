package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import junit.framework.Assert;

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
	private static final HotPadsHttpClient CLIENT = new HotPadsHttpClientBuilder().build();
	private static final int[] STATI = new int[] { 200, 403, 404, 408, 500, 503 };

	private static final class ServSocket extends Thread {
		private static ServerSocket server;
		private static boolean done = false;

		private volatile int status = 200;
		private volatile String response = "hello world";

		public void run() {
			startServer();
		}

		public void startServer() {
			System.out.println("opening socket on port " + PORT);
			try {
				server = new ServerSocket(PORT);
			} catch (IOException e) {
				System.out.println("could not open socket: " + e.getMessage());
				e.printStackTrace();
				System.exit(120);
			}
			while (!done) {
				try {
					accept();
					sleep(100);
				} catch (IOException | InterruptedException e) {
					System.out.println("PROBLEM: " + e.getMessage());
				}
			}
		}

		public void stopServer() {
			done = true;
			System.out.println("\nclosing socket");
			try {
				server.close();
				join();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public void setResponse(int status, String response) {
			this.status = status;
			this.response = response;
		}

		private void accept() throws IOException {
			Socket s = server.accept();
			PrintWriter out = new PrintWriter(s.getOutputStream());
			out.println("HTTP/1.1 " + status);
			out.println("Content-Type: text/plain");
			out.println("Content-Length: " + response.length());
			out.println();
			out.println(response);
			out.flush();
			out.close();
			s.close();
		}
	}

	private static final ServSocket SERVER = new ServSocket();

	@BeforeClass
	public static void setUp() {
		SERVER.start();
	}

	@AfterClass
	public static void tearDown() throws InterruptedException {
		SERVER.stopServer();
	}

	@Test(expected = HotPadsHttpRuntimeException.class)
	public void testUncheckedException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "invalidLocation", false);
		executeRequest("unchecked exception - requesting invalid location", request, false);
	}

	@Test(expected = HotPadsHttpRequestExecutionException.class)
	public void testExecutionException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, "invalidLocation", false);
		executeRequest("unchecked exception from checked call - request invalid location", request, true);
	}

	@Test(expected = HotPadsHttpRequestTimeoutException.class)
	public void testRequestTimeoutException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).setTimeoutMs(0);
		executeRequest("http request timeout", request, true);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class)
	public void testHttpConnectionAbortedException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "localhost:" + PORT, false);
		executeRequest("connection aborted exception - invalid host URI", request, true);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class)
	public void testInvalidRequestHeader() throws HotPadsHttpException {
		SERVER.setResponse(301, "the 301 status code will throw an exception when not provided a location header");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		executeRequest("connection aborted exception - invalid headers", request, true);
	}

	@Test
	public void testSuccessfulRequest() throws HotPadsHttpException {
		int status = STATI[new Random().nextInt(STATI.length)];
		String response = UUID.randomUUID().toString();
		SERVER.setResponse(status, response);

		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		HotPadsHttpResponse httpResponse = executeRequest("TEST - successful request", request, true);
		System.out.println("status " + httpResponse.getStatusCode());
		System.out.println("entity " + httpResponse.getEntity());
		Assert.assertEquals(response, httpResponse.getEntity());
		Assert.assertEquals(status, httpResponse.getStatusCode());
	}

	private HotPadsHttpResponse executeRequest(String message, HotPadsHttpRequest request, boolean checked)
			throws HotPadsHttpException {
		System.out.println("\n" + message);
		try {
			return checked ? CLIENT.executeChecked(request) : CLIENT.execute(request);
		} catch (HotPadsHttpException | HotPadsHttpRuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
