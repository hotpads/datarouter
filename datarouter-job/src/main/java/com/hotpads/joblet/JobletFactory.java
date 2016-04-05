package com.hotpads.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private JobletTypeFactory jobletTypeFactory;


	@SuppressWarnings("unchecked")//can't seem to remove these casts while HotPadsJobletType is an enum
	public <T> Joblet<T> create(JobletRequest jobletRequest, JobletData jobletData){
		JobletType<T> jobletType = (JobletType<T>)jobletTypeFactory.fromJobletRequest(jobletRequest);
		Joblet<T> joblet = (Joblet<T>)injector.getInstance(jobletType.getAssociatedClass());
		joblet.setJoblet(jobletRequest);
		joblet.setJobletData(jobletData);
		T params = joblet.unmarshallData(jobletData.getData());
		joblet.setJobletParams(params);
		return joblet;
	}
}
