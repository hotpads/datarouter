package com.hotpads.datarouter.monitoring.latency;

public class DatarouterClientLatencyCheck extends LatencyCheck{

	private final String clientName;

	public DatarouterClientLatencyCheck(String name, Runnable check, String clientName){
		super(name, check);
		this.clientName = clientName;
	}

	public String getClientName(){
		return clientName;
	}

}
