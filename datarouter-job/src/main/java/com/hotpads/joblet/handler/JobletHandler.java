package com.hotpads.joblet.handler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.types.optional.OptionalBoolean;
import com.hotpads.handler.types.optional.OptionalInteger;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.dao.JobletRequestDao;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.dto.JobletTypeSummary;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.JobletProcessors;
import com.hotpads.joblet.queue.JobletRequestQueueKey;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.test.SleepingJoblet;
import com.hotpads.joblet.test.SleepingJoblet.SleepingJobletParams;
import com.hotpads.util.core.iterable.BatchingIterable;

public class JobletHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(JobletHandler.class);

	private static final String
		URL_JOBLETS_IN_CONTEXT = DatarouterJobDispatcher.URL_DATAROUTER + DatarouterJobDispatcher.JOBLETS,

		PARAM_whereStatus = "whereStatus",

		JSP_joblets = "/jsp/joblet/joblets.jsp",
		JSP_queues = "/jsp/joblet/queues.jsp",
		JSP_threads = "/jsp/joblet/threads.jsp",
	 	JSP_exceptions = "/jsp/joblet/jobletExceptions.jsp";

	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final JobletProcessors jobletProcessors;
	private final JobletService jobletService;
	private final JobletSettings jobletSettings;
	private final JobletRequestDao jobletRequestDao;
	private final JobletRequestQueueManager jobletRequestQueueManager;

	@Inject
	public JobletHandler(JobletTypeFactory jobletTypeFactory, JobletNodes jobletNodes,
			JobletProcessors jobletProcessors, JobletService jobletService, JobletSettings jobletSettings,
			JobletRequestDao jobletRequestDao, JobletRequestQueueManager jobletRequestQueueManager){
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.jobletProcessors = jobletProcessors;
		this.jobletService = jobletService;
		this.jobletSettings = jobletSettings;
		this.jobletRequestDao = jobletRequestDao;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
	}

	@Handler(defaultHandler=true)
	private Mav list(){
		Mav mav = new Mav(JSP_joblets);
		mav.put("minServers", jobletSettings.minJobletServers.getValue());
		mav.put("maxServers", jobletSettings.maxJobletServers.getValue());
		mav.put("jobletStatuses", JobletStatus.values());
		mav.put("runningJobletsByType", jobletProcessors.getRunningJobletsByType());
		String statusString = params.optional(PARAM_whereStatus).orElse(null);
		mav.put(PARAM_whereStatus, statusString);
		Stream<JobletRequest> requests = jobletNodes.jobletRequest().stream(null, null);
		if(DrStringTool.notEmpty(statusString)){
			JobletStatus status = JobletStatus.fromPersistentStringStatic(statusString);
			requests = requests.filter(request -> status == request.getStatus());
		}
		mav.put("summaries", JobletSummary.summarizeByTypeExecutionOrderStatus(requests).values());
		mav.put("jobletTypes", jobletTypeFactory.getAllTypes());
		return mav;
	}

	@Handler
	private Mav queues(String jobletType, int jobletTypeCode, int executionOrder){
		Mav mav = new Mav(JSP_queues);
		mav.put("jobletTypeCode", jobletTypeCode);
		mav.put("jobletType", jobletType);
		mav.put("executionOrder", executionOrder);
		JobletRequestKey prefix = new JobletRequestKey(jobletTypeCode, executionOrder, null, null);
		Stream<JobletRequest> requests = jobletNodes.jobletRequest().streamWithPrefix(prefix, null);
		mav.put("summaries", JobletSummary.summarizeByQueueStatus(requests).values());
		return mav;
	}

	@Handler
	private Mav exceptions(){
		Mav mav = new Mav(JSP_exceptions);
		mav.put("failedJoblets", jobletRequestDao.getWithStatus(JobletStatus.failed));
		return mav;
	}

	@Handler
	private Mav copyJobletRequestsToQueues(String jobletType){
		JobletType<?> jobletTypeEnum = jobletTypeFactory.fromPersistentString(jobletType);
		long numCopied = 0;
		//passing this stream to the BatchingIterable like this is causing a seg fault
//		for(List<JobletRequest> requestBatch : new BatchingIterable<>(jobletRequestDao.streamType(jobletTypeEnum,
//				false), 100)){
		Iterable<JobletRequest> jobletsOfType = jobletRequestDao.streamType(jobletTypeEnum, false)::iterator;
		for(List<JobletRequest> requestBatch : new BatchingIterable<>(jobletsOfType, 100)){
			for(JobletRequest request : requestBatch){
				JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(request);
				jobletNodes.jobletRequestQueueByKey().get(queueKey).put(request, null);
				++numCopied;
			}
			logger.warn("copied {}", numCopied);
		}
		return new MessageMav("copied " + numCopied);
	}

	@Handler
	private Mav restart(String type, String status){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentString(type);
		JobletStatus jobletStatus = JobletStatus.fromPersistentStringStatic(status);
		long numRestarted = jobletService.restartJoblets(jobletType, jobletStatus);
		return new MessageMav("restarted " + numRestarted);
	}

	@Handler
	private Mav timeoutStuckRunning(String type){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentString(type);
		Stream<JobletRequest> requests = jobletRequestDao.streamType(jobletType, false)
				.filter(request -> request.getStatus() == JobletStatus.running)
				.filter(request -> request.getReservedAgoMs().isPresent())
				.filter(request -> request.getReservedAgoMs().get() > Duration.ofDays(2).toMillis());
		long numTimedOut = 0;
		for(List<JobletRequest> requestBatch : new BatchingIterable<>(requests::iterator, 100)){
			List<JobletRequest> toSave = new ArrayList<>();
			for(JobletRequest request : requestBatch){
				request.setStatus(JobletStatus.created);
				request.setNumFailures(0);
				++numTimedOut;
			}
			jobletNodes.jobletRequest().putMulti(toSave, null);
			logger.warn("copied {}", numTimedOut);
		}
		return new MessageMav("timedOut " + numTimedOut);
	}

	@Handler
	private Mav threads(){
		Mav mav = new Mav(JSP_threads);

		//cpu and memory ticket info
		mav.put("numCpuPermits", jobletSettings.cpuTickets.getValue());
		mav.put("numMemoryPermits", jobletSettings.memoryTickets.getValue());
		mav.put("isThrottling", false);//because JobletThrottle no longer exists

		//totals for server
		List<JobletTypeSummary> typeSummaries = jobletProcessors.getTypeSummaries();
		mav.put("typeSummaryDtos", typeSummaries);

		//stats by type
		mav.put("totalThreads", JobletTypeSummary.getTotalThreads(typeSummaries));
		mav.put("totalRunning", JobletTypeSummary.getTotalRunning(typeSummaries));
		mav.put("totalRunningCpuPermits", JobletTypeSummary.getTotalRunningCpuPermits(typeSummaries));
		mav.put("totalRunningMemoryPermits", JobletTypeSummary.getTotalRunningMemoryPermits(typeSummaries));

		//RunningJoblets
		mav.put("runningJobletsByType", jobletProcessors.getRunningJobletsByType());
		return mav;
	}

	@Handler
	private Mav killJobletThread(){
		long threadId = params.requiredLong("threadId");
		jobletProcessors.killThread(threadId);
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	// /datarouter/joblets/createSleepingJoblets?numJoblets=1000&sleepMs=500&executionOrder=10&includeFailures=false&failEveryN=100
	@Handler
	private Mav createSleepingJoblets(int numJoblets, long sleepMs, OptionalInteger executionOrder,
			OptionalBoolean includeFailures, OptionalInteger failEveryN){
		JobletPriority priority = JobletPriority.fromExecutionOrder(executionOrder.get());
		List<JobletPackage> jobletPackages = new ArrayList<>();
		for(int i = 0; i < numJoblets; ++i){
			int numFailuresForThisJoblet = 0;
			if(includeFailures.get()){
				boolean failThisJoblet = i % failEveryN.orElse(10) == 0;
				if(failThisJoblet){
					numFailuresForThisJoblet = JobletRequest.MAX_FAILURES + 3;//+3 to see if it causes a problem
				}
			}
			SleepingJobletParams params = new SleepingJobletParams(String.valueOf(i), sleepMs,
					numFailuresForThisJoblet);
			int batchSequence = i;//specify this so joblets execute in precise order
			JobletPackage jobletPackage = JobletPackage.createDetailed(SleepingJoblet.JOBLET_TYPE, priority, new Date(),
					batchSequence, true, null, params);
			jobletPackages.add(jobletPackage);
		}
		jobletService.submitJobletPackages(jobletPackages);
		return new MessageMav("created " + numJoblets + " @" + sleepMs + "ms");
	}

}
