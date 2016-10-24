package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.queue.QueueMessage;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.job.trigger.JobExceptionCategory;
import com.hotpads.joblet.dao.JobletRequestDao;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletQueueMechanism;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.ParallelJobletProcessor;
import com.hotpads.joblet.jdbc.GetJobletRequest;
import com.hotpads.joblet.jdbc.JobletRequestSqlBuilder;
import com.hotpads.joblet.jdbc.ReserveJobletRequest;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.core.stream.StreamTool;

@Singleton
public class JobletService{
	private static final Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	private final Datarouter datarouter;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final ExceptionRecorder exceptionRecorder;
	private final JdbcFieldCodecFactory jdbcFieldCodecFactory;
	private final JobletRequestSqlBuilder jobletRequestSqlBuilder;
	private final JobletRequestDao jobletRequestDao;
	private final JobletSettings jobletSettings;

	@Inject
	public JobletService(Datarouter datarouter, JobletTypeFactory jobletTypeFactory, JobletNodes jobletNodes,
			ExceptionRecorder exceptionRecorder, JdbcFieldCodecFactory jdbcFieldCodecFactory,
			JobletRequestSqlBuilder jobletRequestSqlBuilder, JobletRequestDao jobletRequestDao,
			JobletSettings jobletSettings){
		this.datarouter = datarouter;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.exceptionRecorder = exceptionRecorder;
		this.jdbcFieldCodecFactory = jdbcFieldCodecFactory;
		this.jobletRequestSqlBuilder = jobletRequestSqlBuilder;
		this.jobletRequestDao = jobletRequestDao;
		this.jobletSettings = jobletSettings;
	}

	/*--------------------- create ------------------------*/

