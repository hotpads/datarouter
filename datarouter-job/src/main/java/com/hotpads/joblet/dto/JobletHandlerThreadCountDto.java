package com.hotpads.joblet.dto;

import java.util.List;

import com.hotpads.joblet.JobletService.JobletServiceThreadCountResponse;
import com.hotpads.webappinstance.databean.WebAppInstance;

public class JobletHandlerThreadCountDto{

	final String jobletType;
	final int clusterLimit;
	final double instanceAvg;
	final int instanceLimit;
	final int numExtraThreads;
	final int firstExtraInstanceIndex;
	final String firstExtraInstanceServerName;
	final boolean thisInstanceRunsExtraThread;


	public JobletHandlerThreadCountDto(List<WebAppInstance> instances,
			JobletServiceThreadCountResponse jobletServiceThreadCountResponse){
		this.jobletType = jobletServiceThreadCountResponse.jobletType.getPersistentString();
		this.clusterLimit = jobletServiceThreadCountResponse.clusterLimit;
		this.instanceAvg = clusterLimit / (double)instances.size();
		this.instanceLimit = jobletServiceThreadCountResponse.instanceLimit;
		this.numExtraThreads = jobletServiceThreadCountResponse.numExtraThreads;
		this.firstExtraInstanceIndex = jobletServiceThreadCountResponse.firstExtraInstanceIdxInclusive;
		this.firstExtraInstanceServerName = instances.get(firstExtraInstanceIndex).getKey().getServerName();
		this.thisInstanceRunsExtraThread = jobletServiceThreadCountResponse.runExtraThread;
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