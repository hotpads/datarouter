package com.hotpads.joblet.job;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.queue.JobletRequestQueueKey;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.scaler.JobletScaler;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.ActiveJobletTypeFactory;
import com.hotpads.util.core.iterable.BatchingIterable;

/**
 * Find joblets that look to be stuck with status=created but apparently aren't in the queue, and requeue them.  If they
 * were in the queue, then the extra message will be ignored.
 */
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
		final Predicate<JobletRequestKey> anyNewRequestsPredicate = prefix -> {
			return jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
					.filter(request -> request.getStatus() == JobletStatus.created)
					.filter(request -> request.getKey().getCreated() >= createdBeforeMs)
					.findAny()
					.isPresent();
		};
		List<JobletRequestKey> allPrefixes = JobletRequestKey.createPrefixesForTypesAndPriorities(
				activeJobletTypeFactory.getAllActiveTypes(), EnumSet.allOf(JobletPriority.class));
		List<JobletRequestKey> prefixesWithoutNewRequests = allPrefixes.stream()
				.filter(anyNewRequestsPredicate.negate())
				.collect(Collectors.toList());
		for(JobletRequestKey prefix : prefixesWithoutNewRequests){
			Iterable<JobletRequest> possiblyStuckRequests = jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
					.filter(request -> request.getStatus() == JobletStatus.created)
					.filter(request -> request.getKey().getCreated() < createdBeforeMs)::iterator;
			JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(prefix);
			for(List<JobletRequest> possiblyStuckBatch : new BatchingIterable<>(possiblyStuckRequests, 100)){
				jobletNodes.jobletRequestQueueByKey().get(queueKey).putMulti(possiblyStuckBatch, null);
				logger.warn("requeued {} of {}", possiblyStuckBatch.size(), prefix);
			}
		}
	}

}
