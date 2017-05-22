package com.hotpads.util.http.security;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Sha1ApiSignatureTool{

	private String secretKey;
	private static final String HMAC_SHA1 = "HmacSHA1";

	public Sha1ApiSignatureTool(String secretKey){
		this.secretKey = secretKey;
	}

	public String getSignature(String urlWithParameters) throws GeneralSecurityException{
		byte[] signature = getSha1Signature(urlWithParameters, secretKey);
		return new Base64(-1).encodeToString(signature); // -1 prevents crlf
	}

	private byte[] getSha1Signature(String data, String privateKey) throws GeneralSecurityException{
		Key secretKey = new SecretKeySpec(privateKey.getBytes(), HMAC_SHA1);
		Mac authCode = Mac.getInstance(HMAC_SHA1);
		authCode.init(secretKey);
		return authCode.doFinal(data.getBytes());
	}

	/** tests ****************************************************************/
	public static class Sha1ApiSignatureToolTests{
		@Test
		public void testGetSignature() throws GeneralSecurityException{
			Sha1ApiSignatureTool zigner = new Sha1ApiSignatureTool("06356d75-7e80-476b-a949-a4e371ebebeb");
			Assert.assertEquals("MPet95hJOOS8ifLKUeNlfFVnGWc=", zigner.getSignature(
					"https://www.zillow.com/webservice/internal/UnblockListingForZpid.htm?zpid=13775149"));
			zigner = new Sha1ApiSignatureTool("a440aea1-eae1-4833-9e08-bcaf82de647a");
			Assert.assertEquals("KJxvMCmG893evqLroroJzhvPH1s=", zigner.getSignature(
					"https://www.tes600.zillow.local/webservice/internal/UnblockListingForZpid.htm?zpid=48663404"));
		}
	}

}
