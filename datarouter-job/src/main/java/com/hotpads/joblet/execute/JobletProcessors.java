package com.hotpads.joblet.execute;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.dto.JobletTypeSummary;
import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.type.JobletType;
import com.hotpads.joblet.type.JobletTypeFactory;
import com.hotpads.util.core.stream.StreamTool;

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

	public Map<JobletType<?>,List<RunningJoblet>> getRunningJobletsByType(){
		return processorByType.values().stream()
				.map(proc -> new AbstractMap.SimpleEntry<>(proc.getJobletType(), proc.getRunningJoblets()))
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(Collectors.toMap(SimpleEntry::getKey, entry -> StreamTool.map(entry.getValue(), RunningJoblet
						::withoutData)));
	}

	public List<JobletTypeSummary> getTypeSummaries(){
		return processorByType.values().stream()
				.map(JobletTypeSummary::new)
				.filter(summary -> summary.getNumRunning() > 0)
				.sorted(Comparator.comparing(JobletTypeSummary::getJobletType))
				.collect(Collectors.toList());
	}

	public void killThread(long threadId){
		processorByType.values().forEach(processor -> processor.killThread(threadId));
	}

	public String getRunningJoblet(long threadId){
		return processorByType.values().stream()
				.map(JobletProcessor::getRunningJoblets)
				.flatMap(Collection::stream)
				.filter(joblet -> joblet.getId().equals(((Long)threadId).toString()))
				.map(RunningJoblet::getJobletData)
				.collect(Collectors.joining(", "));
	}
}
