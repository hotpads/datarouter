package com.hotpads.joblet.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.InContextRedirectMav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.joblet.JobletExecutorThread;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.JobletSettings;
import com.hotpads.joblet.JobletStatus;
import com.hotpads.joblet.JobletType;
import com.hotpads.joblet.JobletTypeFactory;
import com.hotpads.joblet.ParallelJobletProcessors;
import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletQueue;
import com.hotpads.joblet.databean.JobletQueueKey;
import com.hotpads.joblet.dto.JobletSummary;
import com.hotpads.joblet.hibernate.DeleteTimedOutJoblets;
import com.hotpads.joblet.hibernate.ResetJobletQueueTickets;
import com.hotpads.joblet.hibernate.RestartJoblets;
import com.hotpads.joblet.hibernate.TimeoutStuckRunningJoblets;

public class JobletHandler extends BaseHandler{

	public static final String
		ACTION_showJoblets = "showJoblets",

		PARAM_ref = "ref",
		PARAM_whereStatus = "whereStatus",
		PARAM_diff = "diff",
		PARAM_numTickets = "numTickets",
		PARAM_threadStatus = "threadStatus",
		PARAM_expanded = "expanded",

		URL_JOBLETS_IN_CONTEXT = DatarouterJobDispatcher.URL_DATAROUTER + DatarouterJobDispatcher.JOBLETS,
		JSP_joblets = "/jsp/joblet/joblets.jsp",
		JSP_queueSummary = "/jsp/joblet/queueSummary.jsp",
		JSP_threads = "/jsp/joblet/threads.jsp",
	 	JSP_exceptions = "/jsp/joblet/jobletExceptions.jsp";

	private final Datarouter datarouter;
	private final JobletTypeFactory jobletTypeFactory;
	private final JobletNodes jobletNodes;
	private final ParallelJobletProcessors parallelJobletProcessors;
	private final JobletService jobletDao;
	private final JobletSettings jobletSettings;

	@Inject
	public JobletHandler(Datarouter datarouter, JobletTypeFactory jobletTypeFactory, JobletNodes jobletNodes,
			ParallelJobletProcessors parallelJobletProcessors, JobletService jobletDao, JobletSettings jobletSettings){
		this.datarouter = datarouter;
		this.jobletTypeFactory = jobletTypeFactory;
		this.jobletNodes = jobletNodes;
		this.parallelJobletProcessors = parallelJobletProcessors;
		this.jobletDao = jobletDao;
		this.jobletSettings = jobletSettings;
	}

	@Override
	@Handler
	protected Mav handleDefault(){
		return showJoblets();
	}

