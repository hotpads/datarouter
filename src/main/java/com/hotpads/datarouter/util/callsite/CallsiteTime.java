package com.hotpads.datarouter.util.callsite;

import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.StringTool;

/************** CallsiteCount ********************/

@Deprecated//use CallsiteCount
public class CallsiteTime implements Comparable<CallsiteTime>{
	
	private Long microseconds;
	private String callsite;
	
	public CallsiteTime(Long microseconds, String callsite){
		this.microseconds = microseconds;
		this.callsite = callsite;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callsite == null) ? 0 : callsite.hashCode());
		result = prime * result + ((microseconds == null) ? 0 : microseconds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		CallsiteTime other = (CallsiteTime)obj;
		if(callsite == null){
			if(other.callsite != null) return false;
		}else if(!callsite.equals(other.callsite)) return false;
		if(microseconds == null){
			if(other.microseconds != null) return false;
		}else if(!microseconds.equals(other.microseconds)) return false;
		return true;
	}
	
	@Override
	public int compareTo(CallsiteTime that){
		return that.microseconds.compareTo(this.microseconds);
	}
	
	@Override
	public String toString(){
		String countString = NumberFormatter.addCommas(microseconds);
		return StringTool.pad(countString, ' ', 8) + "   " + callsite;
	}
}