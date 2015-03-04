package com.hotpads.util.http.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.HotPadsHttpResponse;
import com.hotpads.util.http.response.exception.HotPadsHttpConnectionAbortedException;
import com.hotpads.util.http.response.exception.HotPadsHttpException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestExecutionException;
import com.hotpads.util.http.response.exception.HotPadsHttpRequestFutureTimeoutException;
import com.hotpads.util.http.response.exception.HotPadsHttpResponseException;
import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.DefaultApiKeyPredicate;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

public class HotPadsHttpClientIntegrationTests {
	private static final Logger logger = LoggerFactory.getLogger(HotPadsHttpClientIntegrationTests.class);
	private static final int PORT = 9091;
	private static final String URL = "http://localhost:" + PORT + "/";
	private static final Random RANDOM = new Random(115509410414623L);
	private static SimpleHttpResponseServer server;

	private static class SimpleHttpResponseServer extends Thread {
		private volatile boolean done = false;
		private volatile int status = 200;
		private volatile int sleepMs = 0;
		private volatile String response = "hello world";

		@Override
		public void run() {
			try (ServerSocket server = new ServerSocket(PORT)) {
				while (!done) {
					accept(server);
					sleep(100);
				}
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		private synchronized void done() {
			done = true;
		}

		private synchronized void setResponse(int status, String response) {
			this.status = status;
			this.response = response;
		}

		private synchronized void setResponseDelay(int sleepMs) {
			this.sleepMs = sleepMs;
		}

		private void accept(ServerSocket server) throws IOException, InterruptedException {
			try (PrintWriter out = new PrintWriter(server.accept().getOutputStream())) {
				logger.debug("connection accepted");
				if (sleepMs > 0) {
					logger.debug("begin sleep");
					sleep(sleepMs);
					logger.debug("end sleep");
				}
				out.println("HTTP/1.1 " + status);
				out.println("Content-Type: text/plain");
				out.println("Content-Length: " + response.length());
				out.println();
				out.println(response);
				out.flush();
				logger.debug("flushed response");
			}
			// NOTE if removing the try-with-resources clause, don't forget the out.close() !!
		}
	}

	@BeforeClass
	public static void setUp() {
		server = new SimpleHttpResponseServer();
		server.start();
	}

	@AfterClass
	public static void tearDown() {
		server.done();
	}

	@Test(expected = HotPadsHttpRuntimeException.class)
	public void testUncheckedException() {
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "invalidLocation", false);
		client.execute(request);
	}

