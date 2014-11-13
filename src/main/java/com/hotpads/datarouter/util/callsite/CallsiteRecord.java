package com.hotpads.datarouter.util.callsite;


public class CallsiteRecord{

	private String nodeName;
	private String datarouterMethodName;
	private String callsite;
	private int numItems;
	private long durationUs;
	
	
	/************** construct ****************/
	
	public CallsiteRecord(String nodeName, String datarouterMethodName, String callsite, int numItems, long durationUs){
		this.nodeName = nodeName;
		this.datarouterMethodName = datarouterMethodName;
		this.callsite = callsite;
		this.numItems = numItems;
		this.durationUs = durationUs;
	}
	
	public static CallsiteRecord fromLong(String line){
		String afterThreadName = line.substring(line.indexOf("]") + 1);
		String[] lineTokens = afterThreadName.split(" ");
		int i = 3;
		String nodeName = lineTokens[i++];
		String datarouterMethodName = lineTokens[i++];
		String callsite = lineTokens[i++];
		Integer numItems = Integer.valueOf(lineTokens[i++]);
		Long microseconds = Long.valueOf(lineTokens[i++]); 
		return new CallsiteRecord(nodeName, datarouterMethodName, callsite, numItems, microseconds);
	}

	
	/***************** get/set ***********************/
	
	public String getNodeName(){
		return nodeName;
	}

	public String getDatarouterMethodName(){
		return datarouterMethodName;
	}

	public String getCallsite(){
		return callsite;
	}

	public int getNumItems(){
		return numItems;
	}

	public long getDurationUs(){
		return durationUs;
	}
	
	
	
	
}
