package com.hotpads.datarouter.util.callsite;

public enum CallsiteReportHeader{
	count,
	microSec,
	avgMicroSec,
	numItems,
	avgItems,
	type,
	node,
	method,
	callsite;
	
	public int length(){
		return toString().length();
	}
}
