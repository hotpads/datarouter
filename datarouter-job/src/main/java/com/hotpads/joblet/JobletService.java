package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.job.trigger.JobExceptionCategory;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletQueueMechanism;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.queue.JobletRequestQueueKey;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.queue.JobletRequestSelector;
import com.hotpads.joblet.queue.JobletRequestSelectorFactory;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.core.stream.StreamTool;

@Singleton
public class JobletService{
	private static final Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	private final JobletRequestQueueManager jobletRequestQueueManager;
	private final JobletNodes jobletNodes;
	private final ExceptionRecorder exceptionRecorder;
	private final JobletSettings jobletSettings;
	private final JobletRequestSelectorFactory jobletRequestSelectorFactory;

	@Inject
	public JobletService(JobletRequestQueueManager jobletRequestQueueManager, JobletNodes jobletNodes,
			ExceptionRecorder exceptionRecorder, JobletSettings jobletSettings,
			JobletRequestSelectorFactory jobletRequestSelectorFactory){
		this.jobletRequestQueueManager = jobletRequestQueueManager;
		this.jobletNodes = jobletNodes;
		this.exceptionRecorder = exceptionRecorder;
		this.jobletSettings = jobletSettings;
		this.jobletRequestSelectorFactory = jobletRequestSelectorFactory;
	}

	/*--------------------- create ------------------------*/

	public void submitJobletPackages(Collection<JobletPackage> jobletPackages){
		String typeString = DrCollectionTool.getFirst(jobletPackages).getJobletRequest().getTypeString();
		PhaseTimer timer = new PhaseTimer("insert " + jobletPackages.size() + " " + typeString);
		jobletNodes.jobletData().putMulti(JobletPackage.getJobletDatas(jobletPackages), Configs.insertOrBust());
		timer.add("inserted JobletData");
		jobletPackages.forEach(JobletPackage::updateJobletDataIdReference);
		List<JobletRequest> jobletRequests = JobletPackage.getJobletRequests(jobletPackages);
		jobletNodes.jobletRequest().putMulti(jobletRequests, Configs.insertOrBust());
		timer.add("inserted JobletRequest");
		if(jobletSettings.getQueueMechanismEnum() == JobletQueueMechanism.SQS){
			Map<JobletRequestQueueKey,List<JobletRequest>> requestsByQueueKey = jobletRequests.stream()
					.collect(Collectors.groupingBy(jobletRequestQueueManager::getQueueKey, Collectors.toList()));
			for(Map.Entry<JobletRequestQueueKey,List<JobletRequest>> queueAndRequests : requestsByQueueKey.entrySet()){
				jobletNodes.jobletRequestQueueByKey().get(queueAndRequests.getKey()).putMulti(queueAndRequests
						.getValue(), null);
			}
			timer.add("queued JobletRequests");
		}
		if(timer.getElapsedTimeBetweenFirstAndLastEvent() > 200){
			logger.warn("slow insert joblets:{}", timer);
		}
	}

	/*---------------------- read ---------------------------*/

	public List<JobletPackage> getJobletPackagesOfType(JobletType<?> jobletType){
		JobletRequestKey prefix = JobletRequestKey.create(jobletType, null, null, null);
		return jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
				.map(this::getJobletPackageForJobletRequest)
				.collect(Collectors.toList());
	}

	public JobletPackage getJobletPackageForJobletRequest(JobletRequest jobletRequest){
		JobletData jobletData = jobletNodes.jobletData().get(jobletRequest.getJobletDataKey(), null);
		return new JobletPackage(jobletRequest, jobletData);
	}

	public boolean jobletRequestExistsWithTypeAndStatus(JobletType<?> jobletType, JobletStatus jobletStatus){
		JobletRequestKey key = JobletRequestKey.create(jobletType, null, null, null);
		Range<JobletRequestKey> range = new Range<>(key, true, key, true);
		Config config = new Config().setIterateBatchSize(50);
		for(JobletRequest jobletRequest : jobletNodes.jobletRequest().scan(range, config)){
			if(jobletStatus == jobletRequest.getStatus()){
				return true;
			}
		}
		return false;
	}

	public JobletData getJobletDataForJobletRequest(JobletRequest joblet){
		return jobletNodes.jobletData().get(joblet.getJobletDataKey(), null);
	}

	/*--------------------- get for processing ---------------------*/

