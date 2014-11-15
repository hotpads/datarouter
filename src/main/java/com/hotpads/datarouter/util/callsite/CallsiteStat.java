package com.hotpads.datarouter.util.callsite;

import java.util.Comparator;

import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.StringTool;

/************** CallsiteCount ********************/

public class CallsiteStat implements Comparable<CallsiteStat>{

	private String datarouterMethodName;
	private String callsite;
	private Long count;
	private Long durationUs;
	

	public CallsiteStat(String datarouterMethodName, String callsite, Long count, Long durationUs){
		this.datarouterMethodName = datarouterMethodName;
		this.callsite = callsite;
		this.count = count;
		this.durationUs = durationUs;
	}
	
	
	/***************** methods ************************/
	
	public String getReportLine(){
		String countString = NumberFormatter.addCommas(count);
		String durationString = NumberFormatter.addCommas(durationUs);
		String avgCallUsString = NumberFormatter.addCommas(durationUs / count);
		return StringTool.pad(countString, ' ', 12)
				+ " " + StringTool.pad(durationString, ' ', 12)
				+ " " + StringTool.pad(avgCallUsString, ' ', 12)
//				+ " " + StringTool.pad(datarouterMethodName, ' ', 20)
				+ " " + callsite;
	}

	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callsite == null) ? 0 : callsite.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((durationUs == null) ? 0 : durationUs.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CallsiteStat other = (CallsiteStat)obj;
		if(callsite == null){
			if(other.callsite != null) return false;
		}else if(!callsite.equals(other.callsite)) return false;
		if(count == null){
			if(other.count != null) return false;
		}else if(!count.equals(other.count)) return false;
		if(durationUs == null){
			if(other.durationUs != null) return false;
		}else if(!durationUs.equals(other.durationUs)) return false;
		return true;
	}


	@Override
	public int compareTo(CallsiteStat that){
		return this.count.compareTo(that.count);
	}
	
	@Override
	public String toString(){
		String countString = NumberFormatter.addCommas(count);
		return StringTool.pad(countString, ' ', 8) + "   " + callsite;
	}

	
	/**************** duration comparator ******************/
	
	public static class CallsiteDurationComparator implements Comparator<CallsiteStat>{
		@Override
		public int compare(CallsiteStat a, CallsiteStat b){
			return a.count.compareTo(b.count);
		}
	}

	
	/******************* get/set ***************************/
	
	public Long getCount(){
		return count;
	}

	public String getCallsite(){
		return callsite;
	}
	
	public Long getDurationUs(){
		return durationUs;
	}

	public String getDatarouterMethodName(){
		return datarouterMethodName;
	}
	
	
	
}