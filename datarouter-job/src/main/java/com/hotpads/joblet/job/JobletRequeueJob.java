package com.hotpads.joblet.job;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;

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
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.ActiveJobletTypeFactory;
import com.hotpads.util.core.collections.Range;

/**
 * Find joblets that look to be stuck with status=created but apparently aren't in the queue, and requeue them.  If they
 * were already in the queue, then the extra message will be ignored when dequeued.
 */
public class JobletRequeueJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobletRequeueJob.class);

	private static final Duration
			MIDDLE_AGE = Duration.ofMinutes(30),
			OLD_AGE = Duration.ofHours(1);

	private static final Range<Duration> MIDDLE_AGE_RANGE = new Range<>(MIDDLE_AGE, OLD_AGE);

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
		List<JobletRequestKey> allPrefixes = JobletRequestKey.createPrefixesForTypesAndPriorities(
				activeJobletTypeFactory.getAllActiveTypes(), EnumSet.allOf(JobletPriority.class));
		allPrefixes.stream()
				.filter(this::anyToRequeueWithPrefix)
				.forEach(this::requeueOldJobletsForPrefix);
	}


	//return if there are any old joblets with a big gap after them
	private boolean anyToRequeueWithPrefix(JobletRequestKey prefix){
		boolean anyOld = false;
		for(JobletRequest request : jobletNodes.jobletRequest().scanWithPrefix(prefix, null)){
			if(request.getStatus() != JobletStatus.created){
				continue;
			}
			Duration age = request.getKey().getAge();
			if(age.compareTo(OLD_AGE) > 0){
				anyOld = true;
			}else if(MIDDLE_AGE_RANGE.contains(age)){
				return false;
			}else{//hit the young joblets without seeing any middle age
				return anyOld;
			}
		}
		return false;
	}

	private void requeueOldJobletsForPrefix(JobletRequestKey prefix){
		JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(prefix);
		for(JobletRequest request : jobletNodes.jobletRequest().scanWithPrefix(prefix, null)){
			if(request.getKey().getAge().compareTo(OLD_AGE) < 0){
				break;
			}
			jobletNodes.jobletRequestQueueByKey().get(queueKey).put(request, null);
			logger.warn("requeued one for {}-{}, {}", request.getTypeString(), request.getKey().getPriority(), request);
		}
	}

}
