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
package io.datarouter.metric.counter.collection.archive;

import java.time.Duration;
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

import io.datarouter.instrumentation.count.CountCollectorPeriod;
import io.datarouter.metric.counter.setting.DatarouterCountSettingRoot;
import io.datarouter.util.DateTool;

public class CountArchiveFlusher{
	private static final Logger logger = LoggerFactory.getLogger(CountArchiveFlusher.class);

	//don't need the timeout if the underlying datarouter node can timeout
	private static final boolean FLUSH_WITH_TIMEOUT = false;

	private static final Duration INDIVIDUAL_FLUSH_ATTEMP_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration DISCARD_COUNTS_OLDER_THAN = Duration.ofMinutes(5);

	private final String name;
	private final long flushPeriodMs;
	private final Queue<CountCollectorPeriod> flushQueue;
	private final List<WritableCountArchive> archives;
	private final ScheduledExecutorService flushScheduler;
	private final ScheduledExecutorService flushExecutor;
	private final DatarouterCountSettingRoot countSettings;

	public CountArchiveFlusher(String name, long flushPeriodMs, ScheduledExecutorService flushScheduler,
			ScheduledExecutorService flushExecutor, DatarouterCountSettingRoot countSettings){
		this.name = name;
		this.flushPeriodMs = flushPeriodMs;
		this.countSettings = countSettings;
		this.flushQueue = new ArrayBlockingQueue<>(60);//careful, size() must iterate every element
		this.archives = new ArrayList<>();
		this.flushExecutor = flushExecutor;//won't be used if FLUSH_WITH_TIMEOUT=false
		this.flushScheduler = flushScheduler;
		logger.warn("CountArchiveFlusher:" + name + " started");
	}

	public void start(){
		this.flushScheduler.scheduleWithFixedDelay(new CountArchiveFlushUntilEmpty(this), 0, flushPeriodMs,
				TimeUnit.MILLISECONDS);
	}

	/*
	 * override shouldRun if custom logic required
	 */
	private boolean shouldRun(){
		return countSettings.saveCounts.get();
	}

	private static class CountArchiveFlushUntilEmpty implements Runnable{
		private final CountArchiveFlusher flusher;

		private CountArchiveFlushUntilEmpty(CountArchiveFlusher flusher){
			this.flusher = flusher;
		}

		//trying synchronized on this method to avoid the RejectedExecutionExceptions that never stop once they start
		@Override
		public void run(){
			try{
				if(flusher.flushQueue == null){
					return;
				}
				if(!flusher.shouldRun()){
					flusher.flushQueue.clear();
					return;
				}
				while(true){
					CountCollectorPeriod countMap = flusher.flushQueue.peek();//don't remove yet
					if(countMap == null){
						break;
					}
					long discardCutOff = System.currentTimeMillis() - DISCARD_COUNTS_OLDER_THAN.toMillis();
					if(countMap.getNextStartTimeMs() < discardCutOff){
						logger.warn("flusher:" + flusher.name + " discarded CountMapPeriod starting at "
								+ DateTool.getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(countMap.getStartTimeMs()));
						flusher.flushQueue.poll();//remove it
						continue;
					}
					CountArchiveFlushAttempt attempt = new CountArchiveFlushAttempt(flusher, countMap);
					if(FLUSH_WITH_TIMEOUT){
						ExecutorService flushExecutorDebug = flusher.flushExecutor;
						Future<?> future = flushExecutorDebug.submit(attempt);
						future.get(INDIVIDUAL_FLUSH_ATTEMP_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
					}else{
						attempt.run();
					}
				}
			}catch(TimeoutException te){
				logger.warn("TimeoutException after " + INDIVIDUAL_FLUSH_ATTEMP_TIMEOUT.toSeconds() + "seconds", te);
			}catch(Throwable e){
				logger.warn("", e);
			}
		}
	}


	private static class CountArchiveFlushAttempt implements Runnable{

		private final CountArchiveFlusher flusher;
		private final CountCollectorPeriod countMap;

		private CountArchiveFlushAttempt(CountArchiveFlusher flusher, CountCollectorPeriod countMap){
			this.flusher = flusher;
			this.countMap = countMap;
		}

		@Override
		public void run(){
			flusher.archives.forEach(archive -> archive.saveCounts(countMap));
			// actually remove from the queue after success
			flusher.flushQueue.poll();
		}
	}


	public void shutdownAndFlushAll(){
		logger.warn("shutting down CountArchiveFlusher " + name);
		flushScheduler.shutdown();
		new CountArchiveFlushUntilEmpty(this).run();
	}


	public void addArchive(WritableCountArchive archive){
		archives.add(archive);
	}

	public void offer(CountCollectorPeriod newCountMap){
		boolean accepted = flushQueue.offer(newCountMap);
		if(!accepted){
			logger.warn("flushQueue rejected our CountMapPeriod");
		}
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "[" + name + "]";
	}

}
