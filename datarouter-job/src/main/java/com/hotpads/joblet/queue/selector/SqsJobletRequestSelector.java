package com.hotpads.joblet.queue.selector;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.execute.ParallelJobletProcessor;
import com.hotpads.joblet.queue.JobletRequestQueueKey;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.queue.JobletRequestSelector;
import com.hotpads.util.core.profile.PhaseTimer;

public class SqsJobletRequestSelector implements JobletRequestSelector{
	private static final Logger logger = LoggerFactory.getLogger(SqsJobletRequestSelector.class);

	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;

	@Override
	public Optional<JobletRequest> getJobletRequestForProcessing(PhaseTimer timer, JobletType<?> type,
			String reservedBy){
		for(JobletPriority priority : JobletPriority.values()){
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, priority);
			if(jobletRequestQueueManager.shouldSkipQueue(queueKey)){
				JobletCounters.incQueueSkip(queueKey.getQueueName());
				continue;
			}
			// set timeout to 0 so we return immediately. processor threads can do the waiting
			Config config = new Config().setTimeoutMs(0L)
					.setVisibilityTimeoutMs(ParallelJobletProcessor.RUNNING_JOBLET_TIMEOUT_MS);
			QueueMessage<JobletRequestKey,JobletRequest> message = jobletNodes.jobletRequestQueueByKey().get(queueKey)
					.peek(config);
			timer.add("peek");
			if(message == null){
				JobletCounters.incQueueMiss(queueKey.getQueueName());
				jobletRequestQueueManager.onJobletRequestQueueMiss(queueKey);
				continue;
			}
			JobletCounters.incQueueHit(queueKey.getQueueName());
			JobletRequest jobletRequest = message.getDatabean();
			boolean existsInDb = jobletNodes.jobletRequest().exists(jobletRequest.getKey(), null);
			timer.add("check exists");
			if(!existsInDb){
				logger.warn("draining non-existent JobletRequest without processing: {}", jobletRequest);
				JobletCounters.ignoredRequestMissingFromDb(type);
				jobletNodes.jobletRequestQueueByKey().get(queueKey).ack(message.getKey(), null);
				timer.add("ack missing request");
				continue;
			}
			jobletRequest.setQueueMessageKey(message.getKey());
			jobletRequest.setReservedBy(reservedBy);
			jobletRequest.setReservedAt(System.currentTimeMillis());
			jobletRequest.setStatus(JobletStatus.running);
			jobletNodes.jobletRequest().put(jobletRequest, null);
			if(!jobletRequest.getRestartable()){//don't let SQS give this joblet out again
				jobletNodes.jobletRequestQueueByKey().get(queueKey).ack(message.getKey(), null);
				timer.add("ack non-restartable");
			}
			return Optional.of(jobletRequest);
		}
		return Optional.empty();
	}
}
