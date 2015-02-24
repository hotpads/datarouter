package com.hotpads.job.trigger;

import java.util.Collections;
import java.util.Map;

public class EmptyTriggerGroup implements TriggerGroup{

	@Override
	public Map<Class<? extends Job>, String> getJobClasses(){
		return Collections.emptyMap();
	}

}
