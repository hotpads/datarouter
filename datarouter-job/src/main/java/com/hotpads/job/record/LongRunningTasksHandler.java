package com.hotpads.job.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.hotpads.datarouter.config.Configs;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;

//TODO move to datarouter module
public class LongRunningTasksHandler extends BaseHandler {

	public static final int LRT_DURATION_THRESHOLD_SECONDS = 20;
	public static final String JSP_tasks = "/jsp/admin/datarouter/job/longRunningTasks.jsp";

	@Inject
	private LongRunningTaskNodeProvider longRunningTaskNodeProvider;


	@Override
	@Handler
	protected Mav handleDefault(){
		Mav mav = new Mav(JSP_tasks);
		Map<String, LongRunningTask> lastCompletions = new HashMap<>();
		Iterable<LongRunningTask> tasks = longRunningTaskNodeProvider.get().scan(null, Configs.slaveOk());
		List<LongRunningTask> currentlyRunningTasks = new ArrayList<>();
		for(LongRunningTask task : tasks){
			if(task.getJobExecutionStatus() == JobExecutionStatus.running){
				currentlyRunningTasks.add(task);
			}
			if(task.getJobExecutionStatus() == JobExecutionStatus.success){
				if(lastCompletions.get(task.getKey().getJobClass()) == null
						|| task.getFinishTime().after(lastCompletions.get(task.getKey().getJobClass())
								.getFinishTime())){
					lastCompletions.put(task.getKey().getJobClass(), task);
				}
			}
		}
		Collections.sort(currentlyRunningTasks, new LongRunningTaskDurationComparator(true));
		mav.put("lastCompletions", lastCompletions);
		mav.put("currentlyRunningTasks", currentlyRunningTasks);
		return mav;
	}
}
