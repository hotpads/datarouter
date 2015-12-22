package com.hotpads.util.core.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	
	protected String groupName;
	protected boolean makeDaemonsByDefault = true;
	protected ThreadGroup group;
    protected  AtomicInteger threadNumber = new AtomicInteger(1);

    public NamedThreadFactory(ThreadGroup parentThreadGroup, String groupName, boolean makeDaemonsByDefault){
    	this.groupName = groupName;
    	this.makeDaemonsByDefault = makeDaemonsByDefault;
    	if(parentThreadGroup==null){
    		this.group = new ThreadGroup(groupName);
    	}else{
    		this.group = new ThreadGroup(parentThreadGroup, groupName);
    	}
    }

    public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(group, runnable, groupName + "-" + threadNumber.getAndIncrement(), 0);
		thread.setDaemon(makeDaemonsByDefault);
        return thread;
    }

	public String getGroupName(){
		return groupName;
	}
}
