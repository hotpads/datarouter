package com.hotpads.joblet.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.routing.Datarouter;
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
import com.hotpads.joblet.execute.JobletExecutorThread;
import com.hotpads.joblet.execute.JobletProcessors;
import com.hotpads.joblet.jdbc.RestartJobletRequests;
import com.hotpads.joblet.jdbc.TimeoutStuckRunningJobletRequests;
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

	private final Datarouter datarouter;
	private final String serverName;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final JobletProcessors jobletProcessors;
	private final JobletService jobletService;
	private final JobletSettings jobletSettings;
	private final JobletRequestDao jobletRequestDao;
	private final JobletRequestQueueManager jobletRequestQueueManager;

	@Inject
	public JobletHandler(Datarouter datarouter, DatarouterProperties datarouterProperties,
			JobletTypeFactory jobletTypeFactory, JobletNodes jobletNodes, JobletProcessors jobletProcessors,
			JobletService jobletService, JobletSettings jobletSettings, JobletRequestDao jobletRequestDao,
			JobletRequestQueueManager jobletRequestQueueManager){
		this.datarouter = datarouter;
		this.serverName = datarouterProperties.getServerName();
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.jobletProcessors = jobletProcessors;
		this.jobletService = jobletService;
		this.jobletSettings = jobletSettings;
		this.jobletRequestDao = jobletRequestDao;
		this.jobletRequestQueueManager = jobletRequestQueueManager;
	}

	@Override
	@Handler
	protected Mav handleDefault(){
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
	private Mav listExceptions(){
		Mav mav = new Mav(JSP_exceptions);
		List<JobletRequest> failedJoblets = new ArrayList<>();
		for(JobletRequest joblet : jobletNodes.jobletRequest().scan(null, null)){
			if(joblet.getStatus() == JobletStatus.failed){
				failedJoblets.add(joblet);
			}
		}
		mav.put("failedJoblets", failedJoblets);
		return mav;
	}

	@Handler
	private Mav copyJobletRequestsToQueues(String jobletType){
		JobletType<?> jobletTypeEnum = jobletTypeFactory.fromPersistentString(jobletType);
		long numCopied = 0;
		for(List<JobletRequest> requestBatch : new BatchingIterable<>(jobletRequestDao.streamType(jobletTypeEnum,
				false)::iterator, 100)){
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
	private Mav restartFailed(){
		int numRestarted = datarouter.run(new RestartJobletRequests(datarouter, jobletNodes, JobletStatus.failed));
		return new MessageMav("restarted " + numRestarted);
	}

	@Handler
	private Mav restartTimedOut(){
		int numRestarted = datarouter.run(new RestartJobletRequests(datarouter, jobletNodes, JobletStatus.timedOut));
		return new MessageMav("restarted " + numRestarted);
	}

	@Handler
	private Mav timeoutStuckRunning(){
		int numTimedOut = datarouter.run(new TimeoutStuckRunningJobletRequests(datarouter, jobletNodes));
		return new MessageMav("timedOut " + numTimedOut);
	}

	@Handler
	private Mav deleteTimedOutJoblets(){
		int numDeleted = 0;
		for(JobletRequest request : jobletNodes.jobletRequest().scan(null, null)){
			if(JobletStatus.timedOut == request.getStatus()){
				//delete individually to minimize joblet table locking
				jobletService.deleteJobletRequestAndData(request);
				++numDeleted;
			}
		}
		return new MessageMav("deleted " + numDeleted);
	}

	@Handler
	private Mav showThreads(){
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

	@Handler
	private Mav restartExecutor(){
		Integer jobletTypeCode = params.requiredInteger("jobletTypeCode");
		jobletProcessors.restartExecutor(jobletTypeCode);
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


	/*--------------------- private -------------------------*/

	private Map<String,List<JobletExecutorThread>> getJobletThreads(List<JobletExecutorThread> jobletThreads){
		Map<String,List<JobletExecutorThread>> jobletThreadsByServer = new HashMap<>();
		jobletThreadsByServer.put(serverName, jobletThreads);
		return jobletThreadsByServer;
	}

}
