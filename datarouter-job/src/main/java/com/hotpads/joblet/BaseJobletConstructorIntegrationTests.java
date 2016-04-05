package com.hotpads.joblet;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

/**
 * Extend and bind your own JobletTypeFactory
 */
public abstract class BaseJobletConstructorIntegrationTests{
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private JobletFactory jobletFactory;

	@Test
	public void testJobletConstructors(){
		for(JobletType<?> jobletType : jobletTypeFactory.getAllTypes()){
			JobletRequest joblet = new JobletRequest(jobletType, 0, 0, false);
			Joblet<?> jobletProcess = jobletFactory.create(joblet, new JobletData(null));
			Assert.assertEquals(jobletType, jobletTypeFactory.fromJoblet(jobletProcess));
		}
	}

}