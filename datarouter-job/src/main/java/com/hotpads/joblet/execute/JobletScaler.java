package com.hotpads.joblet.execute;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.config.Configs;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletSettings;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletScaler {

	private static final long BACKUP_PERIOD_MS = TimeUnit.MINUTES.toMillis(5);

	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;

	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.created);


	/******************* fields ***********************/

	//injected
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletSettings jobletSettings;
	private final JobletNodes jobletNodes;


	/******************** construct ********************/

	@Inject
	private JobletScaler(JobletTypeFactory jobletTypeFactory, JobletSettings jobletSettings,
			JobletNodes jobletNodes){
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletSettings = jobletSettings;
		this.jobletNodes = jobletNodes;
	}


	/****************** methods *************************/

	public int getNumJobletServers(){
		Iterable<JobletRequest> joblets = jobletNodes.joblet().scan(null, Configs.slaveOk());
		return calcNumJobletServers(joblets);
	}


	private int calcNumJobletServers(Iterable<JobletRequest> joblets){
		int minServers = jobletSettings.getMinJobletServers().getValue();
		int maxServers = jobletSettings.getMaxJobletServers().getValue();
		JobletRequest oldestJoblet = JobletRequest.getOldestForTypesAndStatuses(jobletTypeFactory, joblets, jobletTypeFactory
				.getTypesCausingScaling(), STATUSES_TO_CONSIDER);
		if(oldestJoblet == null){
			return minServers;
		}
		long maxAgeMs = System.currentTimeMillis() - oldestJoblet.getKey().getCreated();
		return getNumServersForQueueAge(minServers, maxServers, maxAgeMs);
	}


	private static int getNumServersForQueueAge(int minServers, int maxServers, long ageMs){
		int numPeriodsPending = (int)(ageMs / BACKUP_PERIOD_MS);
		int targetNumServers = minServers + NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD * numPeriodsPending;
		int cappedTargetNumServers = Math.min(targetNumServers, maxServers);
		return cappedTargetNumServers;
	}


	/*********************** test *****************************/

	public static class JobletScalerTests{
		@Test
		public void testGetNumServersForQueueAge(){
			int minServers = 3;
			int maxServers = 11;
			Assert.assertEquals(3, getNumServersForQueueAge(minServers, maxServers, 0));
			Assert.assertEquals(3, getNumServersForQueueAge(minServers, maxServers, 3000));
			Assert.assertEquals(5, getNumServersForQueueAge(minServers, maxServers, TimeUnit.MINUTES.toMillis(6)));
			Assert.assertEquals(11, getNumServersForQueueAge(minServers, maxServers, TimeUnit.HOURS.toMillis(2)));
		}
	}

}
