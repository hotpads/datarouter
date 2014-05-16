package com.hotpads.job.trigger;

import java.util.Map;

public interface TriggerGroup{

	Map<Class<? extends BaseJob>, String> getJobClasses();
	
}
