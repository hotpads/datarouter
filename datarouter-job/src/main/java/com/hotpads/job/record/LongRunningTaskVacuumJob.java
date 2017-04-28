package com.hotpads.job.record;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.job.trigger.BaseJob;
import com.hotpads.job.trigger.JobEnvironment;
import com.hotpads.job.trigger.JobSettings;

public class LongRunningTaskVacuumJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(LongRunningTaskVacuumJob.class);

	public static final Integer NUM_TASKS_PER_CLASS = 5;

	private final DatarouterJobRouter datarouterJobRouter;
	private final JobSettings jobSettings;

	@Inject
	public LongRunningTaskVacuumJob(JobEnvironment jobEnvironment, JobSettings jobSettings,
			DatarouterJobRouter datarouterJobRouter){
		super(jobEnvironment);
		this.jobSettings = jobSettings;
		this.datarouterJobRouter = datarouterJobRouter;
	}

	@Override
	public boolean shouldRun(){
		return jobSettings.runLongRunningTaskVacuum.getValue();
	}

	@Override
	public void run(){
		// Clean all but last 5 triggered successful records for each task
		Map<String,Date> deleteBefore = new HashMap<>();
		ArrayListMultimap<String,Date> lastFiveRuns = ArrayListMultimap.create();
		int numItems = 0;
		for(LongRunningTask task : datarouterJobRouter.longRunningTask.scan(null, null)){
			if(tracker.heartbeat(numItems).isStopRequested()){
				return;
			}
			if(task.getJobExecutionStatus() == JobExecutionStatus.SUCCESS){
				int index = 0;
				for(Date date : lastFiveRuns.get(task.getKey().getJobClass())){
					if(date.before(task.getKey().getTriggerTime())){
						index++;
					}
				}
				lastFiveRuns.get(task.getKey().getJobClass()).add(index, task.getKey().getTriggerTime());
				if(lastFiveRuns.get(task.getKey().getJobClass()).size() > NUM_TASKS_PER_CLASS){
					lastFiveRuns.get(task.getKey().getJobClass()).remove(0);
				}
			}
			numItems++;
		}
		for(String key : lastFiveRuns.keySet()){
			deleteBefore.put(key, lastFiveRuns.get(key).get(0));
		}
		List<LongRunningTaskKey> deleteBatch = new ArrayList<>();
		int numTasksDeleted = 0;
		int numTasksConsidered = 0;
		for(LongRunningTask task : datarouterJobRouter.longRunningTask.scan(null, null)){
			if(tracker.heartbeat(numTasksConsidered).isStopRequested()){
				return;
			}
			if(deleteBefore.get(task.getKey().getJobClass()) == null){
				continue;
			}
			if(deleteBefore.get(task.getKey().getJobClass()).after(task.getKey().getTriggerTime())){
				numTasksConsidered++;
				deleteBatch.add(task.getKey());
				if(deleteBatch.size() >= 100){
					datarouterJobRouter.longRunningTask.deleteMulti(deleteBatch, null);
					numTasksDeleted = numTasksDeleted + deleteBatch.size();
					logger.warn("LongRunningTaskVacuumJob deleted " + DrNumberFormatter.addCommas(numTasksDeleted)
							+ " of " + DrNumberFormatter.addCommas(numTasksConsidered) + " LongRunningTasks");
					deleteBatch = new ArrayList<>();
				}
			}
		}
		datarouterJobRouter.longRunningTask.deleteMulti(deleteBatch, null);
		numTasksDeleted = numTasksDeleted + deleteBatch.size();
		logger.warn("LongRunningTaskVacuumJob deleted " + DrNumberFormatter.addCommas(numTasksDeleted) + " of "
				+ DrNumberFormatter.addCommas(numTasksConsidered) + " LongRunningTasks");

		// clean all but most recently triggered running task for each server
		Map<String,LongRunningTaskKey> mostRecentRunningTaskKeys = new HashMap<>();
		Iterable<LongRunningTask> tasks = datarouterJobRouter.longRunningTask.scan(null, null);
		for(LongRunningTask task : tasks){
			if(task.getJobExecutionStatus() == JobExecutionStatus.RUNNING){
				if(!mostRecentRunningTaskKeys.containsKey(task.getKey().getServerName() + task.getKey().getJobClass())){
					mostRecentRunningTaskKeys.put(task.getKey().getServerName() + task.getKey().getJobClass(), task
							.getKey());
					continue;
				}
				datarouterJobRouter.longRunningTask.delete(mostRecentRunningTaskKeys.get(task.getKey().getServerName()
						+ task.getKey().getJobClass()), null);
				mostRecentRunningTaskKeys.put(task.getKey().getServerName() + task.getKey().getJobClass(), task
						.getKey());
			}
		}
	}

}
