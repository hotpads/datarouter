package com.hotpads.datarouter.util.callsite;

import java.util.Date;

import com.hotpads.datarouter.util.core.DateTool;


public class CallsiteRecord{

	private Date timestamp;
	private String nodeName;
	private String datarouterMethodName;
	private String callsite;
	private long numItems;
	private long durationNs;
	
	
	/************** construct ****************/
	
	public CallsiteRecord(Date timestamp, String nodeName, String datarouterMethodName, String callsite, long numItems, 
			long durationNs){
		this.timestamp = timestamp;
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
		String[] allTokens = line.split(" ");
		String dateTime = allTokens[0] + " " + allTokens[1];
		Date timestamp = DateTool.parseUserInputDate(dateTime, 2014);
		
		String afterThreadName = line.substring(line.indexOf("]") + 1);
		String[] lineTokens = afterThreadName.split(" ");
		int i = 3;
		String nodeName = lineTokens[i++];
		String datarouterMethodName = lineTokens[i++];
		String callsite = lineTokens[i++];
		Integer numItems = Integer.valueOf(lineTokens[i++]);
		Long microseconds = Long.valueOf(lineTokens[i++]); 
		Long nanoseconds = 1000 * microseconds;
		return new CallsiteRecord(timestamp, nodeName, datarouterMethodName, callsite, numItems, nanoseconds);
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

	public long getNumItems(){
		return numItems;
	}

	public long getDurationNs(){
		return durationNs;
	}
	
	public Date getTimestamp(){
		return timestamp;
	}
	
}
