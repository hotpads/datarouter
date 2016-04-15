package com.hotpads.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private JobletTypeFactory jobletTypeFactory;

	@SuppressWarnings("unchecked")
	public <P> Joblet<?> createForPackage(JobletPackage jobletPackage){
		JobletType<P> jobletType = (JobletType<P>)jobletTypeFactory.fromJobletRequest(jobletPackage.getJoblet());
		Joblet<P> jobletCodec = injector.getInstance(jobletType.getAssociatedClass());
		P jobletParams = jobletCodec.unmarshallData(jobletPackage.getJobletData().getData());
		return createForRequestAndParams(jobletPackage.getJoblet(), jobletParams);
	}

	@SuppressWarnings("unchecked")//can't seem to remove the cast while HotPadsJobletType is an enum
	public <P> Joblet<P> createForRequestAndParams(JobletRequest jobletRequest, P jobletParams){
		JobletType<?> jobletType = jobletTypeFactory.fromJobletRequest(jobletRequest);
		Joblet<P> joblet = (Joblet<P>)injector.getInstance(jobletType.getAssociatedClass());
		joblet.setJoblet(jobletRequest);
		joblet.setJobletParams(jobletParams);
		return joblet;
	}
}
