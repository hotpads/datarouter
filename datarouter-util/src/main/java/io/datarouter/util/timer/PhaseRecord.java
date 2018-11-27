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
package io.datarouter.util.timer;

public class PhaseRecord{

	protected String name;
	protected String threadId;
	protected long time;
	protected long duration = 0L;

	public static final String makeThreadId(long threadId){
		return "T" + threadId;
	}

	public PhaseRecord(){
		this.threadId = makeThreadId(Thread.currentThread().getId());
		this.name = threadId;
		this.time = System.currentTimeMillis();
	}

	public PhaseRecord(String name){
		this.name = name == null ? "" : name;
		this.threadId = makeThreadId(Thread.currentThread().getId());
		this.time = System.currentTimeMillis();
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getThreadId(){
		return threadId;
	}

	public void setThreadId(String threadId){
		this.threadId = threadId;
	}

	public long getTime(){
		return time;
	}

	public void setTime(long time){
		this.time = time;
	}

	public long getElapsedTime(){
		return System.currentTimeMillis() - time;
	}

	@Override
	public String toString(){
		return threadId + (name.length() == 0 ? "" : (":" + name) + " @" + time);
	}

}
