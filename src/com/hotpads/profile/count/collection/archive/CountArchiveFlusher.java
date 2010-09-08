package com.hotpads.profile.count.collection.archive;

import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.CountMapPeriod;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class CountArchiveFlusher{
	static Logger logger = Logger.getLogger(CountArchiveFlusher.class);
	
	public static long DISCARD_COUNTS_OLDER_THAN_MS = 5 * DateTool.MILLISECONDS_IN_MINUTE;
	
	protected String name;
	protected long flushPeriodMs;
	protected Queue<CountMapPeriod> flushQueue;
	protected List<CountArchive> archives;
	protected ScheduledExecutorService flushScheduler;
	protected ExecutorService flushExecutor;

	public CountArchiveFlusher(String name, long flushPeriodMs){
		this.name = name;
		this.flushPeriodMs = flushPeriodMs;
		this.flushQueue = new ConcurrentLinkedQueue<CountMapPeriod>();//careful, size() must iterate every element
		this.archives = ListTool.createArrayList();
		this.flushScheduler = Executors.newSingleThreadScheduledExecutor();
		this.flushScheduler.scheduleWithFixedDelay(
				new CountArchiveFlushUntilEmpty(this), 0, flushPeriodMs, TimeUnit.MILLISECONDS); 
		this.flushExecutor = Executors.newSingleThreadScheduledExecutor();
		logger.warn("CountArchiveFlusher:"+name+" started");
	}
	
	/*
	 * override shouldRun if custom logic required
	 */
	public boolean shouldRun(){
		return true;
	}
	
	
	public static class CountArchiveFlushUntilEmpty implements Runnable{
		protected final CountArchiveFlusher flusher;
		public CountArchiveFlushUntilEmpty(CountArchiveFlusher flusher){
			this.flusher = flusher;
		}

		@Override
		public void run(){
			try{
				if(flusher.flushQueue==null){ return; }
				if(!flusher.shouldRun()){ 
					flusher.flushQueue.clear();
					return; 
				}
				while(true){
					final CountMapPeriod countMap = flusher.flushQueue.peek();//don't remove yet
					if(countMap==null){ break; }
					long discardCutOff = System.currentTimeMillis() - DISCARD_COUNTS_OLDER_THAN_MS;
					if(countMap.getNextStartTimeMs() < discardCutOff){ 
						logger.warn(flusher.name+" discarded CountMapPeriod starting at "+new Date(countMap.getStartTimeMs()));
						flusher.flushQueue.poll();//remove it
						continue; 
					}
					Future<?> future = flusher.flushExecutor.submit(
							new CountArchiveFlushAttempt(flusher, countMap));
					future.get(1, TimeUnit.MINUTES);
				}
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
		}
	}
	
	
	public static class CountArchiveFlushAttempt implements Runnable{
		protected final CountArchiveFlusher flusher;
		protected final CountMapPeriod countMap;
		public CountArchiveFlushAttempt(CountArchiveFlusher flusher, CountMapPeriod countMap){
			this.flusher = flusher;
			this.countMap = countMap;
		}

		@Override
		public void run(){
			for(CountArchive archive : IterableTool.nullSafe(flusher.archives)){
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
		this.archives.add(archive);
	}
	
	public void offer(CountMapPeriod newCountMap){
		this.flushQueue.offer(newCountMap);
	}

	public List<CountArchive> getArchives(){
		return archives;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"["+name+"]";
	}
}
