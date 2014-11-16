package com.hotpads.datarouter.util.callsite;

import java.util.Comparator;

import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.StringTool;

/************** CallsiteCount ********************/

public class CallsiteStat{

	public static class CallsiteStatKey{
		private String datarouterMethodName;
		private String callsite;
		
		public CallsiteStatKey(String datarouterMethodName, String callsite){
			this.datarouterMethodName = datarouterMethodName;
			this.callsite = callsite;
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((callsite == null) ? 0 : callsite.hashCode());
			result = prime * result + ((datarouterMethodName == null) ? 0 : datarouterMethodName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			CallsiteStatKey other = (CallsiteStatKey)obj;
			if(callsite == null){
				if(other.callsite != null) return false;
			}else if(!callsite.equals(other.callsite)) return false;
			if(datarouterMethodName == null){
				if(other.datarouterMethodName != null) return false;
			}else if(!datarouterMethodName.equals(other.datarouterMethodName)) return false;
			return true;
		}

		public String getDatarouterMethodName(){
			return datarouterMethodName;
		}

		public String getCallsite(){
			return callsite;
		}
		
	}
	
	
	private CallsiteStatKey key;
	private Long count;
	private Long durationNs;
	

	public CallsiteStat(String datarouterMethodName, String callsite, Long count, Long durationNs){
		this.key = new CallsiteStatKey(datarouterMethodName, callsite);
		this.count = count;
		this.durationNs = durationNs;
	}
	
	
	/***************** methods ************************/
	
	public String getReportLine(){
		String countString = NumberFormatter.addCommas(count);
		String durationString = NumberFormatter.addCommas(getDurationUs());
		String avgCallUsString = NumberFormatter.addCommas(getDurationUs() / count);
		return StringTool.pad(countString, ' ', 12)
				+ " " + StringTool.pad(durationString, ' ', 12)
				+ " " + StringTool.pad(avgCallUsString, ' ', 12)
				+ " " + StringTool.padEnd(key.getDatarouterMethodName(), ' ', 20)
				+ " " + key.getCallsite();
	}

	

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CallsiteStat other = (CallsiteStat)obj;
		if(key == null){
			if(other.key != null) return false;
		}else if(!key.equals(other.key)) return false;
		return true;
	}
	
	@Override
	public String toString(){
		String countString = NumberFormatter.addCommas(count);
		return StringTool.pad(countString, ' ', 8) + "   " + key.getCallsite();
	}

	
	/**************** comparator ******************/

	public static class CallsiteCountComparator implements Comparator<CallsiteStat>{
		@Override
		public int compare(CallsiteStat a, CallsiteStat b){
			return a.count.compareTo(b.count);
		}
	}
	
	public static class CallsiteDurationComparator implements Comparator<CallsiteStat>{
		@Override
		public int compare(CallsiteStat a, CallsiteStat b){
			return a.durationNs.compareTo(b.durationNs);
		}
	}
	
	
	/***************** methods ****************************/
	
	public void addMetrics(CallsiteStat other){
		this.count += other.getCount();
		this.durationNs += other.getDurationNs();
	}
	
	public Long getDurationUs(){
		return durationNs / 1000;
	}
	
	
	/******************* get/set ***************************/
	
	public Long getCount(){
		return count;
	}

	public CallsiteStatKey getKey(){
		return key;
	}

	public Long getDurationNs(){
		return durationNs;
	}
	
	
}