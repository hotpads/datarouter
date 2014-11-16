package com.hotpads.datarouter.util.callsite;


//for some reason, the eclipse error highligher hates the name CallsiteStatKey, so add a random "X"
public class CallsiteStatKeyX{
	private String datarouterMethodName;
	private String callsite;
	
	public CallsiteStatKeyX(String datarouterMethodName, String callsite){
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
		CallsiteStatKeyX other = (CallsiteStatKeyX)obj;
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
