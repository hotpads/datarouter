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
import com.hotpads.joblet.databean.Joblet;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletDataKey;
import com.hotpads.joblet.databean.JobletKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.hibernate.DeleteJoblet;
import com.hotpads.joblet.hibernate.GetJobletForProcessing;
import com.hotpads.joblet.hibernate.GetJobletStatuses;
import com.hotpads.joblet.hibernate.UpdateJobletAndQueue;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.stream.StreamTool;

@Singleton
public class JobletService{
	private static Logger logger = LoggerFactory.getLogger(JobletService.class);

	public static final int MAX_JOBLET_RETRIES = 10;

	private final DatarouterInjector injector;
	private final Datarouter datarouter;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final ExceptionRecorder exceptionRecorder;

	@Inject
	public JobletService(DatarouterInjector injector, Datarouter datarouter, JobletTypeFactory jobletTypeFactory,
			JobletNodes jobletNodes, ExceptionRecorder exceptionRecorder){
		this.injector = injector;
		this.datarouter = datarouter;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.exceptionRecorder = exceptionRecorder;
	}


	public List<JobletProcess> getJobletProcessesOfType(JobletType<?> jobletType){
		JobletKey prefix = new JobletKey(jobletType, null, null, null);
		return jobletNodes.joblet().streamWithPrefix(prefix, null)
				.map(this::getJobletProcessForJoblet)
				.collect(Collectors.toList());
	}

	public JobletProcess getJobletProcessForJoblet(Joblet joblet){
		JobletData jobletData = jobletNodes.jobletData().get(joblet.getJobletDataKey(), null);
		JobletType<?> jobletType = jobletTypeFactory.fromJoblet(joblet);
		JobletProcess jobletProcess = injector.getInstance(jobletType.getAssociatedClass());
		jobletProcess.setJoblet(joblet);
		jobletProcess.setJobletData(jobletData);
		return jobletProcess;
	}

	public Joblet getJobletForProcessing(JobletType<?> type, String reservedBy, long jobletTimeoutMs,
			boolean rateLimited){
		return datarouter.run(new GetJobletForProcessing(jobletTimeoutMs, MAX_JOBLET_RETRIES, reservedBy, type,
				datarouter, jobletNodes, rateLimited));
	}

	public JobletData getJobletData(Joblet joblet){
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

	public void handleJobletInterruption(Joblet joblet, boolean rateLimited){
		joblet.setStatus(JobletStatus.created);
		joblet.setReservedBy(null);
		joblet.setReservedAt(null);
		datarouter.run(new UpdateJobletAndQueue(jobletTypeFactory, joblet, true, datarouter, jobletNodes,
				rateLimited));
		logger.warn("interrupted "+joblet.getKey()+", set status=created, reservedBy=null, reservedAt=null");
	}

	public void handleJobletError(Joblet joblet, boolean rateLimited, Exception exception, String location){
		joblet.setNumFailures(joblet.getNumFailures() + 1);
		if(joblet.getNumFailures() < joblet.getMaxFailures()){
			joblet.setStatus(JobletStatus.created);
		}else{
			joblet.setStatus(JobletStatus.failed);
		}
		ExceptionRecord exceptionRecord = exceptionRecorder.tryRecordException(exception, location,
				JobExceptionCategory.JOBLET);
		joblet.setExceptionRecordId(exceptionRecord.getKey().getId());
		joblet.setReservedBy(null);
		joblet.setReservedAt(null);
		datarouter.run(new UpdateJobletAndQueue(jobletTypeFactory, joblet, true, datarouter, jobletNodes,
				rateLimited));
	}

	public void handleJobletCompletion(Joblet joblet, boolean decrementQueueIfRateLimited, boolean rateLimited){
		datarouter.run(new DeleteJoblet(datarouter, jobletTypeFactory, joblet, jobletNodes, rateLimited));
	}

	public void submitJoblets(Collection<? extends JobletProcess> jobletProcesses){
		jobletNodes.jobletData().putMulti(JobletProcess.getJobletDatas(jobletProcesses), null);
		jobletProcesses.forEach(jobletProcess -> jobletProcess.updateJobletDataIdReference());
		jobletNodes.joblet().putMulti(JobletProcess.getJoblets(jobletProcesses), null);
	}

	public void setJobletsRunningOnServerToCreated(JobletType<?> jobletType, String serverName, boolean rateLimited){
		Iterable<Joblet> joblets = jobletNodes.joblet().scan(null, null);
		String serverNamePrefix = serverName + "_";//don't want joblet1 to include joblet10
		List<Joblet> jobletsToReset = Joblet.filterByTypeStatusReservedByPrefix(joblets, jobletType,
				JobletStatus.running, serverNamePrefix);
		logger.warn("found "+DrCollectionTool.size(jobletsToReset)+" joblets to reset");

		for(Joblet j : jobletsToReset){
			handleJobletInterruption(j, rateLimited);
		}
	}

	public List<JobletSummary> getJobletSummaries(boolean slaveOk){
		Iterable<Joblet> scanner = jobletNodes.joblet().scan(null, new Config().setSlaveOk(slaveOk));
		return Joblet.getJobletCountsCreatedByType(jobletTypeFactory, scanner);
	}

	public List<JobletSummary> getJobletSummariesForTable(String whereStatus, boolean includeQueueId){
		return datarouter.run(new GetJobletStatuses(whereStatus, includeQueueId, datarouter, jobletNodes));
	}

	public boolean jobletExistsWithTypeAndStatus(JobletType<?> jobletType, JobletStatus jobletStatus){
		JobletKey key = new JobletKey(jobletType, null, null, null);
		Range<JobletKey> range = new Range<>(key, true, key, true);
		Config config = new Config().setIterateBatchSize(50);
		for(Joblet joblet : jobletNodes.joblet().scan(range, config)){
			if(jobletStatus == joblet.getStatus()){
				return true;
			}
		}
		return false;
	}

	public void deleteJobletDatasForJoblets(Collection<Joblet> joblets){
		List<JobletDataKey> jobletDataKeys = StreamTool.map(joblets, Joblet::getJobletDataKey);
		jobletNodes.jobletData().deleteMulti(jobletDataKeys, null);
	}
}