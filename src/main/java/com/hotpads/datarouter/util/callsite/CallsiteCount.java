package com.hotpads.datarouter.util.callsite;

import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.StringTool;

/************** CallsiteCount ********************/

public class CallsiteCount implements Comparable<CallsiteCount>{
	
	private Long count;
	private String callsite;
	
	public CallsiteCount(Long count, String callsite){
		this.count = count;
		this.callsite = callsite;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callsite == null) ? 0 : callsite.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CallsiteCount other = (CallsiteCount)obj;
		if(callsite == null){
			if(other.callsite != null) return false;
		}else if(!callsite.equals(other.callsite)) return false;
		if(count == null){
			if(other.count != null) return false;
		}else if(!count.equals(other.count)) return false;
		return true;
	}
	
	@Override
	public int compareTo(CallsiteCount that){
		return that.count.compareTo(this.count);
	}
	
	@Override
	public String toString(){
		String countString = NumberFormatter.addCommas(count);
		return StringTool.pad(countString, ' ', 8) + "   " + callsite;
	}

	
	/******************* get/set ***************************/
	
	public Long getCount(){
		return count;
	}

	public String getCallsite(){
		return callsite;
	}
	
	
	
}