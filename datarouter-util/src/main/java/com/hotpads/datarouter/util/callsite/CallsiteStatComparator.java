package com.hotpads.datarouter.util.callsite;

import java.util.Comparator;

import com.hotpads.datarouter.util.callsite.CallsiteStatX.CallsiteCountComparator;
import com.hotpads.datarouter.util.callsite.CallsiteStatX.CallsiteDurationComparator;
import com.hotpads.util.core.java.ReflectionTool;

public enum CallsiteStatComparator{

	COUNT("count", CallsiteCountComparator.class),
	DURATION("duration", CallsiteDurationComparator.class);
	
	
	private final String varName;
	private final Class<? extends Comparator<CallsiteStatX>> comparatorClass;
	
	
	private CallsiteStatComparator(String varName, Class<? extends Comparator<CallsiteStatX>> comparatorClass){
		this.varName = varName;
		this.comparatorClass = comparatorClass;
	}
	
	
	public static CallsiteStatComparator fromVarName(String string){
		for(CallsiteStatComparator comparatorEnum : values()){
			if(comparatorEnum.varName.equals(string)){
				return comparatorEnum;
			}
		}
		throw new IllegalArgumentException(string + " not found");
	}
	
	
	public Comparator<CallsiteStatX> getComparator(){
		return ReflectionTool.create(comparatorClass);
	}
	
	
	public String getVarName(){
		return varName;
	}
}
