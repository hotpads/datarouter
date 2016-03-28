package com.hotpads.joblet;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.joblet.JobletExecutorThread.JobletExecutorThreadFactory;
import com.hotpads.joblet.JobletExecutorThreadPool.JobletExecutorThreadPoolFactory;
import com.hotpads.joblet.databean.Joblet;

@Guice(moduleFactory=ServicesModuleFactory.class)
public class JobletExecutorThreadIntegrationTests{
	@Inject
	private JobletExecutorThreadFactory jobletExecutorThreadFactory;
	@Inject
	private JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory;
	@Inject
	private JobletTypeFactory jobletTypeFactory;

	@Test
	public void testJobletConstructors(){
		HotPadsJobletType testJobletType = HotPadsJobletType.AreaBorderRendering;
		ThreadGroup testThreadGroup = new ThreadGroup(testJobletType.getPersistentString());
		JobletExecutorThreadPool pool = jobletExecutorThreadPoolFactory.create(0, testJobletType);
		JobletExecutorThread thread = jobletExecutorThreadFactory.create(pool, testThreadGroup);
		for(HotPadsJobletType jobletType : HotPadsJobletType.values()){
			Joblet joblet = new Joblet(jobletType, 0, 0, false);
			JobletPackage jobletPackage = new JobletPackage(joblet, null);
			JobletProcess jobletProcess = thread.createUninitializedJobletProcessFromJoblet(jobletPackage);
			Assert.assertEquals(jobletType, jobletTypeFactory.fromJobletProcess(jobletProcess));
		}
	}

}