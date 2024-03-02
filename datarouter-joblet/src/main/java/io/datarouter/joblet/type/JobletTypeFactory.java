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
package io.datarouter.joblet.type;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.joblet.model.Joblet;
import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobletTypeFactory{

	private final List<JobletType<?>> allTypes;
	private final JobletType<?> sampleType;
	private final Map<String,JobletType<?>> typeByPersistentString;
	private final Map<String,JobletType<?>> typeByShortQueueName;
	private final Set<JobletType<?>> typesCausingScaling;

	@Inject
	public JobletTypeFactory(JobletTypeRegistry registry){
		List<JobletType<?>> types = registry.get();

		this.allTypes = types.stream()
				.sorted(Comparator.comparing(JobletType::getPersistentString))
				.toList();
		this.sampleType = types.getFirst();
		this.typeByPersistentString = new HashMap<>();
		this.typeByShortQueueName = new HashMap<>();
		for(JobletType<?> type : types){
			// error on duplicate persistentStrings
			String persistentString = type.getPersistentString();
			if(typeByPersistentString.containsKey(persistentString)){
				throw new RuntimeException(type.getAssociatedClass() + " duplicates persistentString["
						+ persistentString + "] of " + typeByPersistentString.get(persistentString)
								.getAssociatedClass());
			}
			typeByPersistentString.put(type.getPersistentString(), type);

			// error on duplicate shortQueueNames
			String shortQueueName = type.getShortQueueName();
			if(typeByShortQueueName.containsKey(shortQueueName)){
				throw new RuntimeException(type.getAssociatedClass() + " duplicates shortQueueName["
						+ shortQueueName + "] of " + typeByShortQueueName.get(shortQueueName)
								.getAssociatedClass());
			}
			typeByShortQueueName.put(type.getShortQueueName(), type);
		}
		this.typesCausingScaling = types.stream()
				.filter(JobletType::causesScaling)
				.collect(Collectors.toSet());
	}

	public boolean isRecognizedType(JobletRequest request){
		return fromJobletRequest(request) != null;
	}

	/*------------------ get/set ---------------------*/

	public List<JobletType<?>> getAllTypes(){
		return allTypes;
	}

	public JobletType<?> getSampleType(){
		return sampleType;
	}

	public Set<JobletType<?>> getTypesCausingScaling(){
		return typesCausingScaling;
	}

	/*--------------------- parse persistent string ----------------*/

	public JobletType<?> fromJoblet(Joblet<?> joblet){
		return joblet == null ? null : fromJobletRequest(joblet.getJobletRequest());
	}

	public JobletType<?> fromJobletPackage(JobletPackage jobletPackage){
		return jobletPackage == null ? null : fromJobletRequest(jobletPackage.getJobletRequest());
	}

	public JobletType<?> fromJobletRequest(JobletRequest jobletRequest){
		return jobletRequest == null ? null : fromJobletRequestKey(jobletRequest.getKey());
	}

	public JobletType<?> fromJobletRequestKey(JobletRequestKey jobletKey){
		return jobletKey == null ? null : fromPersistentString(jobletKey.getType());
	}

	public JobletType<?> fromPersistentString(String string){
		return typeByPersistentString.get(string);
	}

}
