package com.hotpads.joblet.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.JobletSettings;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.enums.JobletStatus;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.JobletExecutorThread;
import com.hotpads.joblet.execute.ParallelJobletProcessors;
import com.hotpads.joblet.hibernate.RestartJobletRequests;
import com.hotpads.joblet.hibernate.TimeoutStuckRunningJobletRequests;

public class JobletHandler extends BaseHandler{

	private static final String
		URL_JOBLETS_IN_CONTEXT = DatarouterJobDispatcher.URL_DATAROUTER + DatarouterJobDispatcher.JOBLETS,

		PARAM_whereStatus = "whereStatus",
		PARAM_expanded = "expanded",

		JSP_joblets = "/jsp/joblet/joblets.jsp",
		JSP_threads = "/jsp/joblet/threads.jsp",
	 	JSP_exceptions = "/jsp/joblet/jobletExceptions.jsp";

	private final Datarouter datarouter;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final ParallelJobletProcessors parallelJobletProcessors;
	private final JobletService jobletService;
	private final JobletSettings jobletSettings;

	@Inject
	public JobletHandler(Datarouter datarouter, JobletTypeFactory jobletTypeFactory, JobletNodes jobletNodes,
			ParallelJobletProcessors parallelJobletProcessors, JobletService jobletService,
			JobletSettings jobletSettings){
		this.datarouter = datarouter;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.parallelJobletProcessors = parallelJobletProcessors;
		this.jobletService = jobletService;
		this.jobletSettings = jobletSettings;
	}

	@Override
	@Handler
	protected Mav handleDefault(){
		return showJoblets();
	}

	@Handler
	private Mav showJoblets(){
		Mav mav = new Mav(JSP_joblets);
		mav.put("minServers", jobletSettings.getMinJobletServers().getValue());
		mav.put("maxServers", jobletSettings.getMaxJobletServers().getValue());
		mav.put("jobletStatuses", JobletStatus.values());
		mav.put("runningJobletThreads", getRunningJobletThreads());
		mav.put("waitingJobletThreads", getWaitingJobletThreads());
		String whereStatus = params.optional(PARAM_whereStatus).orElse(null);
		boolean expanded = params.optionalBoolean(PARAM_expanded, false);
		if(DrStringTool.notEmpty(whereStatus)){
			mav.put(PARAM_whereStatus, whereStatus);
		}
		mav.put("whereStatus", whereStatus);
		List<JobletSummary> summaries = jobletService.getJobletSummariesForTable(whereStatus, true);
		List<JobletSummary> combinedSummaries = new ArrayList<>();
		if(!expanded){
			int combinedIndex = 0;
			JobletSummary condensedSummary = null;
			for(JobletSummary summary : summaries){
				if(condensedSummary == null){
					condensedSummary = createCondensedSummary(summary);
					combinedSummaries.add(summary);
					continue;
				}
				if(!condensedSummary.getTypeString().equals(summary.getTypeString())
						|| condensedSummary.getExecutionOrder().intValue() != summary.getExecutionOrder().intValue()){
					if(condensedSummary.getNumType() != combinedSummaries.get(combinedIndex).getNumType()){
						combinedSummaries.add(combinedIndex, condensedSummary);
					}
					combinedIndex = combinedSummaries.size();
					combinedSummaries.add(summary);
					condensedSummary = createCondensedSummary(summary);

				}else{
					condensedSummary.setNumType(condensedSummary.getNumType() + summary.getNumType());
					condensedSummary.setSumItems(condensedSummary.getSumItems() + summary.getSumItems());
					condensedSummary.setSumTasks(condensedSummary.getSumTasks() + summary.getSumTasks());
					condensedSummary.setAvgItems(condensedSummary.getSumItems().floatValue() / condensedSummary
							.getNumType());
					condensedSummary.setAvgTasks(condensedSummary.getSumTasks().floatValue() / condensedSummary
							.getNumType());
					if(condensedSummary.getFirstCreated().after(summary.getFirstCreated())){
						condensedSummary.setFirstCreated(summary.getFirstCreated());
					}
					if(summary.getFirstReserved() != null && condensedSummary.getFirstReserved() != null
							&& summary.getFirstReserved().before(condensedSummary.getFirstReserved())){
						condensedSummary.setFirstReserved(summary.getFirstReserved());
					}
					combinedSummaries.add(summary);
				}
			}
			if(condensedSummary!= null
					&& condensedSummary.getNumType() != combinedSummaries.get(combinedIndex).getNumType()){
				combinedSummaries.add(combinedIndex, condensedSummary);
			}
			mav.put("summaries", combinedSummaries);
		}else{
			mav.put("summaries", summaries);
		}
		mav.put("expanded", expanded);
		mav.put("jobletTypes", jobletTypeFactory.getAllTypes());
		//mav.addObject(jobletThrottle.get, value)
		return mav;
	}

	private JobletSummary createCondensedSummary(JobletSummary baseSummary){
		JobletSummary newCondensedSummary = new JobletSummary(baseSummary.getTypeString(), baseSummary.getSumItems(),
				baseSummary.getFirstCreated().getTime());
		newCondensedSummary.setNumType(baseSummary.getNumType());
		newCondensedSummary.setSumTasks(baseSummary.getSumTasks());
		newCondensedSummary.setAvgItems(baseSummary.getAvgItems());
		newCondensedSummary.setAvgTasks(baseSummary.getAvgTasks());
		newCondensedSummary.setExeuctionOrder(baseSummary.getExecutionOrder());
		newCondensedSummary.setStatus(baseSummary.getStatus());
		newCondensedSummary.setNumFailures(baseSummary.getNumFailures());
		if(baseSummary.getFirstReserved() != null && newCondensedSummary.getFirstReserved() != null){
			newCondensedSummary.setFirstReserved(baseSummary.getFirstReserved());
		}
		newCondensedSummary.setExpandable(true);
		return newCondensedSummary;
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
		String jobletType = params.required("jobletType");
		parallelJobletProcessors.restartExecutor(jobletType);
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	private Map<String,List<JobletExecutorThread>> getWaitingJobletThreads() {
		return getJobletThreads(parallelJobletProcessors.getCurrentlyWaitingJobletExecutorThreads());
	}

	private Map<String,List<JobletExecutorThread>> getRunningJobletThreads() {
		return getJobletThreads(parallelJobletProcessors.getCurrentlyRunningJobletExecutorThreads());
	}

	private Map<String,List<JobletExecutorThread>> getJobletThreads(List<JobletExecutorThread> jobletThreads){
		Map<String,List<JobletExecutorThread>> jobletThreadsByServer = new HashMap<>();
		jobletThreadsByServer.put(datarouter.getServerName(), jobletThreads);
		return jobletThreadsByServer;
	}

}
