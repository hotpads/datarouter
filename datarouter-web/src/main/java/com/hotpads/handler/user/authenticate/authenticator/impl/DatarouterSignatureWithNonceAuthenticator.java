package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.api.ApiRequest;
import com.hotpads.handler.user.authenticate.api.ApiRequestKey;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.exception.InvalidApiCallException;
import com.hotpads.util.http.HttpSignatureTool;
import com.hotpads.util.http.RequestTool;

public class DatarouterSignatureWithNonceAuthenticator extends DatarouterSignatureAuthenticator{

	private static final int NUM_MAX_TIME_DIFF_IN_SECONDS = 600;

	public DatarouterSignatureWithNonceAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes){
		super(request, response, authenticationConfig, userNodes);
	}

	@Override
	protected DatarouterSession getSession(HttpServletRequest request){
		String timestamp = request.getParameter(authenticationConfig.getTimestampParam());
		if(!isTimestampValid(timestamp)){
			throw new InvalidApiCallException("invalid timestamp specified");
		}

		String apiKey = request.getParameter(authenticationConfig.getApiKeyParam());
		String signature = request.getParameter(authenticationConfig.getSignatureParam());
		String nonce = request.getParameter(authenticationConfig.getNonceParam());
		DatarouterUser user = lookupUserByApiKeyAndValidate(apiKey);
		ApiRequest apiRequest = lookupNonceProtectedRequestAndValidate(apiKey, nonce, signature, timestamp);

		String uri = request.getRequestURI();
		Map<String, String> params = RequestTool.getMapOfParameters(request);
		params.remove("signature");
		String expectedSignature = HttpSignatureTool.generateSignature(uri, params, user.getSecretKey());
		if(DrObjectTool.notEquals(expectedSignature, signature)){
			throw new InvalidApiCallException("invalid signature specified");
		}

		//if request reaches this point, it is valid, save it and return session
		apiRequest.setRequestDate(new Date());
		userNodes.getApiRequestNode().put(apiRequest, null);
		DatarouterSession session = DatarouterSession.createFromUser(user);
		session.setPersistent(false);

		return session;
	}

	private ApiRequest lookupNonceProtectedRequestAndValidate(String apiKey, String nonce, String signature,
			String timestamp){
		if(DrStringTool.isNullOrEmpty(apiKey)){
			throw new InvalidApiCallException("no api key specified");
		}
		if(DrStringTool.isNullOrEmpty(nonce)){
			throw new InvalidApiCallException("no nonce specified");
		}
		if(DrStringTool.isNullOrEmpty(signature)){
			throw new InvalidApiCallException("no signature specified");
		}

		ApiRequest testRequest = userNodes.getApiRequestNode().lookupUnique(
				new ApiRequestKey(apiKey, nonce, signature, timestamp),
				null);

		if(testRequest != null){
			throw new InvalidApiCallException("exact request has already be made");
		}

		return new ApiRequest(apiKey, nonce, signature, timestamp);
	}

	private static boolean isTimestampValid(String timestampParameter){
		Date timestampDate;
		try{
			long timestampInMillisecond = Long.valueOf(timestampParameter) * 1000;
			timestampDate = new Date(timestampInMillisecond);
		}catch(Exception e){
			throw new InvalidApiCallException("invalid timestamp specified");
		}
		long timeDifferenceInSeconds = Math.round(DrDateTool.getSecondsBetween(new Date(), timestampDate));

		return Math.abs(timeDifferenceInSeconds) <= NUM_MAX_TIME_DIFF_IN_SECONDS;
	}

	/************************** tests ******************************/

	public static class Tests{

		private static final int NUM_VALID_TIME_DIFF_IN_MILLISECONDS = 100000;
		private static final int NUM_INVALID_TIME_DIFF_IN_MILLISECONDS = Integer.MAX_VALUE;
		private static String invalidTimestamp = "12345678901234567890123456789";
		private static String invalidStringTimestamp = "NOT_AN_ACTUAL_NUMBER";
		private static String emptyStringTimestamp = "";

		private String getStringTimestamp(int timeDiffenceInMilliseconds){
			long timeInSeconds = (new Date().getTime() + timeDiffenceInMilliseconds) / 1000;

			return String.valueOf(timeInSeconds);
		}

		@Test
		public void testIsTimestampValid(){
			Assert.assertTrue(isTimestampValid(getStringTimestamp(NUM_VALID_TIME_DIFF_IN_MILLISECONDS)));
			Assert.assertTrue(isTimestampValid(getStringTimestamp(-NUM_VALID_TIME_DIFF_IN_MILLISECONDS)));
			Assert.assertFalse(isTimestampValid(getStringTimestamp(NUM_INVALID_TIME_DIFF_IN_MILLISECONDS)));
			Assert.assertFalse(isTimestampValid(getStringTimestamp(-NUM_INVALID_TIME_DIFF_IN_MILLISECONDS)));
		}

		@Test(expected = InvalidApiCallException.class)
		public void testIsTimestampValidWithInvalidString(){
			isTimestampValid(invalidStringTimestamp);
		}

		@Test(expected = InvalidApiCallException.class)
		public void testIsTimestampValidExceptionWithLargeNumber(){
			isTimestampValid(invalidTimestamp);
		}

		@Test(expected = InvalidApiCallException.class)
		public void testIsTimestampValidExceptionWithEmptyString(){
			isTimestampValid(emptyStringTimestamp);
		}

		@Test(expected = InvalidApiCallException.class)
		public void testIsTimestampValidExceptionWithNull(){
			isTimestampValid(null);
		}

	}
}
