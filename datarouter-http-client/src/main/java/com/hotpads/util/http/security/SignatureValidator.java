package com.hotpads.util.http.security;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Hex;

public class SignatureValidator{

	private static final String HASHING_ALGORITHM = "SHA-256";
	private String salt;

	public SignatureValidator(String salt){
		this.salt = salt;
	}

	public boolean checkHexSignature(Map<String,String> params, String candidateSignature){
		return getHexSignature(params).equals(candidateSignature);
	}

	public boolean checkHexSignatureMulti(Map<String,String[]> params, String candidateSignature){
		return checkHexSignature(multiToSingle(params), candidateSignature);
	}

	@Deprecated
	public boolean checkBase64Signature(Map<String, String> map, String candidateString){
		byte[] signature = sign(map);
		byte[] candidate = Base64.getDecoder().decode(candidateString);
		return Arrays.equals(candidate, signature);
	}

	public byte[] sign(Map<String, String> map){
		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance(HASHING_ALGORITHM);
		}catch(NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
		for(String parameterName : map.keySet()){
			if(parameterName.equals(SecurityParameters.SIGNATURE) || "submitAction".equals(parameterName)){
				continue;
			}
			try{
				md.update(parameterName.concat(map.get(parameterName)).concat(salt).getBytes(StandardCharsets.UTF_8));
				signature.write(md.digest());
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		return signature.toByteArray();
	}

	public String getHexSignature(Map<String,String> params){
		return Hex.encodeHexString(sign(params));
	}

	public byte[] signMulti(Map<String, String[]> data){
		return sign(multiToSingle(data));
	}

	private Map<String, String> multiToSingle(Map<String, String[]> data){
		Map<String, String> map = new HashMap<>();
		for(Entry<String, String[]> entry : data.entrySet()){
			map.put(entry.getKey(), entry.getValue()[0]);
		}
		return map;
	}

}
