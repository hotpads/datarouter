/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory{

	protected String groupName;
	protected boolean makeDaemonsByDefault = true;
	protected ThreadGroup group;
    protected AtomicInteger threadNumber = new AtomicInteger(1);

    public NamedThreadFactory(ThreadGroup parentThreadGroup, String groupName, boolean makeDaemonsByDefault){
    	this.groupName = groupName;
    	this.makeDaemonsByDefault = makeDaemonsByDefault;
    	if(parentThreadGroup == null){
    		this.group = new ThreadGroup(groupName);
    	}else{
    		this.group = new ThreadGroup(parentThreadGroup, groupName);
    	}
    }

    @Override
	public Thread newThread(Runnable runnable){
		Thread thread = new Thread(group, runnable, groupName + "-" + threadNumber.getAndIncrement(), 0);
		thread.setDaemon(makeDaemonsByDefault);
        return thread;
    }

	public String getGroupName(){
		return groupName;
	}
}
