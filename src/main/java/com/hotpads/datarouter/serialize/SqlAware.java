package com.hotpads.datarouter.serialize;

import java.util.List;

public interface SqlAware{

	List<String> getSqlValuesEscaped();
	List<String> getSqlNameValuePairsEscaped();
	String getSqlNameValuePairsEscapedConjunction();

}
