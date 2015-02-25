package com.hotpads.job.noop;

import java.util.Collections;
import java.util.Map;

import com.hotpads.job.trigger.Job;
import com.hotpads.job.trigger.TriggerGroup;

public class EmptyTriggerGroup implements TriggerGroup{

	@Override
	public Map<Class<? extends Job>, String> getJobClasses(){
		return Collections.emptyMap();
	}

}
