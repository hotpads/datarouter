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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.number.NumberFormatter;

public class ThreadSafePhaseTimer extends PhaseRecord implements PhaseRecorder<ThreadSafePhaseTimer>{

	private List<PhaseRecord> phases = Collections.synchronizedList(new ArrayList<PhaseRecord>());
	private static final String DEFAULT_DELIM = "";

	public ThreadSafePhaseTimer(){
		record("timer-start");
	}

	public ThreadSafePhaseTimer(String name){
		super(name);
		record(name + "-start");
	}

	@Override
	public ThreadSafePhaseTimer record(String eventName){
		PhaseRecord record = new PhaseRecord(eventName);
		phases.add(record);
		return this;
	}

	public void end(){
		record("end");
	}

	public String toString(int showPhasesAtLeastThisMsLong){
		return toString(DEFAULT_DELIM,showPhasesAtLeastThisMsLong);
	}

	@Override
	public String toString(){
		return toString(DEFAULT_DELIM,1);
	}

	public String toString(String delimiter,int showPhasesAtLeastThisMsLong){
		StringBuilder sb = new StringBuilder();
		if(name != null){
			sb.append(name);
		}
		sb.append("[Total:")
				.append(NumberFormatter.addCommas(totalize(phases)))
				.append("ms and ")
				.append(phases.size())
				.append(" records]");
		Map<String,List<PhaseRecord>> threads = buildThreadMap();


		for(String thread : threads.keySet()){
			List<PhaseRecord> phases = threads.get(thread);
			long elapsed = totalize(phases);
			PhaseRecord phase = phases.get(0);
			long previous = phase.time;

			if(threads.size() > 0){
				sb.append(delimiter)
						.append("[")
						.append(thread)
						.append(":total:")
						.append(NumberFormatter.addCommas(elapsed))
						.append("ms");
			}
			String delim = " - ";
			for(int i = 0; i < phases.size(); ++i){
				phase = phases.get(i);
				long diff = phase.time - previous;
				if(diff >= showPhasesAtLeastThisMsLong){
					sb.append(delimiter).append(delim);
					delim = " ";
					sb.append(phase.name)
							.append(":")
							.append(NumberFormatter.addCommas(diff))
							.append("ms");
					previous = phase.time;
				}
			}
			sb.append("]");
		}
		return sb.toString();
	}

	private int insertSingleRecord(PhaseRecord record, int fromPosition){
		while(fromPosition < phases.size()){
			PhaseRecord current = phases.get(fromPosition);
			if(record.time > current.time){
				fromPosition++;
			}else{
				break;
			}
		}
		phases.add(fromPosition++,record);
		return fromPosition;
	}

	public void merge(ThreadSafePhaseTimer other){
		if(other == null || other == this || other.phases.isEmpty()){
			return;
		}
		int otherIndex = 0;
		int position = 0;
		// Avoid use of iterators => ConcurrentModificationException e.g. when other is updated by another thread.
		while(otherIndex < other.phases.size()){
			position = insertSingleRecord(other.phases.get(otherIndex++), position);
		}
	}

	private long totalize(List<PhaseRecord> phases){
		if(phases.size() == 0){
			return 0L;
		}
		long min = 0;
		long max = 0;

		for(int index = 0; index < phases.size(); index++){
			PhaseRecord record = phases.get(index);
			if(record.time > max){
				max = record.time;
			}
			if(record.time < min || min == 0){
				min = record.time;
			}
		}
		return max - min;
	}

	private Map<String,List<PhaseRecord>> buildThreadMap(){
		Map<String,List<PhaseRecord>> result = new LinkedHashMap<>();
		for(int i = 0; i < phases.size(); i++){
			PhaseRecord phase = phases.get(i);
			String threadId = phase.getThreadId();
			List<PhaseRecord> threadEvents = result.get(threadId);
			if(threadEvents == null){
				threadEvents = new ArrayList<>();
				result.put(threadId, threadEvents);
			}
			threadEvents.add(phase);
		}
		return result;
	}

	public static class SafeTimerTests{

		private class TestThread extends Thread{
			private ThreadSafePhaseTimer timer;
			private String name;
			private AtomicBoolean complete = new AtomicBoolean();

			public TestThread(ThreadSafePhaseTimer timer, String str){
				super(str);
				this.name = str;
				this.timer = timer;
				timer.record(name + "-start");
			}

			@Override
			public void run(){
				for(int i = 0; i < 5; i++){
					try{
						int time = (int)(Math.random() * 200);
						sleep(time);
						timer.record(name + "-awoke step " + i + " slept " + time);
					}catch(InterruptedException e){
						break;
					}
				}
				timer.record(name + "-complete");
				complete.set(true);
			}

			public boolean isComplete(){
				return complete.get();
			}
		}

		@Test
		public void testThreads() throws Exception{
			ThreadSafePhaseTimer timer1 = new ThreadSafePhaseTimer("TestOnly");
			ThreadSafePhaseTimer timer2 = new ThreadSafePhaseTimer("Timer2");

			String[] names = {"A1", "B2", "B1", "C1", "C2", "D2"};
			List<TestThread> threads = new ArrayList<>();
			for(String name : names){
				if(name.contains("2")){
					threads.add(new TestThread(timer2, "Timer2 thread: " + name));
				}else{
					threads.add(new TestThread(timer1, "Timer1 thread: " + name));
				}
			}
			for(Thread thread : threads){
				thread.start();
			}
			int totalDone = 0;
			while(totalDone < threads.size()){
				totalDone = 0;
				for(TestThread thread : threads){
					if(thread.isComplete()){
						totalDone++;
					}else{
						thread.timer.record("main-loop waited");
						Thread.sleep(100L);
						break;
					}
				}
			}
			int t1Size = timer1.phases.size();
			int t2Size = timer2.phases.size();
			timer1.merge(timer2);
			Assert.assertEquals(timer1.phases.size(), t1Size + t2Size,
					"Merged timer should contain all original records");
		}

	}

}
