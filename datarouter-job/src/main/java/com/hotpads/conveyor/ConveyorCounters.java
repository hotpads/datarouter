package com.hotpads.conveyor;

import com.hotpads.datarouter.profile.counter.Counters;

public class ConveyorCounters{

	private static final String PREFIX = "Conveyor";

	public static void inc(Conveyor conveyor, String action, long by){
		Counters.inc(PREFIX + " " + action, by);
		Counters.inc(PREFIX + " " + conveyor.getName() + " " + action, by);
	}

	public static void inc(DatabeanBuffer<?,?> buffer, String action, long by){
		Counters.inc(PREFIX + " buffer " + action, by);
		Counters.inc(PREFIX + " buffer " + buffer.getName() + " " + action, by);
	}

	public static void incPutMultiOpAndDatabeans(Conveyor conveyor, long numDatabeans){
		inc(conveyor, "putMulti ops", 1);
		inc(conveyor, "putMulti databeans", numDatabeans);
	}

	public static void incAck(Conveyor conveyor){
		inc(conveyor, "ack", 1);
	}

	public static void incException(Conveyor conveyor){
		inc(conveyor, "exception", 1);
	}

}
