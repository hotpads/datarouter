package com.hotpads.job.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.trigger.Job;
import com.hotpads.job.trigger.JobScheduler;
import com.hotpads.job.trigger.TriggerGroup;
import com.hotpads.util.core.date.CronExpression;

@Singleton
public class TriggersRepository{

	private final Map<String, JobCategory> jobCategoriesByPersistentString;
	private final Set<JobCategory> jobCategories;
	private final List<JobPackage> jobPackages;
	private final Map<Class<? extends Job>,JobPackage> jobPackagesByJobClass;

	private final JobScheduler scheduler;

	@Inject
	public TriggersRepository(JobScheduler scheduler){
		this.scheduler = scheduler;
		this.jobCategoriesByPersistentString = new HashMap<>();
		this.jobCategories = new HashSet<>();
		this.jobPackages = new ArrayList<>();
		this.jobPackagesByJobClass = new HashMap<>();
	}

	public void install(TriggerGroup triggerGroup){
		triggerGroup.makeJobPackages().forEach(this::register);
	}

	private void register(JobPackage jobPackage){
		jobCategories.add(jobPackage.jobCategory);
		jobCategoriesByPersistentString.put(jobPackage.jobCategory.getPersistentString(), jobPackage.jobCategory);
		jobPackages.add(jobPackage);
		jobPackagesByJobClass.put(jobPackage.jobClass, jobPackage);
		scheduler.scheduleJobPackage(jobPackage);
	}

	public Optional<JobCategory> parseJobCategory(String persistentString){
		return Optional.ofNullable(jobCategoriesByPersistentString.get(persistentString));
	}

	public JobPackage getPackageForClass(Class<? extends Job> jobClass){
		return jobPackagesByJobClass.get(jobClass);
	}

	public Set<JobCategory> getJobCategories(){
		return jobCategories;
	}

	public List<JobPackage> getJobPackages(){
		return jobPackages;
	}

	public static class JobPackage{

		public final JobCategory jobCategory;
		public final CronExpression cronExpression;
		public final Class<? extends Job> jobClass;

		public JobPackage(JobCategory jobCategory, CronExpression cronExpression, Class<? extends Job> jobClass){
			this.jobCategory = jobCategory;
			this.cronExpression = cronExpression;
			this.jobClass = jobClass;
		}

	}

}
