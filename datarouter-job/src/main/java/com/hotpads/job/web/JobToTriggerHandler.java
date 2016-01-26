package com.hotpads.job.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.DatarouterWebDispatcher;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.job.dispatcher.DatarouterJobDispatcher;
import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTask;
import com.hotpads.job.record.LongRunningTaskNodeProvider;
import com.hotpads.job.trigger.Job;
import com.hotpads.job.trigger.JobScheduler;
import com.hotpads.job.trigger.TriggerGroup;

public class JobToTriggerHandler extends BaseHandler {

	private static final Logger logger = LoggerFactory.getLogger(JobToTriggerHandler.class);

	public static final String
		P_name = "name",
		P_keyword = "keyword",
		P_category = "category",
		P_default = "bydefault",
		P_custom = "custom",
		P_hideDisabledJobs = "disabled",
		P_hideEnabledJobs = "enabled",

		V_jobs = "jobs",
		V_categoryOptions = "categoryOptions",

		JSP_triggers = "/jsp/admin/datarouter/job/triggers.jsp";

	@Inject
	private DatarouterInjector injector;
	@Inject
	private LongRunningTaskNodeProvider longRunningTaskNodeProvider;
	@Inject
	private JobScheduler jobScheduler;
	@Inject
	private JobCategory defaultJobCategory;

	@Override
	protected Mav handleDefault() {
		return list();
	}

	@Handler Mav list() {
		Mav mav = new Mav(JSP_triggers);

		Iterable<LongRunningTask> tasks = longRunningTaskNodeProvider.get().scan(null, Configs.slaveOk());
		Map<String, LongRunningTask> lastCompletions = new HashMap<>();
		Map<String, LongRunningTask> currentlyRunningTasks = new HashMap<>();
		for (LongRunningTask task : tasks){
			if (task.getJobExecutionStatus() == JobExecutionStatus.running
					&& (currentlyRunningTasks.get(task.getKey().getJobClass()) == null
					|| task.getStartTime().after(currentlyRunningTasks.get(task.getKey().getJobClass())
							.getStartTime()))){
				currentlyRunningTasks.put(task.getKey().getJobClass(), task);
			}
			if (task.getJobExecutionStatus() == JobExecutionStatus.success){
				if (lastCompletions.get(task.getKey().getJobClass()) == null
					|| task.getFinishTime().after(lastCompletions.get(task.getKey().getJobClass()).getFinishTime())){
					lastCompletions.put(task.getKey().getJobClass(), task);
				}
			}
		}

		String keyword = params.optional(P_keyword, null);
		String category = params.optional(P_category, null);
		List<Job> jobList = getTriggeredJobsListFiltered(keyword, category);
		mav.put(V_jobs, jobList);
		mav.put(V_categoryOptions, defaultJobCategory.getHtmlSelectOptions());
		mav.put("lastCompletions", lastCompletions);
		mav.put("currentlyRunningTasks", currentlyRunningTasks);
		return mav;
	}

	@Handler Mav run() throws Exception{
		Map<String, Job> jobMap = getTriggeredJobsMap();
		String jobKey = params.required(P_name);
		Job sampleJob = jobMap.get(jobKey);
		if(jobScheduler.getTracker().get(sampleJob.getClass()).isRunning()){
			return new MessageMav("Unable to run job, it is already running on this server");
		}
		jobScheduler.getTracker().get(sampleJob.getClass()).setRunning(true);
		jobScheduler.getTracker().get(sampleJob.getClass()).setJob(sampleJob);
		Date date = new Date();
		sampleJob.setTriggerTime(date);
		sampleJob.trackBeforeRun(System.currentTimeMillis());
		try{
			sampleJob.run();
		}catch(Exception e){
			jobScheduler.getTracker().get(sampleJob.getClass()).setRunning(false);
			throw e;
		}
		jobScheduler.getTracker().get(sampleJob.getClass()).setRunning(false);
		sampleJob.trackAfterRun(System.currentTimeMillis());
		long durationMs = System.currentTimeMillis() - date.getTime();
		logger.warn("Finished manual trigger of "+sampleJob.getClass().getSimpleName()+" in "+durationMs+"ms");
		return createRedirectMav();
	}

