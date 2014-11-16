package com.hotpads.datarouter.util.callsite;


public class CallsiteRecord{

	private String nodeName;
	private String datarouterMethodName;
	private String callsite;
	private int numItems;
	private long durationNs;
	
	
	/************** construct ****************/
	
	public CallsiteRecord(String nodeName, String datarouterMethodName, String callsite, int numItems, long durationNs){
		this.nodeName = nodeName;
		this.datarouterMethodName = datarouterMethodName;
		this.callsite = callsite;
		this.numItems = numItems;
		this.durationNs = durationNs;
	}
	
	
	/****************** serialize *******************/
	
	public String getLogMessage(){
		long durationUs = durationNs / 1000;
		String message = nodeName
				+ " " + datarouterMethodName
				+ " " + callsite
				+ " " + numItems
				+ " " + durationUs;
		return message;
	}
	
	public static CallsiteRecord fromLogLine(String line){
		String afterThreadName = line.substring(line.indexOf("]") + 1);
		String[] lineTokens = afterThreadName.split(" ");
		int i = 3;
		String nodeName = lineTokens[i++];
		String datarouterMethodName = lineTokens[i++];
		String callsite = lineTokens[i++];
		Integer numItems = Integer.valueOf(lineTokens[i++]);
		Long microseconds = Long.valueOf(lineTokens[i++]); 
		Long nanoseconds = 1000 * microseconds;
		return new CallsiteRecord(nodeName, datarouterMethodName, callsite, numItems, nanoseconds);
	}
	
	
	/**************** methods *************************/

	public long getDurationUs(){
		return durationNs / 1000;
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

	public long getDurationNs(){
		return durationNs;
	}
	
}
