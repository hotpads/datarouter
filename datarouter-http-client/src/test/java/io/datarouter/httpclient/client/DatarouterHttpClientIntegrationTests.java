/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.httpclient.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.HttpRequestMethod;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.httpclient.response.HttpStatusCode;
import io.datarouter.httpclient.response.exception.DatarouterHttpConnectionAbortedException;
import io.datarouter.httpclient.response.exception.DatarouterHttpException;
import io.datarouter.httpclient.response.exception.DatarouterHttpResponseException;
import io.datarouter.httpclient.response.exception.DatarouterHttpRuntimeException;
import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.DefaultCsrfGenerator.RefreshableDefaultCsrfGenerator;
import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.httpclient.security.DefaultSignatureGenerator.RefreshableDefaultSignatureGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.instrumentation.refreshable.RefreshableStringSupplier;

@Test(singleThreaded = true)
public class DatarouterHttpClientIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterHttpClientIntegrationTests.class);

	private static final int PORT = 9091;
	private static final String URL = "http://localhost:" + PORT + "/";
	private static final Random RANDOM = new Random(115509410414623L);
	private static final String CLIENT_NAME = "test-client";

	private static SimpleHttpResponseServer server;

	private static class SimpleHttpResponseServer extends Thread{

		private volatile boolean done = false;
		private volatile int status = HttpStatusCode.SC_200_OK.getStatusCode();
		private volatile int sleepMs = 0;
		private volatile String response = "hello world";

		@Override
		public void run(){
			try(ServerSocket server = new ServerSocket(PORT)){
				while(!done){
					accept(server);
					sleep(100);
				}
			}catch(IOException | InterruptedException e){
				throw new RuntimeException(e);
			}
		}

		private synchronized void done(){
			done = true;
		}

		private synchronized void setResponse(int status, String response){
			this.status = status;
			this.response = response;
		}

		private synchronized void setResponseDelay(int sleepMs){
			this.sleepMs = sleepMs;
		}

		private void accept(ServerSocket server) throws IOException, InterruptedException{
			try(PrintWriter out = new PrintWriter(server.accept().getOutputStream())){
				logger.debug("connection accepted");
				if(sleepMs > 0){
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
	public static void setUp(){
		server = new SimpleHttpResponseServer();
		server.start();
	}

	@AfterClass
	public static void tearDown(){
		server.done();
	}

	@Test(expectedExceptions = DatarouterHttpRuntimeException.class)
	public void testUncheckedException(){
		DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT).build();
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, "invalidLocation")
				.setRetrySafe(false);
		client.execute(request);
	}

	@Test(expectedExceptions = DatarouterHttpConnectionAbortedException.class)
	public void testRequestTimeoutException() throws DatarouterHttpException{
		server.setResponseDelay(200);
		try{
			DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
					.build();
			DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
					.setTimeout(Duration.ofMillis(100))
					.setRetrySafe(false);
			client.executeChecked(request);
		}finally{
			server.setResponseDelay(0);
		}
	}

	@Test(expectedExceptions = DatarouterHttpConnectionAbortedException.class, timeOut = 1000)
	public void testInvalidLocation() throws DatarouterHttpException{
		DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT).build();
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, "invalidLocation")
				.setRetrySafe(false);
		client.executeChecked(request);
	}

	@Test(expectedExceptions = DatarouterHttpConnectionAbortedException.class)
	public void testInvalidRequestHeader() throws DatarouterHttpException{
		server.setResponse(HttpStatus.SC_MOVED_PERMANENTLY,
				"301 status code throws exception when not provided a location header");
		DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT).build();
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL).setRetrySafe(false);
		client.executeChecked(request);
	}

	@Test(expectedExceptions = DatarouterHttpConnectionAbortedException.class, timeOut = 1500)
	public void testRetryFailure() throws DatarouterHttpException{
		server.setResponseDelay(200);
		try{
			DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
					.setRetryCount(() -> 10)
					.build();
			DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
					.setRetrySafe(true)
					.setTimeout(Duration.ofMillis(100));
			client.executeChecked(request);
		}finally{
			server.setResponseDelay(0);
		}
	}

	@Test
	public void testSuccessfulRequests(){
		int status = HttpStatus.SC_OK;
		String expectedResponse = UUID.randomUUID().toString();
		server.setResponse(status, expectedResponse);
		DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT).build();
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
				.setRetrySafe(false);
		DatarouterHttpResponse response = client.execute(request);
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(response.getStatusCode(), status);
	}

	@Test(expectedExceptions = DatarouterHttpResponseException.class)
	public void testBadRequestFailure() throws DatarouterHttpException{
		try{
			int status = HttpStatus.SC_BAD_REQUEST;
			String expectedResponse = UUID.randomUUID().toString();
			server.setResponse(status, expectedResponse);
			DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
					.build();
			DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
					.setRetrySafe(false);
			client.executeChecked(request);
		}catch(DatarouterHttpResponseException e){
			Assert.assertTrue(e.isClientError());
			DatarouterHttpResponse response = e.getResponse();
			Assert.assertNotNull(response);
			Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
			throw e;
		}
	}

	@Test(expectedExceptions = DatarouterHttpResponseException.class)
	public void testServiceUnavailableFailure() throws DatarouterHttpException{
		try{
			int status = HttpStatus.SC_SERVICE_UNAVAILABLE;
			String expectedResponse = UUID.randomUUID().toString();
			server.setResponse(status, expectedResponse);
			DatarouterHttpClient client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
					.build();
			DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
					.setRetrySafe(false);
			client.executeChecked(request);
		}catch(DatarouterHttpResponseException e){
			Assert.assertTrue(e.isServerError());
			DatarouterHttpResponse response = e.getResponse();
			Assert.assertNotNull(response);
			Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_SERVICE_UNAVAILABLE);
			throw e;
		}
	}

	@Test
	public void testSecurityComponents(){
		DatarouterHttpClient client;
		DatarouterHttpRequest request;
		DatarouterHttpResponse response;
		Map<String,String> postParams;

		String salt = "some super secure salty salt " + UUID.randomUUID().toString();
		String cipherKey = "kirg king kind " + UUID.randomUUID().toString();
		String apiKey = "apiKey advanced placement incremental key " + UUID.randomUUID().toString();

		DefaultSignatureGenerator signatureGenerator = new DefaultSignatureGenerator(() -> salt);
		DefaultCsrfGenerator csrfGenerator = new DefaultCsrfGenerator(() -> cipherKey);
		Supplier<String> apiKeySupplier = () -> apiKey;

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier).build();

		Map<String,String> params = new HashMap<>();
		params.put("1", UUID.randomUUID().toString());
		params.put("2", Integer.toString(RANDOM.nextInt()));
		params.put("3", "Everything is awesome! Everything is cool when you're part of a team!");

		String expectedResponse = Arrays.toString(params.entrySet().toArray());
		server.setResponse(HttpStatus.SC_ACCEPTED, expectedResponse);

		// GET request cannot be signed
		request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
				.setRetrySafe(false)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size());
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		// entity enclosing request with no entity or params cannot be signed
		request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(request.getPostParams().size(), 4);
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNotNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		// entity enclosing request already with an entity cannot be signed, even with params
		request = new DatarouterHttpRequest(HttpRequestMethod.PATCH, URL)
				.setRetrySafe(false)
				.setEntity(params)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), 3);
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		// entity enclosing request is signed with entity from post params
		request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL).addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size() + 4);
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNotNull(postParams.get(SecurityParameters.SIGNATURE));

		// test equivalence classes
		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setCsrfGenerator(csrfGenerator)
				.build();

		request = new DatarouterHttpRequest(HttpRequestMethod.PUT, URL)
				.setRetrySafe(false)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size() + 2);
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		request = new DatarouterHttpRequest(HttpRequestMethod.PATCH, URL)
				.setRetrySafe(false)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size() + 1);
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));
	}

	@Test
	public void testSecurityComponentsWithRefreshableSuppliers(){
		DatarouterHttpClient client;
		DatarouterHttpRequest request;
		DatarouterHttpResponse response;
		Map<String,String> postParams;

		String salt = "some super secure salty salt " + UUID.randomUUID().toString();
		String cipherKey = "kirg king kind " + UUID.randomUUID().toString();
		String apiKey = "apiKey advanced placement incremental key " + UUID.randomUUID().toString();

		DefaultSignatureGenerator signatureGenerator = new RefreshableDefaultSignatureGenerator(() -> salt);
		DefaultCsrfGenerator csrfGenerator = new RefreshableDefaultCsrfGenerator(() -> cipherKey);
		Supplier<String> apiKeySupplier = new RefreshableStringSupplier(() -> apiKey);

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier).build();

		Map<String,String> params = new HashMap<>();
		params.put("1", UUID.randomUUID().toString());
		params.put("2", Integer.toString(RANDOM.nextInt()));
		params.put("3", "Everything is awesome! Everything is cool when you're part of a team!");

		String expectedResponse = Arrays.toString(params.entrySet().toArray());
		server.setResponse(HttpStatus.SC_ACCEPTED, expectedResponse);

		// GET request cannot be signed
		request = new DatarouterHttpRequest(HttpRequestMethod.GET, URL)
				.setRetrySafe(false)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size());
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		// entity enclosing request with no entity or params cannot be signed
		request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(request.getPostParams().size(), 4);
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNotNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		// entity enclosing request already with an entity cannot be signed, even with params
		request = new DatarouterHttpRequest(HttpRequestMethod.PATCH, URL)
				.setRetrySafe(false)
				.setEntity(params)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), 3);
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setSignatureGenerator(signatureGenerator)
				.setCsrfGenerator(csrfGenerator)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		// entity enclosing request is signed with entity from post params
		request = new DatarouterHttpRequest(HttpRequestMethod.POST, URL).addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size() + 4);
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNotNull(postParams.get(SecurityParameters.SIGNATURE));

		// test equivalence classes
		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setCsrfGenerator(csrfGenerator)
				.build();

		request = new DatarouterHttpRequest(HttpRequestMethod.PUT, URL)
				.setRetrySafe(false)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size() + 2);
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNotNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));

		client = new DatarouterHttpClientBuilder(CLIENT_NAME, GsonJsonSerializer.DEFAULT)
				.setApiKeySupplier(apiKeySupplier)
				.build();

		request = new DatarouterHttpRequest(HttpRequestMethod.PATCH, URL)
				.setRetrySafe(false)
				.addPostParams(params);
		response = client.execute(request);
		postParams = request.getFirstPostParams();
		Assert.assertEquals(response.getEntity(), expectedResponse);
		Assert.assertEquals(postParams.size(), params.size() + 1);
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_IV));
		Assert.assertNull(postParams.get(SecurityParameters.CSRF_TOKEN));
		Assert.assertNotNull(postParams.get(SecurityParameters.API_KEY));
		Assert.assertNull(postParams.get(SecurityParameters.SIGNATURE));
	}

}
