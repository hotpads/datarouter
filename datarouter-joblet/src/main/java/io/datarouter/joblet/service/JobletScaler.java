/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.service;

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

import io.datarouter.clustersetting.service.ClusterSettingService;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.ActiveJobletTypeFactory;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.model.databean.Databean;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

/**
 * JobletScaler advises the cluster manager how many joblet servers it should be running.  It considers:
 *   - a subset of JobletTypes, where causesScaling=true
 *   - the time age of joblets of those types
 *   - the minServers limit: absolute minimum servers
 *   - the maxServers limit: absolute maximum servers
 *   - the "cluster thread count limit": maximum concurrent joblets of one type that should run across the cluster
 *   - the "instance thread count limit": maximum concurrent joblets of one type that should run on a single instance
 *
 * As an example, let's say we have MyJoblet with:
 *   - causesScaling=true
 *   - minServers=2
 *   - maxServers=10
 *   - clusterLimit=12
 *   - instanceLimit=3
 *
 * If there are no MyJoblets in the queue, we'll run the minimum of 2 servers, but as the queue grows, we will want
 * more threads.  When getTargetServersForQueueAge(..) requests 8 threads, the scaler will request 8 / 3 is 3 servers.
 * When it requests 15 threads, we cap that at clusterLimit 12, divide by instanceLimit 3, and request 4 servers.
 *
 * To force your cluster to grow:
 *   - raise the clusterLimit
 *   - lower the instanceLimit
 *   - raise the maxServers higher than (clusterLimit / instanceLimit)
 */
@Singleton
public class JobletScaler{
	private static final Logger logger = LoggerFactory.getLogger(JobletScaler.class);

	public static final Duration BACKUP_PERIOD = Duration.ofMinutes(5);
	private static final int NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD = 2;
	private static final Set<JobletStatus> STATUSES_TO_CONSIDER = EnumSet.of(JobletStatus.CREATED);
	private static final int IGNORE_OLDEST_N_JOBLETS = 100;//in case they are stuck

	@Inject
	private ClusterSettingService clusterSettingService;
	@Inject
	private ActiveJobletTypeFactory activeJobletTypeFactory;
	@Inject
	private DatarouterJobletSettingRoot jobletSettings;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;

	public int getNumJobletServers(WebappInstance jobletInstance){
		int minServers = jobletSettings.minJobletServers.get();
		if(jobletInstance == null){
			return minServers;
		}

		//find JobletRequests that would trigger scaling
		Map<JobletType<?>,Duration> oldestAgeByJobletType = new TreeMap<>();
		for(JobletType<?> jobletType : activeJobletTypeFactory.getActiveTypesCausingScaling()){
			JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
			jobletRequestDao.scanWithPrefix(prefix)
					.include(request -> STATUSES_TO_CONSIDER.contains(request.getStatus()))
					.map(Databean::getKey)
					.map(JobletRequestKey::getAge)
					.sort(Comparator.reverseOrder())
					.skip(IGNORE_OLDEST_N_JOBLETS)
					.findFirst()
					.ifPresent(age -> oldestAgeByJobletType.put(jobletType, age));
		}

		//calculate desired numServers by type
		int maxServers = jobletSettings.maxJobletServers.get();
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
						NumberFormatter.format(maxNeededForJobletTypeDbl, 2));
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

	private int getClusterLimit(WebappInstance instance, JobletType<?> jobletType){
		CachedSetting<Integer> clusterLimitSetting = jobletSettings.getClusterThreadCountSettings()
				.getSettingForJobletType(jobletType);
		return clusterSettingService.getSettingValueForWebappInstance(clusterLimitSetting, instance);
	}

	private int getInstanceLimit(WebappInstance instance, JobletType<?> jobletType){
		CachedSetting<Integer> instanceLimitSetting = jobletSettings.getThreadCountSettings().getSettingForJobletType(
				jobletType);
		return clusterSettingService.getSettingValueForWebappInstance(instanceLimitSetting, instance);
	}

	public static int getTargetServersForQueueAge(int minServers, Duration age){
		int numPeriodsPending = (int)(age.toMillis() / BACKUP_PERIOD.toMillis());
		return minServers + NUM_EXTRA_SERVERS_PER_BACKUP_PERIOD * numPeriodsPending;
	}

}