	@Handler
	protected Mav showJoblets(){
		Mav mav = new Mav(JSP_joblets);
		mav = populateMav(mav);
		String whereStatus = params.optional(PARAM_whereStatus, null);
		boolean expanded = params.optionalBoolean(PARAM_expanded, false);
		if(DrStringTool.notEmpty(whereStatus)){
			mav.put(PARAM_whereStatus, whereStatus);
		}
		mav.put("whereStatus", whereStatus);
		List<JobletSummary> summaries = jobletDao.getJobletSummariesForTable(whereStatus, true);
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

	private Mav populateMav(Mav mav){
		mav.put("minServers", jobletSettings.getMinJobletServers().getValue());
		mav.put("maxServers", jobletSettings.getMaxJobletServers().getValue());
		mav.put("jobletStatuses", JobletStatus.values());
		List<JobletQueue> queues = DrListTool.createArrayList(jobletNodes.jobletQueue().scan(null, null));
		Collections.sort(queues, new JobletQueue.NumTicketsComparator());
		mav.put("queues", queues);
		int totalTickets = JobletQueue.sumNumTickets(queues);
		mav.put("totalTickets", totalTickets);
		Map<String,JobletQueue> queuesById = JobletQueue.getById(queues);
		mav.put("queuesById", queuesById);
		mav.put("runningJobletThreads", getRunningJobletThreads());
		mav.put("waitingJobletThreads", getWaitingJobletThreads());
		return mav;
	}

	@Handler
	protected Mav listExceptions(){
		Mav mav = new Mav(JSP_exceptions);
		List<JobletRequest> failedJoblets = new ArrayList<>();
		for(JobletRequest joblet : jobletNodes.joblet().scan(null, null)){
			if(joblet.getStatus() == JobletStatus.failed){
				failedJoblets.add(joblet);
			}
		}
		mav.put("failedJoblets", failedJoblets);
		return mav;
	}

	@Handler
	protected Mav restartFailed(){
		int numRestarted = datarouter.run(new RestartJoblets(datarouter, jobletNodes, JobletStatus.failed));
		return new MessageMav("restarted "+numRestarted);
	}

	@Handler
	protected Mav restartTimedOut(){
		int numRestarted = datarouter.run(new RestartJoblets(datarouter, jobletNodes, JobletStatus.timedOut));
		return new MessageMav("restarted "+numRestarted);
	}

	@Handler
	protected Mav timeoutStuckRunning(){
		int numTimedOut = datarouter.run(new TimeoutStuckRunningJoblets(datarouter, jobletNodes));
		return new MessageMav("timedOut "+numTimedOut);
	}

	@Handler
	protected Mav deleteTimedOutJoblets(){
		int numDeleted = datarouter.run(new DeleteTimedOutJoblets(datarouter, jobletNodes));
		return new MessageMav("deleted "+numDeleted);
	}

	@Handler
	protected Mav resetQueueTickets(){
		int numUpdated = datarouter.run(new ResetJobletQueueTickets(datarouter, jobletNodes));
		return new MessageMav("updated "+numUpdated);
	}

	@Handler
	protected Mav showQueues(){
		Mav mav = new Mav(JSP_queueSummary);
		List<JobletQueue> queues = DrListTool.createArrayList(jobletNodes.jobletQueue().scan(null, null));
		Collections.sort(queues, new JobletQueue.NumTicketsComparator());
		mav.put("queues", queues);
		int totalTickets = JobletQueue.sumNumTickets(queues);
		mav.put("totalTickets", totalTickets);
		return mav;
	}

	@Handler
	protected Mav showThreads(){
		Mav mav = new Mav(JSP_threads);
		mav.put("runningJobletThreads", getRunningJobletThreads());
		mav.put("waitingJobletThreads", getWaitingJobletThreads());
		return mav;
	}

	@Handler
	protected Mav alterQueueNumTickets(){
		Mav mav = new Mav();
		String ref = params.optional(PARAM_ref, null);
		JobletQueue queue = jobletNodes.jobletQueue().get(
				new JobletQueueKey(params.required( "queueId")), null);
		Integer numTickets = params.optionalInteger(PARAM_numTickets, null);
		if(numTickets != null){
			queue.setNumTickets(numTickets);
		}
		Integer diff = params.optionalInteger(PARAM_diff, 0);
		if(diff != null){
			queue.setNumTickets(queue.getNumTickets() + diff);
		}
		jobletNodes.jobletQueue().put(queue, null);
		if(ACTION_showJoblets.equals(ref)){
			mav.setViewName(Mav.REDIRECT + params.getContextPath() + URL_JOBLETS_IN_CONTEXT);
		}else{
			mav.setViewName(Mav.REDIRECT + params.getContextPath() + URL_JOBLETS_IN_CONTEXT
					+ "?submitAction=showQueues");
		}
		return mav;
	}

	@Handler
	protected Mav killJobletThread(){
		long threadId = params.requiredLong("threadId");
		parallelJobletProcessors.killThread(threadId);
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	@Handler
	protected Mav restartExecutor(){
		String jobletType = params.optional("jobletType", null);
		parallelJobletProcessors.restartExecutor(jobletType);
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	@Handler
	protected Mav runOrDeleteJobletByDataId(){
		//scan the Joblet table to get the matching joblet
		Long jobletDataId = DrNumberTool.getLongNullSafe(params.optional("jobletDataId", ""),null);
		boolean delete = params.optionalBoolean("delete", false);

		List<JobletRequest> joblets = DrListTool.createArrayList(jobletNodes.joblet().scan(null, null));
		JobletRequest joblet = findJobletWithId(joblets, jobletDataId);
		if(delete){
			jobletNodes.joblet().delete(joblet.getKey(), null);
		}else{
			// parallelJobletProcessors.restartExecutor(joblet.getTypedPersistentString());
			JobletData jobletData = jobletDao.getJobletData(joblet);
			JobletType<?> jobletType = jobletTypeFactory.fromJoblet(joblet);
			JobletPackage jobletPackage = new JobletPackage(joblet, jobletData);
			parallelJobletProcessors.getMap().get(jobletType).getJobletScheduler().submitJoblet(jobletPackage);
		}
		return new InContextRedirectMav(params, URL_JOBLETS_IN_CONTEXT);
	}

	private JobletRequest findJobletWithId(Collection<JobletRequest> joblets, Long jobletDataId){
		for(JobletRequest joblet : joblets){
			if(joblet.getJobletDataId().equals(jobletDataId)){
				return joblet;
			}
		}
		return null;
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
