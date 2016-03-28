package com.hotpads.joblet;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.joblet.databean.Joblet;
import com.hotpads.joblet.databean.JobletKey;

public class JobletTypeFactory{

	private final JobletType<? extends JobletType<?>> sampleJobletType;
	private final Set<JobletType<?>> typesCausingScaling;

	public JobletTypeFactory(JobletType<? extends JobletType<?>>[] types){
		this.sampleJobletType = DrArrayTool.getFirst(types);
		this.typesCausingScaling = Arrays.stream(types)
				.filter(JobletType::causesScaling)
				.collect(Collectors.toSet());
	}

	/*------------------ methods ---------------------*/

	public Set<JobletType<?>> getTypesCausingScaling(){
		return typesCausingScaling;
	}

	public JobletType<?> fromJobletProcess(JobletProcess jobletProcess){
		return jobletProcess == null ? null : fromJoblet(jobletProcess.getJoblet());
	}

	public JobletType<?> fromJobletPackage(JobletPackage jobletPackage){
		return jobletPackage == null ? null : fromJoblet(jobletPackage.getJoblet());
	}

	public JobletType<?> fromJoblet(Joblet joblet){
		return joblet == null ? null : fromJobletKey(joblet.getKey());
	}

	public JobletType<?> fromJobletKey(JobletKey jobletKey){
		return jobletKey == null ? null : fromPersistentString(jobletKey.getType());
	}

	public JobletType<?> fromPersistentString(String string){
		return sampleJobletType.fromPersistentString(string);
	}
}
