package com.hotpads.conveyor;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.hotpads.listener.DatarouterAppListener;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.concurrent.NamedThreadFactory;

public abstract class BaseConveyors extends DatarouterAppListener{

	private static final long DELAY_SEC = 3L;

	private final ThreadFactory threadFactory;
	private final NavigableMap<String,Pair<ExecutorService,Conveyor>> execsAndConveyorsByName;

	protected BaseConveyors(){
		this.threadFactory = new NamedThreadFactory(null, getClass().getSimpleName(), true);
		this.execsAndConveyorsByName = new TreeMap<>();
	}


	@Override
	protected void onShutDown(){
		for(Entry<String,Pair<ExecutorService,Conveyor>> entry : execsAndConveyorsByName.entrySet()){
			ExecutorService exec = entry.getValue().getLeft();
			exec.shutdownNow();//calls Thread.interrupt();
		}
	}


	protected void start(Conveyor conveyor){
		String name = conveyor.getName();
		Preconditions.checkArgument(execsAndConveyorsByName.get(name) == null, name + " already exists");
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(threadFactory);
		exec.scheduleWithFixedDelay(conveyor, DELAY_SEC, DELAY_SEC, TimeUnit.SECONDS);
		execsAndConveyorsByName.put(name, new Pair<>(exec, conveyor));
	}

}
