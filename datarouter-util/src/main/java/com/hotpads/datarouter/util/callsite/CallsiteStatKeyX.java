package com.hotpads.datarouter.util.callsite;


//for some reason, the eclipse error highligher hates the name CallsiteStatKey, so add a random "X"
public class CallsiteStatKeyX{
	private String callsite;
	private String nodeName;

	public CallsiteStatKeyX(String callsite, String nodeName){
		this.callsite = callsite;
		this.nodeName = nodeName;
	}

	public String getCallsite(){
		return callsite;
	}

	public String getNodeName(){
		return nodeName;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (callsite == null ? 0 : callsite.hashCode());
		result = prime * result + (nodeName == null ? 0 : nodeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(!(obj instanceof CallsiteStatKeyX)){
			return false;
		}
		CallsiteStatKeyX other = (CallsiteStatKeyX)obj;
		if(callsite == null){
			if(other.callsite != null){
				return false;
			}
		}else if(!callsite.equals(other.callsite)){
			return false;
		}
		if(nodeName == null){
			if(other.nodeName != null){
				return false;
			}
		}else if(!nodeName.equals(other.nodeName)){
			return false;
		}
		return true;
	}

}
