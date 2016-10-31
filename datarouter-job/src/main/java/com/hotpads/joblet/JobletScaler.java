package com.hotpads.joblet;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.config.Configs;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.setting.JobletSettings;

@Singleton
public class JobletScaler {

	private static final long BACKUP_PERIOD_MS = Duration.ofMinutes(5).toMillis();
	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;
	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.created);

	private final JobletTypeFactory jobletTypeFactory;
	private final JobletSettings jobletSettings;
	private final JobletNodes jobletNodes;

	@Inject
	private JobletScaler(JobletTypeFactory jobletTypeFactory, JobletSettings jobletSettings, JobletNodes jobletNodes){
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletSettings = jobletSettings;
		this.jobletNodes = jobletNodes;
	}

	public int getNumJobletServers(){
		Iterable<JobletRequest> joblets = jobletNodes.jobletRequest().scan(null, Configs.slaveOk());
		return calcNumJobletServers(joblets);
	}

	/*--------------- private -----------------*/

	private int calcNumJobletServers(Iterable<JobletRequest> jobletRequests){
		int minServers = jobletSettings.getMinJobletServers().getValue();
		int maxServers = jobletSettings.getMaxJobletServers().getValue();
		JobletRequest oldestJobletRequest = JobletRequest.getOldestForTypesAndStatuses(jobletTypeFactory,
				jobletRequests, jobletTypeFactory.getTypesCausingScaling(), STATUSES_TO_CONSIDER);
		if(oldestJobletRequest == null){
			return minServers;
		}
		long maxAgeMs = System.currentTimeMillis() - oldestJobletRequest.getKey().getCreated();
		return getNumServersForQueueAge(minServers, maxServers, maxAgeMs);
	}

	private static int getNumServersForQueueAge(int minServers, int maxServers, long ageMs){
		int numPeriodsPending = (int)(ageMs / BACKUP_PERIOD_MS);
		int targetNumServers = minServers + NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD * numPeriodsPending;
		int cappedTargetNumServers = Math.min(targetNumServers, maxServers);
		return cappedTargetNumServers;
	}


	/*--------------------- test -------------------------*/

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
