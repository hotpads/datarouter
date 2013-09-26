package com.hotpads.job.trigger;

import java.util.List;

public interface TriggerGroup{

	List<Class<? extends Job>> getJobClasses();
	
}
