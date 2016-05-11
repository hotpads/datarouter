package com.hotpads.joblet;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.job.trigger.JobExceptionCategory;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.hibernate.DeleteJobletRequest;
import com.hotpads.joblet.hibernate.GetJobletRequestForProcessing;
import com.hotpads.joblet.hibernate.GetJobletRequestStatuses;
import com.hotpads.joblet.hibernate.UpdateJobletRequestAndQueue;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.stream.StreamTool;

@Singleton
public class JobletService{
	private static Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	private final Datarouter datarouter;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletFactory jobletFactory;
	private final JobletNodes jobletNodes;
	private final ExceptionRecorder exceptionRecorder;

	@Inject
	public JobletService(DatarouterInjector injector, Datarouter datarouter, JobletTypeFactory jobletTypeFactory,
			JobletFactory jobletFactory, JobletNodes jobletNodes, ExceptionRecorder exceptionRecorder){
		this.datarouter = datarouter;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletFactory = jobletFactory;
		this.jobletNodes = jobletNodes;
		this.exceptionRecorder = exceptionRecorder;
	}


	public List<JobletPackage> getJobletPackagesOfType(JobletType<?> jobletType){
		JobletRequestKey prefix = new JobletRequestKey(jobletType, null, null, null);
		return jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
				.map(this::getJobletPackageForJoblet)
				.collect(Collectors.toList());
	}

	public JobletPackage getJobletPackageForJoblet(JobletRequest jobletRequest){
		JobletData jobletData = jobletNodes.jobletData().get(jobletRequest.getJobletDataKey(), null);
		return new JobletPackage(jobletRequest, jobletData);
	}

	public JobletRequest getJobletRequestForProcessing(JobletType<?> type, String reservedBy, long jobletTimeoutMs,
			boolean rateLimited){
		return datarouter.run(new GetJobletRequestForProcessing(jobletTimeoutMs, MAX_JOBLET_RETRIES, reservedBy, type,
				datarouter, jobletNodes, rateLimited));
	}

	public JobletData getJobletData(JobletRequest joblet){
		Long jobletDataId = joblet.getJobletDataId();
		JobletData jobletData = getJobletData(jobletDataId);
		return jobletData;
	}

	public JobletData getJobletData(Long jobletDataId){
		// mysql has a bug that returns the lastest auto-increment row when queried for null
		if(jobletDataId == null){
			return null;
		}// avoid querying for null
		return jobletNodes.jobletData().get(new JobletDataKey(jobletDataId), null);
	}

	public void handleJobletInterruption(JobletRequest jobletRequest, boolean rateLimited){
		jobletRequest.setStatus(JobletStatus.created);
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		datarouter.run(new UpdateJobletRequestAndQueue(jobletTypeFactory, jobletRequest, true, datarouter, jobletNodes,
				rateLimited));
		logger.warn("interrupted "+jobletRequest.getKey()+", set status=created, reservedBy=null, reservedAt=null");
	}

	public void handleJobletError(JobletRequest jobletRequest, boolean rateLimited, Exception exception,
			String location){
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
		datarouter.run(new UpdateJobletRequestAndQueue(jobletTypeFactory, jobletRequest, true, datarouter, jobletNodes,
				rateLimited));
	}

	public void handleJobletCompletion(JobletRequest jobletRequest, boolean decrementQueueIfRateLimited,
			boolean rateLimited){
		datarouter.run(new DeleteJobletRequest(datarouter, jobletTypeFactory, jobletRequest, jobletNodes, rateLimited));
	}

	public void submitJobletPackages(Collection<JobletPackage> jobletPackages){
		jobletNodes.jobletData().putMulti(JobletPackage.getJobletDatas(jobletPackages), null);
		jobletPackages.forEach(JobletPackage::updateJobletDataIdReference);
		jobletNodes.jobletRequest().putMulti(JobletPackage.getJobletRequests(jobletPackages), null);
	}

	public void setJobletRequestsRunningOnServerToCreated(JobletType<?> jobletType, String serverName,
			boolean rateLimited){
		Iterable<JobletRequest> jobletRequests = jobletNodes.jobletRequest().scan(null, null);
		String serverNamePrefix = serverName + "_";//don't want joblet1 to include joblet10
		List<JobletRequest> jobletRequestsToReset = JobletRequest.filterByTypeStatusReservedByPrefix(jobletRequests,
				jobletType, JobletStatus.running, serverNamePrefix);
		logger.warn("found "+DrCollectionTool.size(jobletRequestsToReset)+" jobletRequests to reset");

		for(JobletRequest jobletRequest : jobletRequestsToReset){
			handleJobletInterruption(jobletRequest, rateLimited);
		}
	}

	public List<JobletSummary> getJobletSummaries(boolean slaveOk){
		Iterable<JobletRequest> scanner = jobletNodes.jobletRequest().scan(null, new Config().setSlaveOk(slaveOk));
		return JobletRequest.getJobletCountsCreatedByType(jobletTypeFactory, scanner);
	}

	public List<JobletSummary> getJobletSummariesForTable(String whereStatus, boolean includeQueueId){
		return datarouter.run(new GetJobletRequestStatuses(whereStatus, includeQueueId, datarouter, jobletNodes));
	}

	public boolean jobletRequestExistsWithTypeAndStatus(JobletType<?> jobletType, JobletStatus jobletStatus){
		JobletRequestKey key = new JobletRequestKey(jobletType, null, null, null);
		Range<JobletRequestKey> range = new Range<>(key, true, key, true);
		Config config = new Config().setIterateBatchSize(50);
		for(JobletRequest jobletRequest : jobletNodes.jobletRequest().scan(range, config)){
			if(jobletStatus == jobletRequest.getStatus()){
				return true;
			}
		}
		return false;
	}

	public void deleteJobletDatasForJoblets(Collection<JobletRequest> jobletRequests){
		List<JobletDataKey> jobletDataKeys = StreamTool.map(jobletRequests, JobletRequest::getJobletDataKey);
		jobletNodes.jobletData().deleteMulti(jobletDataKeys, null);
	}
}