	@Test(expected = HotPadsHttpRequestFutureTimeoutException.class, timeout = 1000)
	public void testRequestFutureTimeoutException() throws HotPadsHttpException {
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).setFutureTimeoutMs(0L);
		client.executeChecked(request);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class, timeout = 1000)
	public void testRequestTimeoutException() throws HotPadsHttpException {
		server.setResponseDelay(200);
		try {
			HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
			HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).setTimeoutMs(100);
			client.executeChecked(request);
		} catch (Exception e) {
			throw e;
		} finally {
			server.setResponseDelay(0);
		}
	}

	@Test(expected = HotPadsHttpRequestExecutionException.class, timeout = 1000)
	public void testInvalidLocation() throws HotPadsHttpException {
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, "invalidLocation", false);
		client.executeChecked(request);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class)
	public void testInvalidRequestHeader() throws HotPadsHttpException {
		server.setResponse(301, "301 status code throws exception when not provided a location header");
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		client.executeChecked(request);
	}

	@Test(expected = HotPadsHttpConnectionAbortedException.class, timeout = 1500)
	public void testRetryFailure() throws HotPadsHttpException {
		server.setResponseDelay(200);
		try {
			HotPadsHttpClient client = new HotPadsHttpClientBuilder().setRetryCount(10).build();
			HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, true).setTimeoutMs(100);
			client.executeChecked(request);
		} finally {
			server.setResponseDelay(0);
		}
	}

	@Test
	public void testSuccessfulRequests() {
		int status = HttpStatus.SC_OK;
		String expectedResponse = UUID.randomUUID().toString();
		server.setResponse(status, expectedResponse);
		HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
		HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
		HotPadsHttpResponse response = client.execute(request);
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(status, response.getStatusCode());
	}
	
	@Test(expected = HotPadsHttpResponseException.class)
	public void testBadRequestFailure() throws HotPadsHttpException {
		try {
			int status = HttpStatus.SC_BAD_REQUEST;
			String expectedResponse = UUID.randomUUID().toString();
			server.setResponse(status, expectedResponse);
			HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
			HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
			client.executeChecked(request);
		} catch (HotPadsHttpResponseException e) {
			Assert.assertTrue(e.isClientError());
			HotPadsHttpResponse response = e.getResponse();
			Assert.assertNotNull(response);
			Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCode());
			throw e;
		}
	}

	@Test(expected = HotPadsHttpResponseException.class)
	public void testServiceUnavailableFailure() throws HotPadsHttpException {
		try {
			int status = HttpStatus.SC_SERVICE_UNAVAILABLE;
			String expectedResponse = UUID.randomUUID().toString();
			server.setResponse(status, expectedResponse);
			HotPadsHttpClient client = new HotPadsHttpClientBuilder().build();
			HotPadsHttpRequest request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false);
			client.executeChecked(request);
		} catch (HotPadsHttpResponseException e) {
			Assert.assertTrue(e.isServerError());
			HotPadsHttpResponse response = e.getResponse();
			Assert.assertNotNull(response);
			Assert.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusCode());
			throw e;
		}
	}

	@Test
	public void testSecurityComponents() {
		HotPadsHttpClient client;
		HotPadsHttpRequest request;
		HotPadsHttpResponse response;
		Map<String, String> postParams;

		String salt = "some super secure salty salt " + UUID.randomUUID().toString();
		String cipherKey = "kirg king kind " + UUID.randomUUID().toString();
		String cipherIv = "iv independent variable https://en.wikipedia.org/wiki/IV " + UUID.randomUUID().toString();
		String apiKey = "apiKey advanced placement incremental key " + UUID.randomUUID().toString();

		SignatureValidator signatureValidator = new SignatureValidator(salt);
		CsrfValidator csrfValidator = new CsrfValidator(cipherKey, cipherIv);
		DefaultApiKeyPredicate apiKeyPredicate = new DefaultApiKeyPredicate(apiKey);

		client = new HotPadsHttpClientBuilder().setSignatureValidator(signatureValidator)
				.setCsrfValidator(csrfValidator).setApiKeyPredicate(apiKeyPredicate).build();

		Map<String, String> params = new HashMap<String, String>();
		params.put("1", UUID.randomUUID().toString());
		params.put("2", Integer.toString(RANDOM.nextInt()));
		params.put("3", "Everything is awesome! Everything is cool when you're part of a team!");

		String expectedResponse = Arrays.toString(params.entrySet().toArray());
		server.setResponse(HttpStatus.SC_ACCEPTED, expectedResponse);

		// GET request cannot be signed
		request = new HotPadsHttpRequest(HttpRequestMethod.GET, URL, false).addPostParams(params);
		response = client.execute(request);
		postParams = request.getPostParams();
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(params.size(), postParams.size());
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		// entity enclosing request with no entity or params cannot be signed
		request = new HotPadsHttpRequest(HttpRequestMethod.POST, URL, false);
		response = client.execute(request);
		postParams = request.getPostParams();
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(3, request.getPostParams().size());
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNotNull(postParams.get(SecurityParameters.SIGNATURE));

		// entity enclosing request already with an entity cannot be signed, even with params
		request = new HotPadsHttpRequest(HttpRequestMethod.PATCH, URL, false).setEntity(params).addPostParams(params);
		response = client.execute(request);
		postParams = request.getPostParams();
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(3, postParams.size());
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		// entity enclosing request is signed with entity from post params
		request = new HotPadsHttpRequest(HttpRequestMethod.POST, URL, false).addPostParams(params);
		response = client.execute(request);
		postParams = request.getPostParams();
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(params.size() + 3, postParams.size());
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNotNull(postParams.get(SecurityParameters.SIGNATURE));

		// test equivalence classes
		client = new HotPadsHttpClientBuilder().setCsrfValidator(csrfValidator).build();

		request = new HotPadsHttpRequest(HttpRequestMethod.PUT, URL, false).addPostParams(params);
		response = client.execute(request);
		postParams = request.getPostParams();
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(params.size() + 1, postParams.size());
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new HotPadsHttpClientBuilder().setApiKeyPredicate(apiKeyPredicate).build();

		request = new HotPadsHttpRequest(HttpRequestMethod.PATCH, URL, false).addPostParams(params);
		response = client.execute(request);
		postParams = request.getPostParams();
		Assert.assertEquals(expectedResponse, response.getEntity());
		Assert.assertEquals(params.size() + 1, postParams.size());
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));
	}
}
