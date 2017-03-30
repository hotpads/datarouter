package com.hotpads.util.http.security;

import javax.servlet.http.HttpServletRequest;

public interface SignatureValidator{

	boolean checkHexSignatureMulti(HttpServletRequest request);

}
