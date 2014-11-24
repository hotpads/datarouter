package com.hotpads.handler.user.authenticate.api.enums;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.enums.EnumTool;
import com.hotpads.util.core.enums.StringPersistedEnum;


public enum NonceProtectedApiRequest implements StringPersistedEnum, StringEnum<NonceProtectedApiRequest>{
	//TODO please remove this test uri (and this todo) when you add a new nonce protected request
	TEST("/adproducts/api/v*/test", 1)
	;
	
	private String uri;
	private int key;
	
	private NonceProtectedApiRequest(String uri, int key) {
		this.uri = uri;
		this.key = key;
	}
	@Override
	public String getPersistentString(){
		return uri;
	}

	@Override
	public NonceProtectedApiRequest fromPersistentString(String s){
		return fromPersistentStringStatic(s);
	}
	
	@Override
	public String getDisplay(){
		return uri;
	}
	
	public int getNonceProtectedApiRequestKey () {
		return key;
	}
	
	public static NonceProtectedApiRequest fromPersistentStringStatic(String requestPath){		
		return EnumTool.fromPersistentString(values(), requestPath, false); 
	}
	
	public static boolean areNonceAndTimestampRequired(String apiRequestPath) {
		if(StringTool.isNullOrEmptyOrWhitespace(apiRequestPath)) {
			return false;
		}
		return fromPersistentStringStatic(getVersionAgnosticRequestUri(apiRequestPath)) != null;
	}
	
	private static String getVersionAgnosticRequestUri(String path) {
		if(StringTool.isNull(path)) {
			return null;
		}
		
		return path.replaceFirst("\\/v\\d+\\/", "/v*/");
	}
	
	/************************** tests ******************************/
	
	public static class Tests {
		private static String nonceProtectedRequestUri = NonceProtectedApiRequest.TEST.getPersistentString();
		private static String notNonceProtectedRequestUri = "/adproducts/api/v1/invalidEndpoint";
		private static String emptyString = "";
		private static String whiteSpace = " ";
		
		@Test public void testAreNonceAndTimestampRequired() {
			Assert.assertFalse(areNonceAndTimestampRequired(notNonceProtectedRequestUri));
			Assert.assertFalse(areNonceAndTimestampRequired(null));
			Assert.assertFalse(areNonceAndTimestampRequired(emptyString));
			Assert.assertFalse(areNonceAndTimestampRequired(whiteSpace));
			Assert.assertTrue(areNonceAndTimestampRequired(nonceProtectedRequestUri));			
		}
		
		@Test public void testGetVersionAgnosticRequestPath() {
			Assert.assertEquals(NonceProtectedApiRequest.TEST.getPersistentString(), 
					getVersionAgnosticRequestUri("/adproducts/api/v1/test"));
			Assert.assertEquals("/adproducts/api/v*/testv5", 
					getVersionAgnosticRequestUri("/adproducts/api/v1/testv5"));
			Assert.assertEquals("/adproductsv7/v8api/v*/testv5", 
					getVersionAgnosticRequestUri("/adproductsv7/v8api/v1/testv5"));
			Assert.assertEquals("/adproductsv7/v8api/v*/testv5/v2/", 
					getVersionAgnosticRequestUri("/adproductsv7/v8api/v1/testv5/v2/"));
			Assert.assertEquals(null, getVersionAgnosticRequestUri(null));
			Assert.assertEquals(emptyString, getVersionAgnosticRequestUri(emptyString));
			Assert.assertEquals(whiteSpace, getVersionAgnosticRequestUri(whiteSpace));
		}
	}

}
