package com.hotpads.joblet.job;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.hotpads.datarouter.config.Config;
import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.setting.JobletSettings;

public class JobletCounterJob extends BaseJob{

	private final JobletSettings jobletSettings;
	private final JobletNodes jobletNodes;
	private final JobletCounters jobletCounters;

	@Inject
	public JobletCounterJob(JobEnvironment jobEnvironment, JobletSettings jobletSettings, JobletNodes jobletNodes,
			JobletCounters jobletCounters){
		super(jobEnvironment);
		this.jobletSettings = jobletSettings;
		this.jobletNodes = jobletNodes;
		this.jobletCounters = jobletCounters;
	}

	@Override
	public boolean shouldRun(){
		return jobletSettings.runJobletCounterJob.getValue();
	}

	@Override
	public void run(){
		Stream<JobletRequest> requests = jobletNodes.jobletRequest().stream(null, new Config().setSlaveOk(true));
		Collection<JobletSummary> summaries = JobletSummary.summarizeByTypeStatus(requests).values();
		List<JobletSummary> createdSummaries = JobletSummary.filterForStatus(summaries, JobletStatus.created);
		for(JobletSummary summary : createdSummaries){
			Duration age = Duration.ofMillis(new Date().getTime() - summary.getFirstCreated().getTime());
			jobletCounters.saveQueueLength(summary.getTypeString(), summary.getSumItems());
			jobletCounters.saveFirstCreated(summary.getTypeString(), age.toMinutes());
		}
	}

}
