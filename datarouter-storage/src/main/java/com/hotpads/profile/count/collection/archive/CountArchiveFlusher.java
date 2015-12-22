package com.hotpads.profile.count.collection.archive;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.profile.ProfilingSettings;
import com.hotpads.profile.count.collection.CountCollectorPeriod;

public class CountArchiveFlusher{
	private static final Logger logger = LoggerFactory.getLogger(CountArchiveFlusher.class);
	
	//don't need the timeout if the underlying datarouter node can timeout
	private static final boolean FLUSH_WITH_TIMEOUT = false;
	
	private static long 
		INDIVIDUAL_FLUSH_ATTEMP_TIMEOUT_SECONDS = 10,
		DISCARD_COUNTS_OLDER_THAN_MS = 5 * DrDateTool.MILLISECONDS_IN_MINUTE;
	
	public static final String
		NAME_MEMORY = "memory";
	
	
	private final String name;
	private final long flushPeriodMs;
	private final Queue<CountCollectorPeriod> flushQueue;
	private final List<CountArchive> archives;
	private final ScheduledExecutorService flushScheduler;
	//not sure why this has to be a ScheduledExecutor, but it won't work otherwise
	private final ScheduledExecutorService flushExecutor;
	private final ProfilingSettings profilingSettings;

	public CountArchiveFlusher(String name, long flushPeriodMs, ScheduledExecutorService flushScheduler,
			ScheduledExecutorService flushExecutor, ProfilingSettings profilingSettings){
		this.name = name;
		this.flushPeriodMs = flushPeriodMs;
		this.profilingSettings = profilingSettings;
		this.flushQueue = new ArrayBlockingQueue<>(60);//careful, size() must iterate every element
		this.archives = new ArrayList<>();
		this.flushExecutor = flushExecutor;//won't be used if FLUSH_WITH_TIMEOUT=false
		this.flushScheduler = flushScheduler;
		logger.warn("CountArchiveFlusher:" + name + " started");
	}
	
	
	public void start(){
		this.flushScheduler.scheduleWithFixedDelay(
				new CountArchiveFlushUntilEmpty(this), 0, flushPeriodMs, TimeUnit.MILLISECONDS); 
	}
	
	/*
	 * override shouldRun if custom logic required
	 */
	public boolean shouldRun(){
		return profilingSettings.getSaveCounts().getValue();
	}
	
	public static class CountArchiveFlushUntilEmpty implements Runnable{
		private final CountArchiveFlusher flusher;
		
		public CountArchiveFlushUntilEmpty(CountArchiveFlusher flusher){
			this.flusher = flusher;
		}

		//trying synchronized on this method to avoid the RejectedExecutionExceptions that never stop once they start
		@Override
		public /*synchronized*/ void run(){
			try{
				if(flusher.flushQueue == null) {
					return;
				}
				if(!flusher.shouldRun()){ 
					flusher.flushQueue.clear();
					return; 
				}
				while(true){
					final CountCollectorPeriod countMap = flusher.flushQueue.peek();//don't remove yet
					if(countMap == null) {
						break;
					}
					long discardCutOff = System.currentTimeMillis() - DISCARD_COUNTS_OLDER_THAN_MS;
					if(countMap.getNextStartTimeMs() < discardCutOff){ 
						logger.warn("flusher:"+flusher.name+" discarded CountMapPeriod starting at "
								+DrDateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(countMap.getStartTimeMs()));
						flusher.flushQueue.poll();//remove it
						continue; 
					}
//					logger.warn("submitting CountMapPeriod starting at "
//							+DateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(countMap.getStartTimeMs()));
					CountArchiveFlushAttempt attempt = new CountArchiveFlushAttempt(flusher, countMap);
					if(FLUSH_WITH_TIMEOUT){
						ExecutorService flushExecutorDebug = flusher.flushExecutor;
						Future<?> future = flushExecutorDebug.submit(attempt);
						future.get(INDIVIDUAL_FLUSH_ATTEMP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
					}else{
						attempt.run();
					}
				}
			}catch(TimeoutException te){
				logger.warn("TimeoutException after "+INDIVIDUAL_FLUSH_ATTEMP_TIMEOUT_SECONDS+" seconds", te);
			}catch(Exception e){
				logger.warn("", e);
			}
		}
	}
	
	
	public static class CountArchiveFlushAttempt implements Runnable{
		private final CountArchiveFlusher flusher;
		private final CountCollectorPeriod countMap;
		
		public CountArchiveFlushAttempt(CountArchiveFlusher flusher, CountCollectorPeriod countMap){
			this.flusher = flusher;
			this.countMap = countMap;
		}

		@Override
		public void run(){
			for(CountArchive archive : flusher.archives){
				//TODO if only some of the tables complete, then we may get double counting on the next loop
				//... probably unlikely though since all archives in a flusher should be related somehow
				archive.saveCounts(countMap);
			}
			flusher.flushQueue.poll();//actually remove from the queue after success
		}
	}
	
	
	public void shutdownAndFlushAll(){
		logger.warn("shutting down CountArchiveFlusher "+name);
		flushScheduler.shutdown();
		new CountArchiveFlushUntilEmpty(this).run();
	}
	
	
	public void addArchive(CountArchive archive){
		archives.add(archive);
	}
	
	public void offer(CountCollectorPeriod newCountMap){
		boolean accepted = flushQueue.offer(newCountMap);
		if(!accepted){
			logger.warn("flushQueue rejected our CountMapPeriod");
		}
	}

	public List<CountArchive> getArchives(){
		return archives;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"["+name+"]";
	}
}
