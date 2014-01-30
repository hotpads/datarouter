package com.hotpads.job.trigger;

import java.util.Map;

public interface TriggerGroup{

	Map<Class<? extends Job>, String> getJobClasses();
	
}
