/*
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
package io.datarouter.webappinstance.service;

import java.util.List;
import java.util.stream.IntStream;

import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.HashMethods;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ClusterThreadCountService{

	@Inject
	private ServerName serverName;
	@Inject
	private CachedWebappInstancesOfThisServerType cachedWebAppInstancesOfThisServerType;

	public InstanceThreadCounts getThreadCountInfoForThisInstance(
			String resourcePersistentString,
			int clusterThreadLimit,
			int instanceThreadLimit){
		//get cached inputs
		List<String> serverNames = cachedWebAppInstancesOfThisServerType.getSortedServerNamesForThisWebApp();
		//calculate intermediate things
		int numInstances = serverNames.size();
		if(numInstances == 0){
			return new InstanceThreadCounts(clusterThreadLimit, instanceThreadLimit, 0, 0, 0, false, 0);
		}
		int minThreadsPerInstance = clusterThreadLimit / numInstances;//round down
		int numExtraThreads = clusterThreadLimit % numInstances;
		long resourceHash = HashMethods.longDjbHash(resourcePersistentString);
		double hashFractionOfOne = (double)resourceHash / (double)Long.MAX_VALUE;
		int firstExtraInstanceIdx = (int)Math.floor(hashFractionOfOne * numInstances);
		//calculate effective limit
		int effectiveLimit = minThreadsPerInstance;
		boolean runExtraThread = false;
		if(minThreadsPerInstance >= instanceThreadLimit){
			effectiveLimit = instanceThreadLimit;
		}else{
			runExtraThread = IntStream.range(0, numExtraThreads)
					.mapToObj(threadIdx -> (firstExtraInstanceIdx + threadIdx) % numInstances)
					.map(serverNames::get)
					.anyMatch(serverName.get()::equals);
			if(runExtraThread){
				++effectiveLimit;
			}
		}
		return new InstanceThreadCounts(
				clusterThreadLimit,
				instanceThreadLimit,
				minThreadsPerInstance,
				numExtraThreads,
				firstExtraInstanceIdx,
				runExtraThread,
				effectiveLimit);
	}

	public record InstanceThreadCounts(
			int clusterLimit,
			int instanceLimit,
			int minThreadsPerInstance,
			int numExtraThreads,
			int firstExtraInstanceIdxInclusive,
			boolean runExtraThread,
			int effectiveLimit){
	}

}
