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

	public <P> Joblet<?> createForPackage(JobletPackage jobletPackage){
		@SuppressWarnings("unchecked")
		JobletType<P> jobletType = (JobletType<P>)jobletTypeFactory.fromJobletRequest(jobletPackage.getJoblet());
		JobletCodec<P> jobletCodec = injector.getInstance(jobletType.getCodecClass());
		P jobletParams = jobletCodec.unmarshallData(jobletPackage.getJobletData().getData());
		return create(jobletType, jobletPackage.getJoblet(), jobletParams);
	}

	private <P> Joblet<P> create(JobletType<P> jobletType, JobletRequest jobletRequest, P jobletParams){
		Joblet<P> joblet = injector.getInstance(jobletType.getAssociatedClass());
		joblet.setJoblet(jobletRequest);
		joblet.setJobletParams(jobletParams);
		return joblet;

	}
}
