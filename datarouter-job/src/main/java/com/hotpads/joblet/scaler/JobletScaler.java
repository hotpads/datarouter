package com.hotpads.joblet.scaler;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.ActiveJobletTypeFactory;
import com.hotpads.joblet.type.JobletTypeFactory;

@Singleton
public class JobletScaler{
	private static final Logger logger = LoggerFactory.getLogger(JobletScaler.class);

	public static final Duration BACKUP_PERIOD = Duration.ofMinutes(5);
	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;
	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.created);
	private static final int IGNORE_OLDEST_N_JOBLETS = 100;//in case they are stuck

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
		List<JobletRequestKey> prefixes = JobletRequestKey.createPrefixesForTypesAndPriorities(
				activeJobletTypeFactory.getActiveTypesCausingScaling(), EnumSet.allOf(JobletPriority.class));
		Duration maxAge = Duration.ZERO;
		Optional<JobletRequest> oldestJobletRequest = Optional.empty();
		for(JobletRequestKey prefix : prefixes){
			Optional<JobletRequest> oldestWithPrefix = jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
					.filter(request -> STATUSES_TO_CONSIDER.contains(request.getStatus()))
					.skip(IGNORE_OLDEST_N_JOBLETS)
					.findFirst();
			if(!oldestWithPrefix.isPresent()){
				continue;
			}
			Duration age = oldestWithPrefix.get().getKey().getAge();
			if(age.compareTo(maxAge) > 0){
				maxAge = age;
				oldestJobletRequest = oldestWithPrefix;
			}
		}
		int minServers = jobletSettings.minJobletServers.getValue();
		int maxServers = jobletSettings.maxJobletServers.getValue();
		if(!oldestJobletRequest.isPresent()){
			return minServers;
		}
		int targetServers = getTargetServersForQueueAge(minServers, maxServers, maxAge);
		if(targetServers > minServers){
			logger.warn("targetServers at {} because of {} with age {}m", targetServers, jobletTypeFactory
					.fromJobletRequest(oldestJobletRequest.get()), maxAge.toMinutes());
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
