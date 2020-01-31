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
package io.datarouter.clustersetting.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClusterSettingSearchService{

	@Inject
	private CachedClusterSettingNames cachedClusterSettingNames;

	public List<SettingNameMatchResult> searchSettingNames(String query, int limit){
		return cachedClusterSettingNames.get().stream()
				.map(name -> getMatchResultFromNameAndQuery(name, query))
				.sorted(Comparator.comparingInt(SettingNameMatchResult::getMatchPercentage).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	private SettingNameMatchResult getMatchResultFromNameAndQuery(String name, String query){
		String lowerName = name.toLowerCase();
		String lowerQuery = query.toLowerCase();
		int matchWeight = 0;
		int queryIndex = 0;
		int incrementAmount = 1;
		for(char c : lowerQuery.toCharArray()){
			int indexOfChar = lowerName.indexOf(c, queryIndex);
			if(indexOfChar != -1){
				if(indexOfChar == queryIndex){
					incrementAmount++;
				}else{
					incrementAmount = 1;
				}
				matchWeight += incrementAmount;
				queryIndex = indexOfChar + 1;
			}
		}
		int normalizedMatchWeight = (matchWeight * 100) / query.length();
		return new SettingNameMatchResult(name, normalizedMatchWeight);
	}

	public static class SettingNameMatchResult{

		private final String name;
		private final int matchWeight;

		public SettingNameMatchResult(String name, int matchPercentage){
			this.name = name;
			this.matchWeight = matchPercentage;
		}

		public String getName(){
			return name;
		}

		public int getMatchPercentage(){
			return matchWeight;
		}

	}

}
