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
import com.hotpads.joblet.hibernate.GetJobletRequestForProcessing;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.profile.PhaseTimer;
import com.hotpads.util.core.stream.StreamTool;

@Singleton
public class JobletService{
	private static Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	private final Datarouter datarouter;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final ExceptionRecorder exceptionRecorder;

	@Inject
	public JobletService(DatarouterInjector injector, Datarouter datarouter, JobletTypeFactory jobletTypeFactory,
			JobletNodes jobletNodes, ExceptionRecorder exceptionRecorder){
		this.datarouter = datarouter;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.exceptionRecorder = exceptionRecorder;
	}

	/*--------------------- create ------------------------*/

	public void submitJobletPackages(Collection<JobletPackage> jobletPackages){
		String typeString = DrCollectionTool.getFirst(jobletPackages).getJoblet().getTypeString();
		PhaseTimer timer = new PhaseTimer("insert " + jobletPackages.size() + typeString);
		jobletNodes.jobletData().putMulti(JobletPackage.getJobletDatas(jobletPackages), null);
		timer.add("inserted JobletData");
		jobletPackages.forEach(JobletPackage::updateJobletDataIdReference);
		jobletNodes.jobletRequest().putMulti(JobletPackage.getJobletRequests(jobletPackages), null);
		timer.add("inserted JobletRequest");
		if(timer.getElapsedTimeBetweenFirstAndLastEvent() > 200){
			logger.warn("slow insert joblets:{}", timer);
		}
	}

	/*---------------------- read ---------------------------*/

	public List<JobletPackage> getJobletPackagesOfType(JobletType<?> jobletType){
		JobletRequestKey prefix = new JobletRequestKey(jobletType, null, null, null);
		return jobletNodes.jobletRequest().streamWithPrefix(prefix, null)
				.map(this::getJobletPackageForJobletRequest)
				.collect(Collectors.toList());
	}

	public JobletPackage getJobletPackageForJobletRequest(JobletRequest jobletRequest){
		JobletData jobletData = jobletNodes.jobletData().get(jobletRequest.getJobletDataKey(), null);
		return new JobletPackage(jobletRequest, jobletData);
	}

	public JobletRequest getJobletRequestForProcessing(JobletType<?> type, String reservedBy, long jobletTimeoutMs){
		long startMs = System.currentTimeMillis();
		JobletRequest jobletRequest = datarouter.run(new GetJobletRequestForProcessing(jobletTimeoutMs,
				MAX_JOBLET_RETRIES, reservedBy, type, datarouter, jobletNodes));
		long durationMs = System.currentTimeMillis() - startMs;
		if(durationMs > 200){
			String message = jobletRequest == null ? "none" : jobletRequest.getKey().toString();
			logger.warn("slow get joblet type={}, durationMs={}, got {}", type.getPersistentString(), durationMs,
					message);
		}
		return jobletRequest;
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

	public JobletData getJobletDataForJobletRequest(JobletRequest joblet){
		return jobletNodes.jobletData().get(joblet.getJobletDataKey(), null);
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
		jobletRequest.setStatus(JobletStatus.created);
		jobletRequest.setReservedBy(null);
		jobletRequest.setReservedAt(null);
		logger.warn("interrupted "+jobletRequest.getKey()+", set status=created, reservedBy=null, reservedAt=null");
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
	}

	public void handleJobletCompletion(JobletRequest jobletRequest){
		deleteJobletRequestAndData(jobletRequest);
	}

	/*--------------------- summaries -----------------------*/

	public List<JobletSummary> getJobletSummaries(boolean slaveOk){
		Iterable<JobletRequest> scanner = jobletNodes.jobletRequest().scan(null, new Config().setSlaveOk(slaveOk));
		return JobletRequest.getJobletCountsCreatedByType(jobletTypeFactory, scanner);
	}
}
