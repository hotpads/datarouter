package com.hotpads.joblet;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.config.Configs;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.ActiveJobletTypeFactory;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.setting.JobletSettings;

@Singleton
public class JobletScaler{
	private static final Logger logger = LoggerFactory.getLogger(JobletScaler.class);

	public static final Duration BACKUP_PERIOD = Duration.ofMinutes(5);
	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;
	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.created);

	private final ActiveJobletTypeFactory activeJobletTypeFactory;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletSettings jobletSettings;
	private final JobletNodes jobletNodes;

	@Inject
	private JobletScaler(ActiveJobletTypeFactory activeJobletTypeFactory, JobletTypeFactory jobletTypeFactory,
			JobletSettings jobletSettings, JobletNodes jobletNodes){
		this.activeJobletTypeFactory = activeJobletTypeFactory;
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
		int minServers = jobletSettings.minJobletServers.getValue();
		int maxServers = jobletSettings.maxJobletServers.getValue();
		JobletRequest oldestJobletRequest = JobletRequest.getOldestForTypesAndStatuses(jobletTypeFactory,
				jobletRequests, activeJobletTypeFactory.getActiveTypesCausingScaling(), STATUSES_TO_CONSIDER);
		if(oldestJobletRequest == null){
			return minServers;
		}
		Duration maxAge = Duration.ofMillis(System.currentTimeMillis() - oldestJobletRequest.getKey().getCreated());
		int targetServers = getTargetServersForQueueAge(minServers, maxServers, maxAge);
		if(targetServers > minServers){
			logger.warn("targetServers at {} because of {} with age {}m", targetServers, jobletTypeFactory
					.fromJobletRequest(oldestJobletRequest), maxAge.toMinutes());
		}
		return targetServers;
	}

	private static int getTargetServersForQueueAge(int minServers, int maxServers, Duration age){
		int numPeriodsPending = (int)(age.toMillis() / BACKUP_PERIOD.toMillis());
		int targetServers = minServers + NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD * numPeriodsPending;
		int cappedTargetServers = Math.min(targetServers, maxServers);
		return cappedTargetServers;
	}


	/*--------------------- test -------------------------*/

	public static class JobletScalerTests{
		@Test
		public void testGetNumServersForQueueAge(){
			int minServers = 3;
			int maxServers = 11;
			Assert.assertEquals(3, getTargetServersForQueueAge(minServers, maxServers, Duration.ofMillis(0)));
			Assert.assertEquals(3, getTargetServersForQueueAge(minServers, maxServers, Duration.ofMillis(3000)));
			Assert.assertEquals(5, getTargetServersForQueueAge(minServers, maxServers, Duration.ofMinutes(6)));
			Assert.assertEquals(11, getTargetServersForQueueAge(minServers, maxServers, Duration.ofHours(2)));
		}
	}

}
