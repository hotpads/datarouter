package com.hotpads.setting;

import java.util.List;

public interface NonceProtectedUri{
	
	List<String> getNonceProtectedUris();
	boolean isNonceRequiredForUri(String uri);
}
