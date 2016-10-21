package com.hotpads.joblet.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.JobletSettings;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.JobletExecutorThread;
import com.hotpads.joblet.execute.ParallelJobletProcessor;
import com.hotpads.joblet.execute.ParallelJobletProcessors;
import com.hotpads.joblet.jdbc.RestartJobletRequests;
import com.hotpads.joblet.jdbc.TimeoutStuckRunningJobletRequests;
import com.hotpads.joblet.test.SleepingJoblet;
import com.hotpads.joblet.test.SleepingJoblet.SleepingJobletParams;

public class JobletHandler extends BaseHandler{

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
	private final ParallelJobletProcessors parallelJobletProcessors;
	private final JobletService jobletService;
	private final JobletSettings jobletSettings;

	@Inject
	public JobletHandler(Datarouter datarouter, DatarouterProperties datarouterProperties, JobletTypeFactory
			jobletTypeFactory, JobletNodes jobletNodes,	ParallelJobletProcessors parallelJobletProcessors, JobletService
			jobletService, JobletSettings jobletSettings){
		this.datarouter = datarouter;
		this.serverName = datarouterProperties.getServerName();
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.parallelJobletProcessors = parallelJobletProcessors;
		this.jobletService = jobletService;
		this.jobletSettings = jobletSettings;
	}

	@Override
	@Handler
	protected Mav handleDefault(){
		Mav mav = new Mav(JSP_joblets);
		mav.put("minServers", jobletSettings.getMinJobletServers().getValue());
		mav.put("maxServers", jobletSettings.getMaxJobletServers().getValue());
		mav.put("jobletStatuses", JobletStatus.values());
		mav.put("runningJobletThreads", getRunningJobletThreads());
		mav.put("waitingJobletThreads", getWaitingJobletThreads());
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
	private Mav restartFailed(){
		int numRestarted = datarouter.run(new RestartJobletRequests(datarouter, jobletNodes, JobletStatus.failed));
		return new MessageMav("restarted "+numRestarted);
	}

	@Handler
	private Mav restartTimedOut(){
		int numRestarted = datarouter.run(new RestartJobletRequests(datarouter, jobletNodes, JobletStatus.timedOut));
		return new MessageMav("restarted "+numRestarted);
	}

	@Handler
	private Mav timeoutStuckRunning(){
		int numTimedOut = datarouter.run(new TimeoutStuckRunningJobletRequests(datarouter, jobletNodes));
		return new MessageMav("timedOut "+numTimedOut);
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
		mav.put("numCpuPermits", jobletSettings.getCpuTickets().getValue());
		mav.put("numMemoryPermits", jobletSettings.getMemoryTickets().getValue());
		mav.put("isThrottling", false);//because JobletThrottle no longer exists

		List<TypeSummaryDto> typeSummaryDtos = parallelJobletProcessors.getMap().values().stream()
				.map(TypeSummaryDto::new)
				.sorted(Comparator.comparing(TypeSummaryDto::getJobletType))
				.collect(Collectors.toList());

		//totals for server
		mav.put("typeSummaryDtos", typeSummaryDtos);

		//stats by type
		mav.put("totalRunning", TypeSummaryDto.getTotalRunning(typeSummaryDtos));
		mav.put("totalRunningCpuPermits", TypeSummaryDto.getTotalRunningCpuPermits(typeSummaryDtos));
		mav.put("totalRunningMemoryPermits", TypeSummaryDto.getTotalRunningMemoryPermits(typeSummaryDtos));
		mav.put("totalWaiting", TypeSummaryDto.getTotalWaiting(typeSummaryDtos));

		//all threads
		mav.put("runningJobletThreads", getRunningJobletThreads());
		mav.put("waitingJobletThreads", getWaitingJobletThreads());
		return mav;
	}

	@Handler
	private Mav killJobletThread(){
		long threadId = params.requiredLong("threadId");
		parallelJobletProcessors.killThread(threadId);
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	@Handler
	private Mav restartExecutor(){
		Integer jobletTypeCode = params.requiredInteger("jobletTypeCode");
		parallelJobletProcessors.restartExecutor(jobletTypeCode);
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	// /datarouter/joblets/createSleepingJoblets?numJoblets=100&sleepMs=500
	@Handler
	private Mav createSleepingJoblets(int numJoblets, long sleepMs){
		List<JobletPackage> jobletPackages = new ArrayList<>();
		for(int i = 0; i < numJoblets; ++i){
			SleepingJobletParams params = new SleepingJobletParams(String.valueOf(i), sleepMs);
			int batchSequence = i;//specify this so joblets execute in precise order
			JobletPackage jobletPackage = JobletPackage.createDetailed(SleepingJoblet.JOBLET_TYPE,
					JobletPriority.DEFAULT, new Date(), batchSequence, true, null, params);
			jobletPackages.add(jobletPackage);
		}
		jobletService.submitJobletPackages(jobletPackages);
		return new MessageMav("created " + numJoblets + " @" + sleepMs + "ms");
	}


	/*--------------------- private -------------------------*/

	private Map<String,List<JobletExecutorThread>> getWaitingJobletThreads() {
		return getJobletThreads(parallelJobletProcessors.getCurrentlyWaitingJobletExecutorThreads());
	}

	private Map<String,List<JobletExecutorThread>> getRunningJobletThreads() {
		return getJobletThreads(parallelJobletProcessors.getCurrentlyRunningJobletExecutorThreads());
	}

	private Map<String,List<JobletExecutorThread>> getJobletThreads(List<JobletExecutorThread> jobletThreads){
		Map<String,List<JobletExecutorThread>> jobletThreadsByServer = new HashMap<>();
		jobletThreadsByServer.put(serverName, jobletThreads);
		return jobletThreadsByServer;
	}

	public static class TypeSummaryDto{
		private final JobletType<?> jobletTypeEnum;
		private final int numRunning;
		private final int numWaiting;

		public TypeSummaryDto(ParallelJobletProcessor processor){
			this.jobletTypeEnum = processor.getJobletType();
			this.numRunning = processor.getRunningJobletExecutorThreads().size();
			this.numWaiting = processor.getWaitingJobletExecutorThreads().size();
		}

		public static int getTotalRunning(Collection<TypeSummaryDto> dtos){
			return dtos.stream().mapToInt(TypeSummaryDto::getNumRunning).sum();
		}

		public static long getTotalRunningCpuPermits(Collection<TypeSummaryDto> dtos){
			return dtos.stream().mapToLong(TypeSummaryDto::getNumRunningCpuPermits).sum();
		}

		public static long getTotalRunningMemoryPermits(Collection<TypeSummaryDto> dtos){
			return dtos.stream().mapToLong(TypeSummaryDto::getNumRunningMemoryPermits).sum();
		}

		public static int getTotalWaiting(Collection<TypeSummaryDto> dtos){
			return dtos.stream().mapToInt(TypeSummaryDto::getNumWaiting).sum();
		}

		public long getNumRunningCpuPermits(){
			return jobletTypeEnum.getCpuPermits() * numRunning;
		}

		public long getNumRunningMemoryPermits(){
			return jobletTypeEnum.getMemoryPermits() * numRunning;
		}

		public String getJobletType(){
			return jobletTypeEnum.getPersistentString();
		}

		public int getNumRunning(){
			return numRunning;
		}

		public int getNumWaiting(){
			return numWaiting;
		}
	}

}
