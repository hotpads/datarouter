package com.hotpads.joblet.job;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletScaler;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.ActiveJobletTypeFactory;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.queue.JobletRequestQueueKey;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.util.core.iterable.BatchingIterable;


public class JobletRequeueJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobletRequeueJob.class);

	private final JobletSettings jobletSettings;
	private final ActiveJobletTypeFactory activeJobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final JobletRequestQueueManager jobletRequestQueueManager;

	@Inject
	public JobletRequeueJob(JobEnvironment jobEnvironment, JobletSettings jobletSettings,
			ActiveJobletTypeFactory activeJobletTypeFactory, JobletNodes jobletNodes,
			JobletRequestQueueManager jobletRequestQueueManager){
		super(jobEnvironment);
		this.jobletSettings = jobletSettings;
		this.activeJobletTypeFactory = activeJobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
	}


	@Override
	public boolean shouldRun(){
		return jobletSettings.runJobletRequeueJob.getValue();
	}

	@Override
	public void run(){
		long createdBeforeMs = System.currentTimeMillis() - JobletScaler.BACKUP_PERIOD.toMillis();
		final Predicate<JobletRequestKey> anyNewRequests = prefix -> {
			return jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
					.filter(request -> request.getStatus() == JobletStatus.created)
					.filter(request -> request.getKey().getCreated() >= createdBeforeMs)
					.findAny()
					.isPresent();
		};

		List<JobletRequestKey> prefixes = JobletRequestKey.createPrefixesForTypesAndPriorities(activeJobletTypeFactory
				.getActiveTypes(), JobletPriority.valuesList());
		prefixes.stream()
				.filter(anyNewRequests.negate())//only consider requests in queues that aren't congested
				.forEach(prefix -> {
						Iterable<JobletRequest> possiblyStuckRequests = jobletNodes.jobletRequest().streamWithPrefix(
								prefix, null)
								.filter(request -> request.getStatus() == JobletStatus.created)
								.filter(request -> request.getKey().getCreated() < createdBeforeMs)::iterator;
						JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(prefix);
						for(List<JobletRequest> possiblyStuckBatch : new BatchingIterable<>(possiblyStuckRequests,
								100)){
							jobletNodes.jobletRequestQueueByKey().get(queueKey).putMulti(possiblyStuckBatch, null);
							logger.warn("requeued {} of {}", possiblyStuckBatch.size(), prefix);
						}
				});
	}

}
