package com.hotpads.joblet;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.joblet.JobletExecutorThread.JobletExecutorThreadFactory;
import com.hotpads.joblet.JobletExecutorThreadPool.JobletExecutorThreadPoolFactory;
import com.hotpads.joblet.databean.Joblet;

/**
 * Extend and bind your own JobletTypeFactory
 */
public abstract class BaseJobletConstructorIntegrationTests{
	@Inject
	private JobletExecutorThreadFactory jobletExecutorThreadFactory;
	@Inject
	private JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory;
	@Inject
	private JobletTypeFactory jobletTypeFactory;

	@Test
	public void testJobletConstructors(){
		JobletType<?> testJobletType = jobletTypeFactory.getSampleType();
		ThreadGroup testThreadGroup = new ThreadGroup(testJobletType.getPersistentString());
		JobletExecutorThreadPool pool = jobletExecutorThreadPoolFactory.create(0, testJobletType);
		JobletExecutorThread thread = jobletExecutorThreadFactory.create(pool, testThreadGroup);
		for(JobletType<?> jobletType : jobletTypeFactory.getAllTypes()){
			Joblet joblet = new Joblet(jobletType, 0, 0, false);
			JobletPackage jobletPackage = new JobletPackage(joblet, null);
			JobletProcess jobletProcess = thread.createUninitializedJobletProcessFromJoblet(jobletPackage);
			Assert.assertEquals(jobletType, jobletTypeFactory.fromJobletProcess(jobletProcess));
		}
	}

}