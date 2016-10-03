package com.hotpads.conveyor;

import com.hotpads.datarouter.profile.counter.Counters;

public class ConveyorCounters{

	private static final String PREFIX = "Conveyor";

	public static void inc(Conveyor conveyor, String action, long by){
		Counters.inc(PREFIX + " " + conveyor.getName() + " " + action, by);
	}

}
