package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
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
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

public class HotPadsHttpClientTester {
	private static final int PORT = 9091;
	private static final String URL = "http://localhost:" + PORT + "/";
	private static final HotPadsHttpClient CLIENT = new HotPadsHttpClientBuilder().build();
	private static final int[] STATI = new int[] { 200, 403, 404, 408, 500, 503 };
	private static final Random RANDOM = new Random(115509410414623L);

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
		executeRequest(CLIENT, "unchecked exception - requesting invalid location", request, false);
	}

	@Test(expected = HotPadsHttpRequestExecutionException.class)
	public void testExecutionException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.POST, "invalidLocation", false);
		executeRequest(CLIENT, "unchecked exception from checked call - request invalid location", request, true);
	}

	@Test(expected = HotPadsHttpRequestTimeoutException.class)
	public void testRequestTimeoutException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).setTimeoutMs(0);
		executeRequest(CLIENT, "http request timeout", request, true);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class)
	public void testHttpConnectionAbortedException() throws HotPadsHttpException {
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "localhost:" + PORT, false);
		executeRequest(CLIENT, "connection aborted exception - invalid host URI", request, true);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class)
	public void testInvalidRequestHeader() throws HotPadsHttpException {
		SERVER.setResponse(301, "301 status code throws exception when not provided a location header");
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		executeRequest(CLIENT, "connection aborted exception - invalid headers", request, true);
	}

	@Test
	public void testSuccessfulRequest() throws HotPadsHttpException {
		int status = STATI[RANDOM.nextInt(STATI.length)];
		String expectedResponse = UUID.randomUUID().toString();
		SERVER.setResponse(status, expectedResponse);

		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		HotPadsHttpResponse response = executeRequest(CLIENT, "successful request", request, true);
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(status, response.getStatusCode());
	}

	@Test
	public void testClientSignatureValidator() throws HotPadsHttpException {
		String salt = "some super secure salty salt " + UUID.randomUUID().toString();
		SignatureValidator signatureValidator = new SignatureValidator(salt);
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().setSignatureValidator(signatureValidator).build();
		HotPadsHttpRequest request;
		HotPadsHttpResponse response;

		Map<String, String> params = new HashMap<String, String>();
		params.put("1", UUID.randomUUID().toString());
		params.put("2", Integer.toString(RANDOM.nextInt()));
		params.put("3", "Everything is awesome! Everything is cool when you're part of a team!");
		params.put("4", "$*&^)}\r\n\\\t~\" drop table User");
		params.put("5", Base64.encodeBase64String(signatureValidator.sign(params)));

		int status = STATI[RANDOM.nextInt(STATI.length)];
		String expectedResponse = Arrays.toString(params.entrySet().toArray());
		SERVER.setResponse(status, expectedResponse);

		// GET request cannot be signed since it has no entity/payload
		request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).addPostParams(params);
		response = executeRequest(client, "signature validator with GET request", request, true);
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(params.size(), request.getPostParams().size());

		request = new HotPadsHttpRequest(HttpRequestMethod.POST, URL, false).addPostParams(params);
		response = executeRequest(client, "signature validator with POST request", request, true);

		String expectedSignature = Base64.encodeBase64String(signatureValidator.sign(params));
		Map<String, String> postParams = request.getPostParams();
		String signature = postParams.get(SecurityParameters.SIGNATURE);
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(params.size() + 1, postParams.size());
		Assert.assertNotNull(signature);
		Assert.assertEquals(expectedSignature, signature);
	}

	@Test
	public void testClientCsrfValidator() throws HotPadsHttpException {
		String cipherKey = "cipherKey";
		String cipherIv = "cipherIv";
		CsrfValidator csrfValidator = new CsrfValidator(cipherKey, cipherIv);
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().setCsrfValidator(csrfValidator).build();
	}

	@Test
	public void testClientApiKeyPredicate() throws HotPadsHttpException {
		DefaultApiKeyPredicate apiKeyPredicate = new DefaultApiKeyPredicate("apiKey");
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().setApiKeyPredicate(apiKeyPredicate).build();
	}

	private HotPadsHttpResponse executeRequest(HotPadsHttpClient client, String message, HotPadsHttpRequest request,
			boolean checked) throws HotPadsHttpException {
		System.out.println("\n" + message);
		try {
			HotPadsHttpResponse response = checked ? client.executeChecked(request) : CLIENT.execute(request);
			System.out.println("status " + response.getStatusCode());
			System.out.println("entity " + response.getEntity());
			return response;
		} catch (HotPadsHttpException | HotPadsHttpRuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
