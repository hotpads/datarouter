package com.hotpads.joblet.enums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;

public class JobletTypeFactory{

	private final List<JobletType<?>> allTypes;
	private final JobletType<?> sampleType;
	private final Map<String,JobletType<?>> typeByPersistentString;
	private final Set<JobletType<?>> typesCausingScaling;

	public JobletTypeFactory(Collection<JobletType<?>> types){
		this.allTypes = new ArrayList<>(types);
		this.sampleType = DrCollectionTool.getFirst(types);
		this.typeByPersistentString = types.stream()
				.collect(Collectors.toMap(JobletType::getPersistentString, Function.identity()));
		this.typesCausingScaling = types.stream()
				.filter(JobletType::causesScaling)
				.collect(Collectors.toSet());
	}


	/*------------------ get/set ---------------------*/

	public List<JobletType<?>> getAllTypes(){
		return allTypes;
	}

	public JobletType<?> getSampleType(){
		return sampleType;
	}

	public Set<JobletType<?>> getTypesCausingScaling(){
		return typesCausingScaling;
	}


	/*--------------------- parse persistent string ----------------*/

	public JobletType<?> fromJoblet(Joblet<?> joblet){
		return joblet == null ? null : fromJobletRequest(joblet.getJobletRequest());
	}

	public JobletType<?> fromJobletPackage(JobletPackage jobletPackage){
		return jobletPackage == null ? null : fromJobletRequest(jobletPackage.getJobletRequest());
	}

	public JobletType<?> fromJobletRequest(JobletRequest jobletRequest){
		return jobletRequest == null ? null : fromJobletKey(jobletRequest.getKey());
	}

	public JobletType<?> fromJobletKey(JobletRequestKey jobletKey){
		return jobletKey == null ? null : fromPersistentString(jobletKey.getType());
	}

	public JobletType<?> fromPersistentString(String string){
		return typeByPersistentString.get(string);
	}
}
