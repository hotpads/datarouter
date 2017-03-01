package com.hotpads.joblet.dto;

public class JobletHandlerThreadCountDto{
	String jobletType;
	int clusterLimit;
	double instanceAvg;
	int instanceLimit;
	int numExtraThreads;
	int firstExtraInstanceIndex;
	String firstExtraInstanceServerName;
	boolean thisInstanceRunsExtraThread;

	public JobletHandlerThreadCountDto(String jobletType, int clusterLimit, double instanceAvg, int instanceLimit,
			int numExtraThreads, int firstExtraInstanceIndex, String firstExtraInstanceServerName,
			boolean thisInstanceRunsExtraThread){
		this.jobletType = jobletType;
		this.clusterLimit = clusterLimit;
		this.instanceAvg = instanceAvg;
		this.instanceLimit = instanceLimit;
		this.numExtraThreads = numExtraThreads;
		this.firstExtraInstanceIndex = firstExtraInstanceIndex;
		this.firstExtraInstanceServerName = firstExtraInstanceServerName;
		this.thisInstanceRunsExtraThread = thisInstanceRunsExtraThread;
	}

	//getters for jsp
	public String getJobletType(){
		return jobletType;
	}

	public int getClusterLimit(){
		return clusterLimit;
	}

	public double getInstanceAvg(){
		return instanceAvg;
	}

	public int getInstanceLimit(){
		return instanceLimit;
	}

	public int getNumExtraThreads(){
		return numExtraThreads;
	}

	public int getFirstExtraInstanceIndex(){
		return firstExtraInstanceIndex;
	}

	public String getFirstExtraInstanceServerName(){
		return firstExtraInstanceServerName;
	}

	public boolean getThisInstanceRunsExtraThread(){
		return thisInstanceRunsExtraThread;
	}

}