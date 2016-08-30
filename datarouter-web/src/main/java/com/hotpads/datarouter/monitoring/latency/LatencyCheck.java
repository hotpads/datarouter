package com.hotpads.datarouter.monitoring.latency;

public class LatencyCheck{

	public final String name;
	public final Runnable check;

	public LatencyCheck(String name, Runnable check){
		this.name = name;
		this.check = check;
	}

}
