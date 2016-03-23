package com.hotpads.job.joblet;

import javax.inject.Singleton;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletKey;
import com.hotpads.config.job.enums.JobletType;
import com.hotpads.job.joblets.JobletPackage;

@Singleton
public class JobletTypeFactory<T extends JobletType<T>>{

	private final T sampleJobletType;

	public JobletTypeFactory(T sampleJobletType){
		this.sampleJobletType = sampleJobletType;
	}

	/*------------------ methods ---------------------*/

	public T fromJobletProcess(JobletProcess jobletProcess){
		return jobletProcess == null ? null : fromJoblet(jobletProcess.getJoblet());
	}

	public T fromJobletPackage(JobletPackage jobletPackage){
		return jobletPackage == null ? null : fromJoblet(jobletPackage.getJoblet());
	}

	public T fromJoblet(Joblet joblet){
		return joblet == null ? null : fromJobletKey(joblet.getKey());
	}

	public T fromJobletKey(JobletKey jobletKey){
		return jobletKey == null ? null : fromPersistentString(jobletKey.getType());
	}

	public T fromPersistentString(String string){
		return sampleJobletType.fromPersistentString(string);
	}
}
