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
package io.datarouter.joblet.execute;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.dto.RunningJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;

@Singleton
public class JobletProcessors{

	private final JobletProcessorFactory jobletProcessorFactory;
	private final JobletTypeFactory jobletTypeFactory;

	private final AtomicLong idGenerator;
	private Map<JobletType<?>,JobletProcessor> processorByType;

	@Inject
	public JobletProcessors(JobletProcessorFactory jobletProcessorV2Factory, JobletTypeFactory jobletTypeFactory){
		this.jobletProcessorFactory = jobletProcessorV2Factory;
		this.jobletTypeFactory = jobletTypeFactory;
		this.idGenerator = new AtomicLong(0);
	}


	public void createAndStartProcessors(){
		this.processorByType = jobletTypeFactory.getAllTypes().stream()
				.map(jobletType -> jobletProcessorFactory.create(idGenerator, jobletType))
				.collect(Collectors.toMap(JobletProcessor::getJobletType, Function.identity()));
	}

	public void requestShutdown(){
		processorByType.values().forEach(JobletProcessor::requestShutdown);
	}

	public List<RunningJoblet> getRunningJoblets(){
		return processorByType.values().stream()
				.map(JobletProcessor::getRunningJoblets)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public Optional<String> killThread(long threadId){
		return processorByType.values().stream()
				.filter(processor -> processor.killThread(threadId))
				.findAny()
				.map(JobletProcessor::getJobletType)
				.map(io.datarouter.joblet.type.JobletType::getPersistentString);
	}

	public String getRunningJoblet(long threadId){
		return processorByType.values().stream()
				.map(JobletProcessor::getRunningJoblets)
				.flatMap(Collection::stream)
				.filter(joblet -> joblet.getId().equals(Long.toString(threadId)))
				.map(RunningJoblet::getJobletData)
				.collect(Collectors.joining(", "));
	}

}