	@Handler Mav disable(){
		Map<String, Job> jobMap = getTriggeredJobsMap();
		String jobKey = params.required(P_name);
		Job sampleJob = jobMap.get(jobKey);
		sampleJob.disableJob();
		return createRedirectMav();
	}

	@Handler Mav enable(){
		Map<String, Job> jobMap = getTriggeredJobsMap();
		String jobKey = params.required(P_name);
		Job sampleJob = jobMap.get(jobKey);
		sampleJob.enableJob();
		return createRedirectMav();
	}

	@Handler Mav interrupt(){
		Map<String, Job> jobMap = getTriggeredJobsMap();
		String jobKey = params.required(P_name);
		Job sampleJob = jobMap.get(jobKey);
		jobScheduler.getTracker().get(sampleJob.getClass()).getJob().getLongRunningTaskTracker().requestStop();
		jobScheduler.getTracker().get(sampleJob.getClass()).setRunning(false);
		return createRedirectMav();
	}

	/***********helper************/

	public List<Job> getTriggeredJobsListFiltered(String keyword, String category) {
		List<Job> jobList = new ArrayList<>();
		Map<Class<? extends Job>, String> jobClasses = injector.getInstance(TriggerGroup.class).getJobClasses();
		for(Entry<Class<? extends Job>, String> entry : jobClasses.entrySet()){
			Job sampleJob = injector.getInstance(entry.getKey());
			Preconditions.checkNotNull(sampleJob, "injector couldn't find instance of "+entry.getKey().toString());
			if(DrStringTool.notEmpty(keyword)
					&& !entry.getKey().getSimpleName().toLowerCase().contains(keyword.toLowerCase())) {
				continue;
			}
			boolean defaultOff = DrStringTool.equalsCaseInsensitive(params.optional(P_default, ""), "");
			boolean customOff = DrStringTool.equalsCaseInsensitive(params.optional(P_custom, ""), "");
			boolean disabledOff = DrStringTool.equalsCaseInsensitive(params.optional(P_hideDisabledJobs, ""), "");
			boolean enabledOff = DrStringTool.equalsCaseInsensitive(params.optional(P_hideEnabledJobs, ""), "");

			boolean filterConditions =
					(defaultOff && !sampleJob.getIsCustom() || customOff && sampleJob.getIsCustom())
					&& (disabledOff && !sampleJob.shouldRun() || enabledOff && sampleJob.shouldRun());
			if(!filterConditions){
				continue;
			}
			if (DrStringTool.isEmpty(category)
					|| DrStringTool.equalsCaseInsensitive(category, defaultJobCategory.getPersistentString())){
				jobList.add(sampleJob);
			}else if (DrStringTool.equalsCaseInsensitive(sampleJob.getJobCategory(), category)){
				jobList.add(sampleJob);
			}
		}
		Collections.sort(jobList);
		return jobList;
	}

	public Map<String, Job> getTriggeredJobsMap(){
		Map<String, Job> jobMap = new HashMap<>();
		Map<Class<? extends Job>, String> jobClasses = injector.getInstance(TriggerGroup.class).getJobClasses();
		for(Entry<Class<? extends Job>, String> entry : jobClasses.entrySet()){
			Job sampleJob = injector.getInstance(entry.getKey());
			jobMap.put(sampleJob.getClass().getName(), sampleJob);
		}
		return jobMap;
	}

	private Mav createRedirectMav(){
		return new Mav(Mav.REDIRECT + servletContext.getContextPath() + DatarouterWebDispatcher.URL_DATAROUTER
				+ DatarouterJobDispatcher.TRIGGERS);
	}
}
