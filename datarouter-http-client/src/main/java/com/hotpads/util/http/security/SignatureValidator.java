package com.hotpads.util.http.security;

import java.util.Map;

public interface SignatureValidator{

	boolean checkHexSignatureMulti(Map<String,String[]> parameterMap, String signature, String apiKey);

}