	public void submitJobletPackages(Collection<JobletPackage> jobletPackages){
		String typeString = DrCollectionTool.getFirst(jobletPackages).getJobletRequest().getTypeString();
		PhaseTimer timer = new PhaseTimer("insert " + jobletPackages.size() + " " + typeString);
		jobletNodes.jobletData().putMulti(JobletPackage.getJobletDatas(jobletPackages), Configs.insertOrBust());
		timer.add("inserted JobletData");
		jobletPackages.forEach(JobletPackage::updateJobletDataIdReference);
		jobletNodes.jobletRequest().putMulti(JobletPackage.getJobletRequests(jobletPackages), Configs.insertOrBust());
		timer.add("inserted JobletRequest");
		for(JobletPackage jobletPackage : jobletPackages){
			JobletRequest request = jobletPackage.getJobletRequest();
			JobletType<?> type = jobletTypeFactory.fromJobletRequest(request);
			JobletPriority priority = request.getKey().getPriority();
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, priority);
			jobletNodes.jobletRequestQueueByKey().get(queueKey).put(request, null);
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

	public JobletRequest getJobletRequestForProcessing(JobletType<?> type, JobletPriority startAtPriority,
			String reservedBy){
		long startMs = System.currentTimeMillis();
		Optional<JobletRequest> jobletRequest;
		JobletQueueMechanism queueMechanism = jobletSettings.getQueueMechanismEnum();
		if(JobletQueueMechanism.JDBC_LOCK_FOR_UPDATE == queueMechanism){
			jobletRequest = Optional.ofNullable(getJobletRequestByGetOp(type, reservedBy));
		}else if(JobletQueueMechanism.JDBC_UPDATE_AND_SCAN == queueMechanism){
			jobletRequest = Optional.ofNullable(getJobletRequestByReserveOp(type, reservedBy));
		}else if(JobletQueueMechanism.SQS == queueMechanism){
			jobletRequest = getJobletRequestFromQueues(type, startAtPriority, reservedBy);
		}else{
			throw new IllegalStateException("unknown JobletQueueMechanism");
		}
		long durationMs = System.currentTimeMillis() - startMs;
		if(durationMs > 200){
			String message = jobletRequest.map(Databean::getKey).map(Object::toString).orElse("none");
			logger.warn("slow get joblet type={}, durationMs={}, got {}", type, durationMs, message);
		}
		return jobletRequest.orElse(null);
	}

	private JobletRequest getJobletRequestByGetOp(JobletType<?> type, String reservedBy){
		while(true){
			GetJobletRequest jdbcOp = new GetJobletRequest(reservedBy, type, datarouter, jobletNodes,
					jdbcFieldCodecFactory, jobletRequestSqlBuilder);
			JobletRequest jobletRequest = datarouter.run(jdbcOp);
			if(jobletRequest == null){
				return null;
			}
			if( ! jobletRequest.getStatus().isRunning()){
				continue;//weird flow.  it was probably just marked as timedOut, so skip it
			}
			return jobletRequest;
		}
	}

	private JobletRequest getJobletRequestByReserveOp(JobletType<?> type, String reservedBy){
		ReserveJobletRequest jdbcOp = new ReserveJobletRequest(reservedBy, type, datarouter, jobletNodes,
				jobletRequestSqlBuilder);
		while(datarouter.run(jdbcOp)){//returns false if no joblet found
			JobletRequest jobletRequest = jobletRequestDao.getReservedRequest(type, reservedBy);
			if(JobletStatus.created == jobletRequest.getStatus()){
				jobletRequest.setStatus(JobletStatus.running);
				jobletNodes.jobletRequest().put(jobletRequest, null);
				return jobletRequest;
			}

			//we got a previously timed-out joblet
			jobletRequest.incrementNumTimeouts();
			if(jobletRequest.getNumTimeouts() <= MAX_JOBLET_RETRIES){
				jobletNodes.jobletRequest().put(jobletRequest, null);
				return jobletRequest;
			}

			jobletRequest.setStatus(JobletStatus.timedOut);
			jobletNodes.jobletRequest().put(jobletRequest, null);
			//loop around for another
		}
		return null;
	}

	private Optional<JobletRequest> getJobletRequestFromQueues(JobletType<?> type, JobletPriority startAtPriority,
			String reservedBy){
		for(JobletPriority priority : JobletPriority.values()){
			if(priority.compareTo(startAtPriority) < 0){
				continue;
			}
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, priority);
			// set timeout to 0 so we return immediately. processor threads can do the waiting
			Config config = new Config().setTimeoutMs(0L)
					.setVisibilityTimeoutMs(ParallelJobletProcessor.RUNNING_JOBLET_TIMEOUT_MS);
			QueueMessage<JobletRequestKey,JobletRequest> message = jobletNodes.jobletRequestQueueByKey().get(queueKey)
					.peek(config);
			if(message == null){
				continue;
			}
			JobletRequest jobletRequest = message.getDatabean();
			jobletRequest.setQueueMessageKey(message.getKey());
			jobletRequest.setReservedBy(reservedBy);
			jobletRequest.setReservedAt(System.currentTimeMillis());
			jobletRequest.setStatus(JobletStatus.running);
			jobletNodes.jobletRequest().put(jobletRequest, null);
			return Optional.of(jobletRequest);
		}
		return Optional.empty();
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
		logger.warn("interrupted {}, set status={}, reservedBy=null, reservedAt=null", jobletRequest.getKey(),
				setStatusTo);
		jobletNodes.jobletRequest().put(jobletRequest, new Config().setPutMethod(PutMethod.UPDATE_OR_BUST));
	}

	public void handleJobletError(JobletRequest jobletRequest, Exception exception, String location){
		jobletRequest.setNumFailures(jobletRequest.getNumFailures() + 1);
		if(jobletRequest.getNumFailures() < jobletRequest.getMaxFailures()){
			jobletRequest.setStatus(JobletStatus.created);
		}else{
			jobletRequest.setStatus(JobletStatus.failed);
		}
		ExceptionRecord exceptionRecord = exceptionRecorder.tryRecordException(exception, location,
				JobExceptionCategory.JOBLET);
		jobletRequest.setExceptionRecordId(exceptionRecord.getKey().getId());
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		jobletNodes.jobletRequest().put(jobletRequest, new Config().setPutMethod(PutMethod.UPDATE_OR_BUST));
	}

	public void handleJobletCompletion(JobletRequest jobletRequest){
		if(jobletRequest.getQueueMessageKey() != null){
			JobletType<?> type = jobletTypeFactory.fromJobletRequest(jobletRequest);
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, jobletRequest.getKey().getPriority());
			jobletNodes.jobletRequestQueueByKey().get(queueKey).ack(jobletRequest.getQueueMessageKey(), null);
		}
		deleteJobletRequestAndData(jobletRequest);
	}

}
