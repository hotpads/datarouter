package com.hotpads.job.trigger;

import java.util.Map;

import com.google.common.base.Preconditions;

public interface TriggerGroup{

	Map<Class<? extends Job>, String> getJobClasses();

	default void addAndVerifyNotDuplicate(String cronExpression, Class<? extends Job> job){
		Preconditions.checkArgument(!getJobClasses().containsKey(job));
		getJobClasses().put(job, cronExpression);
	}

}
