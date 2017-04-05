package com.hotpads.job.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Configs;
import com.hotpads.datarouter.config.DatarouterProperties;
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
import com.hotpads.job.trigger.TriggerInfo;
import com.hotpads.job.web.TriggersRepository.JobPackage;
import com.hotpads.util.core.enums.EnumTool;

public class TriggerHandler extends BaseHandler {
	private static final Logger logger = LoggerFactory.getLogger(TriggerHandler.class);

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
	private TriggersRepository triggersRepository;
	@Inject
	private DatarouterProperties datarouterProperties;

	@Override
	protected Mav handleDefault() {
		return list();
	}

	@Handler
	public Mav list(){
		Mav mav = new Mav(JSP_triggers);
		mav.put("serverName", datarouterProperties.getServerName());
		Iterable<LongRunningTask> tasks = longRunningTaskNodeProvider.get().scan(null, Configs.slaveOk());
		Map<String, LongRunningTask> lastCompletions = new HashMap<>();
		Map<String, LongRunningTask> currentlyRunningTasks = new HashMap<>();
		for (LongRunningTask task : tasks){
			if (task.getJobExecutionStatus() == JobExecutionStatus.RUNNING
					&& (currentlyRunningTasks.get(task.getKey().getJobClass()) == null
					|| task.getStartTime().after(currentlyRunningTasks.get(task.getKey().getJobClass())
							.getStartTime()))){
				currentlyRunningTasks.put(task.getKey().getJobClass(), task);
			}
			if (task.getJobExecutionStatus() == JobExecutionStatus.SUCCESS){
				if (lastCompletions.get(task.getKey().getJobClass()) == null
					|| task.getFinishTime().after(lastCompletions.get(task.getKey().getJobClass()).getFinishTime())){
					lastCompletions.put(task.getKey().getJobClass(), task);
				}
			}
		}

		String keyword = params.optional(P_keyword).orElse("");
		Optional<JobCategory> category = triggersRepository.parseJobCategory(params.optional(P_category).orElse(null));
		List<Job> jobList = getTriggeredJobsListFiltered(keyword, category);
		mav.put(V_jobs, jobList);
		mav.put(V_categoryOptions, EnumTool.getHtmlSelectOptions(triggersRepository.getJobCategories()));
		mav.put("lastCompletions", lastCompletions);
		mav.put("currentlyRunningTasks", currentlyRunningTasks);
		return mav;
	}

	@Handler
	public Mav run() throws Exception{
		String jobKey = params.required(P_name);
		Job sampleJob = injector.getInstance(Class.forName(jobKey).asSubclass(Job.class));
		TriggerInfo triggerInfo = jobScheduler.getTracker().get(sampleJob.getClass());
		if(!triggerInfo.switchToRunning()){
			return new MessageMav("Unable to run job, it is already running on this server");
		}
		triggerInfo.setJob(sampleJob);
		Date date = new Date();
		sampleJob.setTriggerTime(date);
		sampleJob.trackBeforeRun(System.currentTimeMillis());
		try{
			sampleJob.run();
		}catch(Exception e){
			logger.error("cannot run job " + jobKey, e);
			throw e;
		}finally{
			triggerInfo.setRunning(false);
		}
		sampleJob.trackAfterRun(System.currentTimeMillis());
		long durationMs = System.currentTimeMillis() - date.getTime();
		logger.warn("Finished manual trigger of " + sampleJob.getClass().getSimpleName() + " in " + durationMs + "ms");
		return createRedirectMav();
	}

	@Handler
	public Mav disable() throws ClassNotFoundException{
		String jobKey = params.required(P_name);
		Job sampleJob = injector.getInstance(Class.forName(jobKey).asSubclass(Job.class));
		sampleJob.disableJob();
		return createRedirectMav();
	}

	@Handler
	public Mav enable() throws ClassNotFoundException{
		String jobKey = params.required(P_name);
		Job sampleJob = injector.getInstance(Class.forName(jobKey).asSubclass(Job.class));
		sampleJob.enableJob();
		return createRedirectMav();
	}

	@Handler
	public Mav interrupt() throws ClassNotFoundException{
		String jobKey = params.required(P_name);
		Job sampleJob = injector.getInstance(Class.forName(jobKey).asSubclass(Job.class));
		jobScheduler.getTracker().get(sampleJob.getClass()).getJob().getLongRunningTaskTracker().requestStop();
		jobScheduler.getTracker().get(sampleJob.getClass()).setRunning(false);
		return createRedirectMav();
	}

	/***********helper************/

	public List<Job> getTriggeredJobsListFiltered(String keyword, Optional<JobCategory> category) {
		final boolean defaultOff = DrStringTool.equalsCaseInsensitive(params.optional(P_default).orElse(""), "");
		final boolean customOff = DrStringTool.equalsCaseInsensitive(params.optional(P_custom).orElse(""), "");
		final boolean disabledOff = DrStringTool.equalsCaseInsensitive(params.optional(P_hideDisabledJobs).orElse(""),
				"");
		final boolean enabledOff = DrStringTool.equalsCaseInsensitive(params.optional(P_hideEnabledJobs).orElse(""),
				"");

		List<Job> jobList = new ArrayList<>();
		for(JobPackage jobPackage : triggersRepository.getJobPackages()){
			if(!jobPackage.jobClass.getSimpleName().toLowerCase().contains(keyword.toLowerCase())){
				continue;
			}
			if(category.isPresent() && !jobPackage.jobCategory.equals(category.get())){
				continue;
			}
			Job sampleJob = injector.getInstance(jobPackage.jobClass);
			boolean filterConditions = (defaultOff && !sampleJob.getIsCustom() || customOff && sampleJob.getIsCustom())
					&& (disabledOff && !sampleJob.shouldRun() || enabledOff && sampleJob.shouldRun());
			if(!filterConditions){
				continue;
			}
			jobList.add(sampleJob);
		}
		Collections.sort(jobList);
		return jobList;
	}

	private Mav createRedirectMav(){
		return new Mav(Mav.REDIRECT + servletContext.getContextPath() + DatarouterWebDispatcher.PATH_datarouter
				+ DatarouterJobDispatcher.TRIGGERS);
	}
}
