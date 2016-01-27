package com.hotpads.util.core.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrNumberFormatter;

public class ThreadSafePhaseTimer extends PhaseRecord implements PhaseRecorder<ThreadSafePhaseTimer> {
	private List<PhaseRecord> phases = Collections.synchronizedList(new ArrayList<PhaseRecord>());
	private static final String DEFAULT_DELIM = "";
	public ThreadSafePhaseTimer() {
		super();
		record( "timer-start" );
	}
	public ThreadSafePhaseTimer(String name) {
		super(name);
		record( name+"-start");
	}
	
	/*********************** methods ****************************************/
	public ThreadSafePhaseTimer record(String eventName){
		PhaseRecord record = new PhaseRecord( eventName );
		phases.add(record);
		return this;
	}
			
	public int numEvents(){
		return this.phases.size();
	}
	
	public String toString(int showPhasesAtLeastThisMsLong){
		return toString(DEFAULT_DELIM,showPhasesAtLeastThisMsLong);
	}
	
	public String toString(){
		return toString(DEFAULT_DELIM,1);
	}

	public String toString(String delimiter){
		return toString(delimiter,1);
	}

	public String toString(String delimiter,int showPhasesAtLeastThisMsLong){
		StringBuilder sb = new StringBuilder();
		if (name != null){ 
			sb.append(name);
		}
		sb.append("[Total:")
			.append(DrNumberFormatter.addCommas(totalize(phases)))
			.append("ms and ")
			.append(phases.size())
			.append(" records]");
		Map<String,List<PhaseRecord>> threads = buildThreadMap();
		for (String thread : threads.keySet()) {
			List<PhaseRecord> phases = threads.get(thread);
			long elapsed = totalize(phases);
			long previous = this.getTime();
			PhaseRecord phase = phases.get(0);
			if (!phase.getThreadId().equals(this.getThreadId())) {
				previous = phase.time;
			}
			if (threads.size() > 1 ) {
				sb.append(delimiter)
				.append( "[" )
				.append( thread )
				.append(":total:")
				.append(DrNumberFormatter.addCommas(elapsed))
				.append("ms]"); 
			}
			for(int i=0; i < phases.size(); ++i){
				phase = phases.get(i);
				long diff = phase.time - previous;
				if (diff >= showPhasesAtLeastThisMsLong) {
					sb.append(delimiter).append( "[" );
					if (threads.size() > 1) {
						sb.append(thread)
						.append(":");
					}
					sb.append(phase.name)
					.append(":")
					.append(DrNumberFormatter.addCommas(diff))
					.append("ms]");
					previous = phase.time;
				}
			}
		}
		return sb.toString();
	}

	private void addRecords( Map<Long,List<PhaseRecord>> tree, List<PhaseRecord> phases ) {
		for (PhaseRecord record : phases ) {
			List<PhaseRecord> list = tree.get( record.time );
			if ( list == null ) {
				list = new ArrayList<>();
				tree.put( record.time, list );
			}
			list.add( record );
		}
	}
	
	/**
	 * Guaranteed to create valid merge but concurrent updates of phases might get lost obviously.
	 * We don't need to support that use case.
	 */
	public void merge(ThreadSafePhaseTimer other) {
		if (other == null || other == this || other.phases.isEmpty() ) {
			return;
		}
		Map<Long,List<PhaseRecord>> tree = new TreeMap<>();
		addRecords(tree,other.phases);
		addRecords(tree,this.phases);
		List<PhaseRecord> merged = Collections
				.synchronizedList(new ArrayList<PhaseRecord>());
		for (List<PhaseRecord> list: tree.values()) {
			merged.addAll(list);
		};
		this.phases = merged;
	}
	
	private long totalize( List<PhaseRecord> phases ) {
		if ( phases.size() == 0 ) {
			return 0L;
		}
		long min = this.getTime();
		if(!phases.get(0).threadId.equals( this.threadId )) {
			min = phases.get(0).getTime();
		}
		long max = min;
		for ( PhaseRecord p : phases ) {
			if ( p.time > max ) {
				max = p.time;
			}
		}
		return max - min;
	}
	
	private Map<String,List<PhaseRecord>> buildThreadMap() {
		Map<String,List<PhaseRecord>> result = new LinkedHashMap<String, List<PhaseRecord>>();
		for (int i = 0;i < phases.size(); i++) {
			PhaseRecord phase = phases.get(i);
			String threadId = phase.getThreadId();
			List<PhaseRecord> threadEvents = result.get( threadId );
			if ( threadEvents == null ) {
				threadEvents = new ArrayList<PhaseRecord>();
				result.put( threadId, threadEvents );
			}
			threadEvents.add( phase );
		}
		return result;
	}
		
	public static class SafeTimerTests {
		private class TestThread extends Thread {
			private ThreadSafePhaseTimer timer;
			private String name;
			private AtomicBoolean complete = new AtomicBoolean();
		    public TestThread(ThreadSafePhaseTimer timer, String str) {
		    	super(str);
		    	this.name = str;
		    	this.timer = timer;
		    	timer.record( name + "-start" );
		    }
		    public void run() {
				for (int i = 0; i < 5; i++) {
		            try {
		            	int time = (int)(Math.random() * 200); 
		            	sleep(time);
		            	timer.record( name + "-awoke step " + i + " slept " + time );
		            } catch (InterruptedException e) {
		            	break;
		            }
				}
				timer.record( name + "-complete" );	
				complete.set(true);
			}
		    public boolean isComplete() {
		    	return complete.get();
		    }
		}

		@Test
		public void testThreads() throws Exception{
			ThreadSafePhaseTimer timer1 = new ThreadSafePhaseTimer("TestOnly");
			ThreadSafePhaseTimer timer2 = new ThreadSafePhaseTimer("Timer2");
			
			String[] names = { "A1", "B2", "B1", "C1", "C2", "D2" };
			List<TestThread> threads = new ArrayList<>();
			for ( String name : names ) {
				if (name.contains( "2" )) {
					threads.add( new TestThread(timer2, "Timer2 thread: " + name ) );
				} else {
					threads.add( new TestThread(timer1, "Timer1 thread: " + name ) );
				}
			}
			for ( Thread thread : threads ) {
				thread.start();
			}
			int totalDone = 0;
			while (totalDone < threads.size()) {
				totalDone = 0;

				for (TestThread thread : threads) {
					if (thread.isComplete()) {
						totalDone++;
					} else {
						thread.timer.record("main-loop waited");
						Thread.sleep( 100L );
						break;
					}
				}
			}
			System.out.println( "Here is the timer1 output:\n" + timer1.toString("\n",0) );
			System.out.println( "Here is the timer2 output:\n" + timer2.toString("\n",0) );
			int t1Size = timer1.phases.size();
			int t2Size = timer2.phases.size();
			timer1.merge( timer2 );
			System.out.println( "Here is the time1 with timer2 merged:\n" + timer1.toString("\n",0) );
			System.out.println( "Note any 0ms times will not be shown typically" );
			Assert.assertTrue( "Merged timer should contain all original records", 
					timer1.phases.size() == t1Size + t2Size );
		}

	}	
	
}
