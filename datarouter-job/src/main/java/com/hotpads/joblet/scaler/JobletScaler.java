package com.hotpads.joblet.scaler;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.ActiveJobletTypeFactory;
import com.hotpads.joblet.type.JobletType;

@Singleton
public class JobletScaler{
	private static final Logger logger = LoggerFactory.getLogger(JobletScaler.class);

	public static final Duration BACKUP_PERIOD = Duration.ofMinutes(5);
	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;
	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.created);
	private static final int IGNORE_OLDEST_N_JOBLETS = 100;//in case they are stuck

	private final ActiveJobletTypeFactory activeJobletTypeFactory;
	private final JobletSettings jobletSettings;
	private final JobletNodes jobletNodes;

	@Inject
	private JobletScaler(ActiveJobletTypeFactory activeJobletTypeFactory, JobletSettings jobletSettings,
			JobletNodes jobletNodes){
		this.activeJobletTypeFactory = activeJobletTypeFactory;
		this.jobletSettings = jobletSettings;
		this.jobletNodes = jobletNodes;
	}

	public int getNumJobletServers(){
		//find JobletRequests that would trigger scaling
		Map<JobletType<?>,JobletRequest> oldestJobletRequestByType = new TreeMap<>();
		for(JobletType<?> jobletType : activeJobletTypeFactory.getActiveTypesCausingScaling()){
			JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
			Optional<JobletRequest> optRequest = jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
					.filter(request -> STATUSES_TO_CONSIDER.contains(request.getStatus()))
					.skip(IGNORE_OLDEST_N_JOBLETS)
					.findFirst();
			if(optRequest.isPresent()){
				oldestJobletRequestByType.put(jobletType, optRequest.get());
			}
		}

		//calculate desired numServers by type
		int minServers = jobletSettings.minJobletServers.getValue();
		int maxServers = jobletSettings.maxJobletServers.getValue();
		Map<JobletType<?>,Integer> requestedNumServersByJobletType = new TreeMap<>();
		for(JobletType<?> jobletType : oldestJobletRequestByType.keySet()){
			Duration age = oldestJobletRequestByType.get(jobletType).getKey().getAge();
			int targetServers = getTargetServersForQueueAge(minServers, age);
			int maxNeededForJobletType = (int)Math.ceil(maxNeededForJobletType(jobletType));
			int withClusterThreadCap = Math.min(targetServers, maxNeededForJobletType);
			int withAbsoluteCap = Math.min(withClusterThreadCap, maxServers);
			if(withAbsoluteCap > minServers){
				requestedNumServersByJobletType.put(jobletType, withAbsoluteCap);
			}
		}

		//find highest request
		int targetServers = minServers;
		JobletType<?> highestType = null;
		for(JobletType<?> jobletType : requestedNumServersByJobletType.keySet()){
			int numForType = requestedNumServersByJobletType.get(jobletType);
			logger.warn("{} requesting {} servers", jobletType, numForType);
			if(numForType > targetServers){
				targetServers = numForType;
				highestType = jobletType;
			}
		}
		if(highestType != null){
			Duration hungriestTypeAge = oldestJobletRequestByType.get(highestType).getKey().getAge();
			logger.warn("targetServers at {} because of {} with age {}m", targetServers, highestType, hungriestTypeAge
					.toMinutes());
		}
		return targetServers;
	}

	private static int getTargetServersForQueueAge(int minServers, Duration age){
		int numPeriodsPending = (int)(age.toMillis() / BACKUP_PERIOD.toMillis());
		return minServers + NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD * numPeriodsPending;
	}

	private double maxNeededForJobletType(JobletType<?> jobletType){
		int clusterLimit = jobletSettings.getClusterThreadCountForJobletType(jobletType);
		int instanceLimit = jobletSettings.getThreadCountForJobletType(jobletType);
		return (double)clusterLimit / (double)instanceLimit;
	}


	/*--------------------- test -------------------------*/

	public static class JobletScalerTests{
		@Test
		public void testGetNumServersForQueueAge(){
			int minServers = 3;
			Assert.assertEquals(3, getTargetServersForQueueAge(minServers, Duration.ofMillis(0)));
			Assert.assertEquals(3, getTargetServersForQueueAge(minServers, Duration.ofMillis(3000)));
			Assert.assertEquals(5, getTargetServersForQueueAge(minServers, Duration.ofMinutes(6)));
		}
	}

}
