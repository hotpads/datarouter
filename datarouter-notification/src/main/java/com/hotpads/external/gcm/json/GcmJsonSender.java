/*
 *
 *
 *
 * this is a significantly modified version of
 * github.com/google/gcm/blob/master/client-libraries/java/rest-client/src/com/google/android/gcm/server/Sender.java
 * that scraps form encoding and enables using some of the newer JSON-only features required by iOS
 *
 *
 *
 *
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotpads.external.gcm.json;

import static com.hotpads.external.gcm.Constants.GCM_SEND_ENDPOINT;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Helper class to send messages to the GCM service using an API Key.
 */
public class GcmJsonSender{

	protected static final String UTF8 = "UTF-8";

	/**
	 * Initial delay before first retry, without jitter.
	 */
	protected static final int BACKOFF_INITIAL_DELAY = 1000;
	/**
	 * Maximum delay before a retry.
	 */
	protected static final int MAX_BACKOFF_DELAY = 1024000;

	protected final Random random = new Random();
	protected static final Logger logger = LoggerFactory.getLogger(GcmJsonSender.class.getName());

	private final String key;

	/**
	 * Default constructor.
	 *
	 * @param key API key obtained through the Google API Console.
	 */
	public GcmJsonSender(String key){
		this.key = nonNull(key);
	}

	/**
	 * Sends a message to one device, retrying in case of unavailability.
	 * <strong>Note: </strong> this method uses exponential back-off to retry in
	 * case of service unavailability and hence could block the calling thread
	 * for many seconds.
	 *
	 * @param message message to be sent, including the device's registration id.
	 * @param retries number of retries in case of service unavailability errors.
	 * @return result of the request (see its javadoc for more details).
	 * @throws IllegalArgumentException if registrationId is {@literal null}.
	 * @throws IOException              if message could not be sent.
	 */
	public GcmResponse send(GcmRequest request, int retries) throws IOException{
		System.out.println(request);
		int attempt = 0;
		GcmResponse result;
		int backoff = BACKOFF_INITIAL_DELAY;
		boolean tryAgain;
		do{
			attempt++;
			logger.info("Attempt #" + attempt + " to send message to regIds " + request.to);
			result = sendNoRetry(request);
			tryAgain = result == null && attempt <= retries;
			if(tryAgain){
				int sleepTime = backoff / 2 + random.nextInt(backoff);
				sleep(sleepTime);
				if(2 * backoff < MAX_BACKOFF_DELAY){
					backoff *= 2;
				}
			}
		}while(tryAgain);
		if(result == null){
			throw new IOException("Could not send message after " + attempt + " attempts");
		}
		return result;
	}

	/**
	 * Sends a message without retrying in case of service unavailability. See
	 * {@link #send(Message, String, int)} for more info.
	 *
	 * @return result of the post, or {@literal null} if the GCM service was
	 * unavailable or any network exception caused the request to fail.
	 * @throws IllegalArgumentException if registrationId is {@literal null}.
	 */
	public GcmResponse sendNoRetry(GcmRequest request){
		Gson gson = new Gson();
		String requestBody = gson.toJson(request);
		logger.info("Request body: " + requestBody);
		HttpURLConnection conn;
		int status;
		try{
			conn = post(GCM_SEND_ENDPOINT, requestBody);
			status = conn.getResponseCode();
		}catch(IOException e){
			logger.error("IOException posting to GCM", e);
			return null;
		}
		if(status / 100 == 5){
			logger.error("GCM service is unavailable (status " + status + ")");
			return null;
		}
		String responseBody;
		if(status != 200){
			try{
				responseBody = getAndClose(conn.getErrorStream());
				logger.error("Plain post error response: " + responseBody);
			}catch(IOException e){
				// ignore the exception since it will thrown an InvalidRequestException
				// anyways
				responseBody = "N/A";
				logger.error("Exception reading response: ", e);
			}
			throw new IllegalStateException(status + ": " + responseBody);
		}
		try{
			responseBody = getAndClose(conn.getInputStream());
		}catch(IOException e){
			logger.error("Exception reading response: ", e);
			// return null so it can retry
			return null;
		}
		logger.info("Response: " + responseBody);
		GcmResponse result = gson.fromJson(responseBody, GcmResponse.class);
		return result;
	}


	private static void close(Closeable closeable){
		if(closeable != null){
			try{
				closeable.close();
			}catch(IOException e){
				// ignore error
				logger.error("IOException closing stream", e);
			}
		}
	}

	/**
	 * Make an HTTP post to a given URL.
	 *
	 * @return HTTP response.
	 */
	protected HttpURLConnection post(String url, String body)
			throws IOException{
		return post(url, "application/json;charset=UTF-8", body);
	}

	/**
	 * Makes an HTTP POST request to a given endpoint.
	 * <p>
	 * <p>
	 * <strong>Note: </strong> the returned connected should not be disconnected,
	 * otherwise it would kill persistent connections made using Keep-Alive.
	 *
	 * @param url         endpoint to post the request.
	 * @param contentType type of request.
	 * @param body        body of the request.
	 * @return the underlying connection.
	 * @throws IOException propagated from underlying methods.
	 */
	protected HttpURLConnection post(String url, String contentType, String body)
			throws IOException{
		if(url == null || body == null){
			throw new IllegalArgumentException("arguments cannot be null");
		}
		if(!url.startsWith("https://")){
			logger.warn("URL does not use https: " + url);
		}
		logger.info("Sending POST to " + url);
		logger.info("POST body: " + body);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = getConnection(url);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setFixedLengthStreamingMode(bytes.length);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Authorization", "key=" + key);
		OutputStream out = conn.getOutputStream();
		try{
			out.write(bytes);
		}finally{
			close(out);
		}
		return conn;
	}

	/**
	 * Gets an {@link HttpURLConnection} given an URL.
	 */
	protected HttpURLConnection getConnection(String url) throws IOException{
		HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
		return conn;
	}

	/**
	 * Convenience method to convert an InputStream to a String.
	 * <p>
	 * If the stream ends in a newline character, it will be stripped.
	 * <p>
	 * If the stream is {@literal null}, returns an empty string.
	 */
	protected static String getString(InputStream stream) throws IOException{
		if(stream == null){
			return "";
		}
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(stream));
		StringBuilder content = new StringBuilder();
		String newLine;
		do{
			newLine = reader.readLine();
			if(newLine != null){
				content.append(newLine).append('\n');
			}
		}while(newLine != null);
		if(content.length() > 0){
			// strip last newline
			content.setLength(content.length() - 1);
		}
		return content.toString();
	}

	private static String getAndClose(InputStream stream) throws IOException{
		try{
			return getString(stream);
		}finally{
			if(stream != null){
				close(stream);
			}
		}
	}

	private static <T> T nonNull(T argument){
		if(argument == null){
			throw new IllegalArgumentException("argument cannot be null");
		}
		return argument;
	}

	private void sleep(long millis){
		try{
			Thread.sleep(millis);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
	}
}