	public Optional<JobletRequest> getJobletRequestForProcessing(JobletType<?> type, String reservedBy){
		long startMs = System.currentTimeMillis();
		JobletRequestSelector selector = jobletRequestSelectorFactory.create();
		Optional<JobletRequest> jobletRequest = selector.getJobletRequestForProcessing(type, reservedBy);
		long durationMs = System.currentTimeMillis() - startMs;
		if(durationMs > 1000){
			String message = jobletRequest.map(Databean::getKey).map(Object::toString).orElse("none");
			logger.warn("slow get joblet type={}, durationMs={}, got {}", type, durationMs, message);
		}
		return jobletRequest;
	}

	/*------------------- update ----------------------------*/

	public void setJobletRequestsRunningOnServerToCreated(JobletType<?> jobletType, String serverName){
		Iterable<JobletRequest> jobletRequests = jobletNodes.jobletRequest().scan(null, null);
		String serverNamePrefix = serverName + "_";//don't want joblet1 to include joblet10
		List<JobletRequest> jobletRequestsToReset = JobletRequest.filterByTypeStatusReservedByPrefix(jobletRequests,
				jobletType, JobletStatus.running, serverNamePrefix);
		logger.warn("found "+DrCollectionTool.size(jobletRequestsToReset)+" jobletRequests to reset");

		for(JobletRequest jobletRequest : jobletRequestsToReset){
			handleJobletInterruption(jobletRequest);
		}
	}

	/*--------------------- delete -----------------------*/

	public void deleteJobletRequestAndData(JobletRequest request){
		jobletNodes.jobletRequest().delete(request.getKey(), null);
		jobletNodes.jobletData().delete(request.getJobletDataKey(), null);
	}

	public void deleteJobletDatasForJobletRequests(Collection<JobletRequest> jobletRequests){
		List<JobletDataKey> jobletDataKeys = StreamTool.map(jobletRequests, JobletRequest::getJobletDataKey);
		jobletNodes.jobletData().deleteMulti(jobletDataKeys, null);
	}

	/*--------------------- lifecycle events -----------------------*/

	public void handleJobletInterruption(JobletRequest jobletRequest){
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		JobletStatus setStatusTo = jobletRequest.getRestartable() ? JobletStatus.created : JobletStatus.interrupted;
		jobletRequest.setStatus(setStatusTo);
		requeueJobletRequest(jobletRequest);
		jobletNodes.jobletRequest().put(jobletRequest, new Config().setPutMethod(PutMethod.UPDATE_OR_BUST));
		logger.warn("interrupted {}, set status={}, reservedBy=null, reservedAt=null", jobletRequest.getKey(),
				setStatusTo);
	}

	public void handleJobletError(JobletRequest jobletRequest, Exception exception, String location){
		ExceptionRecord exceptionRecord = exceptionRecorder.tryRecordException(exception, location,
				JobExceptionCategory.JOBLET);
		jobletRequest.setExceptionRecordId(exceptionRecord.getKey().getId());
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		jobletRequest.incrementNumFailures();
		if(jobletRequest.getRestartable() && ! jobletRequest.hasReachedMaxFailures()){
			jobletRequest.setStatus(JobletStatus.created);
			requeueJobletRequest(jobletRequest);
		}else{
			jobletRequest.setStatus(JobletStatus.failed);
		}
		jobletNodes.jobletRequest().put(jobletRequest, new Config().setPutMethod(PutMethod.UPDATE_OR_BUST));
	}

	public void handleJobletCompletion(JobletRequest jobletRequest){
		if(jobletRequest.getQueueMessageKey() != null){
			JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(jobletRequest);
			jobletNodes.jobletRequestQueueByKey().get(queueKey).ack(jobletRequest.getQueueMessageKey(), null);
		}
		deleteJobletRequestAndData(jobletRequest);
	}

	private void requeueJobletRequest(JobletRequest jobletRequest){
		if(jobletRequest.getQueueMessageKey() == null){
			return;
		}
		JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(jobletRequest);
		//rather than ack/put, is there an ack(false) mechanism?
		jobletNodes.jobletRequestQueueByKey().get(queueKey).ack(jobletRequest.getQueueMessageKey(), null);
		jobletNodes.jobletRequestQueueByKey().get(queueKey).put(jobletRequest, null);
	}

}
