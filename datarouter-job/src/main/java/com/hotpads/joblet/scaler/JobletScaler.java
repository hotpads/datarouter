package com.hotpads.joblet.scaler;

import java.time.Duration;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.clustersetting.ClusterSettingService;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.ActiveJobletTypeFactory;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.webappinstance.databean.WebAppInstance;

@Singleton
public class JobletScaler{
	private static final Logger logger = LoggerFactory.getLogger(JobletScaler.class);

	public static final Duration BACKUP_PERIOD = Duration.ofMinutes(5);
	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;
	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.created);
	private static final int IGNORE_OLDEST_N_JOBLETS = 100;//in case they are stuck

	private final ClusterSettingService clusterSettingService;
	private final ActiveJobletTypeFactory activeJobletTypeFactory;
	private final JobletSettings jobletSettings;
	private final JobletNodes jobletNodes;

	@Inject
	private JobletScaler(ClusterSettingService clusterSettingService, ActiveJobletTypeFactory activeJobletTypeFactory,
			JobletSettings jobletSettings, JobletNodes jobletNodes){
		this.clusterSettingService = clusterSettingService;
		this.activeJobletTypeFactory = activeJobletTypeFactory;
		this.jobletSettings = jobletSettings;
		this.jobletNodes = jobletNodes;
	}

	public int getNumJobletServers(WebAppInstance jobletInstance){
		int minServers = jobletSettings.minJobletServers.getValue();
		if(jobletInstance == null){
			return minServers;
		}

		//find JobletRequests that would trigger scaling
		Map<JobletType<?>,Duration> oldestAgeByJobletType = new TreeMap<>();
		for(JobletType<?> jobletType : activeJobletTypeFactory.getActiveTypesCausingScaling()){
			JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
			jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
					.filter(request -> STATUSES_TO_CONSIDER.contains(request.getStatus()))
					.map(Databean::getKey)
					.map(JobletRequestKey::getAge)
					.sorted(Comparator.reverseOrder())
					.skip(IGNORE_OLDEST_N_JOBLETS)
					.findFirst()
					.ifPresent(age -> oldestAgeByJobletType.put(jobletType, age));
		}

		//calculate desired numServers by type
		int maxServers = jobletSettings.maxJobletServers.getValue();
		Map<JobletType<?>,Integer> requestedNumServersByJobletType = new TreeMap<>();
		for(JobletType<?> jobletType : oldestAgeByJobletType.keySet()){
			Duration age = oldestAgeByJobletType.get(jobletType);
			int targetServers = getTargetServersForQueueAge(minServers, age);
			int clusterLimit = getClusterLimit(jobletInstance, jobletType);
			int instanceLimit = getInstanceLimit(jobletInstance, jobletType);
			double maxNeededForJobletTypeDbl = (double)clusterLimit / (double)instanceLimit;
			int maxNeededForJobletType = (int)Math.ceil(maxNeededForJobletTypeDbl);
			int withClusterThreadCap = Math.min(targetServers, maxNeededForJobletType);
			if(targetServers > minServers){
				logger.warn("{} with age {}m requesting {}, limiting to {} (clusterLimit={}, instanceLimit={} -> {})",
						jobletType, age.toMinutes(), targetServers, maxNeededForJobletType, clusterLimit, instanceLimit,
						DrNumberFormatter.format(maxNeededForJobletTypeDbl, 2));
			}
			if(withClusterThreadCap > minServers){
				requestedNumServersByJobletType.put(jobletType, withClusterThreadCap);
			}
		}

		//find highest request among jobletTypes
		int targetServers = minServers;
		JobletType<?> highestType = null;
		for(JobletType<?> jobletType : requestedNumServersByJobletType.keySet()){
			int numForType = requestedNumServersByJobletType.get(jobletType);
			if(numForType > targetServers){
				targetServers = numForType;
				highestType = jobletType;
			}
		}

		//cap at maxServers, log it, and return
		targetServers = Math.min(targetServers, maxServers);
		if(highestType == null){
			logger.warn("targetServers at minimum of {}", minServers);
		}else{
			Duration hungriestTypeAge = oldestAgeByJobletType.get(highestType);
			logger.warn("targetServers at {} of max {} because of {} with age {}m", targetServers, maxServers,
					highestType, hungriestTypeAge.toMinutes());
		}
		return targetServers;
	}

	private int getClusterLimit(WebAppInstance instance, JobletType<?> jobletType){
		Setting<Integer> clusterLimitSetting = jobletSettings.getClusterThreadCountSettings().getSettingForJobletType(
				jobletType);
		return clusterSettingService.getSettingValueForWebAppInstance(clusterLimitSetting, instance);
	}

	private int getInstanceLimit(WebAppInstance instance, JobletType<?> jobletType){
		Setting<Integer> instanceLimitSetting = jobletSettings.getThreadCountSettings().getSettingForJobletType(
				jobletType);
		return clusterSettingService.getSettingValueForWebAppInstance(instanceLimitSetting, instance);
	}

	private static int getTargetServersForQueueAge(int minServers, Duration age){
		int numPeriodsPending = (int)(age.toMillis() / BACKUP_PERIOD.toMillis());
		return minServers + NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD * numPeriodsPending;
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
