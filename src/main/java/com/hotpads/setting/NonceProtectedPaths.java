package com.hotpads.setting;

import java.util.List;

public interface NonceProtectedPaths{
	
	List<String> getNonceProtectedPaths();
	boolean isNonceRequiredForPath(String uri);
}
