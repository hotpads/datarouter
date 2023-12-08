/*
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
package io.datarouter.job.lock;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.job.storage.joblock.DatarouterJobLockDao;
import io.datarouter.job.storage.joblock.JobLock;
import io.datarouter.job.storage.joblock.JobLockKey;
import io.datarouter.job.storage.triggerlock.DatarouterTriggerLockDao;
import io.datarouter.job.storage.triggerlock.TriggerLock;
import io.datarouter.job.storage.triggerlock.TriggerLockKey;
import io.datarouter.job.util.Outcome;
import io.datarouter.model.databean.Databean;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.types.MilliTime;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TriggerLockService{
	private static final Logger logger = LoggerFactory.getLogger(TriggerLockService.class);

	private static final String FORMAT_STRING = "yyyy'y'MM'm'dd'd'HH'h'mm'm'ss's'SSS'ms'";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(FORMAT_STRING);

	@Inject
	private DatarouterJobLockDao jobLockDao;
	@Inject
	private DatarouterTriggerLockDao triggerLockDao;
	@Inject
	private ServerName serverName;

	public Outcome acquireJobAndTriggerLocks(TriggerLockConfig triggerLockConfig, Instant triggerTime, Duration delay){
		var jobLockAcquired = acquireJobLock(triggerLockConfig, triggerTime, delay);
		if(jobLockAcquired.failed()){
			return jobLockAcquired;
		}
		var triggerLockAcquired = acquireTriggerLock(triggerLockConfig, triggerTime);
		if(triggerLockAcquired.failed()){
			releaseJobLock(triggerLockConfig.jobName);//could contend with another server?
			return triggerLockAcquired;
		}
		return Outcome.success();
	}

	private Outcome acquireJobLock(TriggerLockConfig triggerLockConfig, Instant triggerTime, Duration delay){
		logStrangeTriggerTime(triggerLockConfig.jobName, triggerTime);
		JobLock jobLock = toJobLock(triggerLockConfig, triggerTime);
		try{
			jobLockDao.putAndAcquire(jobLock);
			logAction(triggerLockConfig.jobName, triggerTime, "acquired clusterJobLock, delay=" + delay.toMillis()
					+ "ms");
			return Outcome.success();
		}catch(Exception e){
			String reason;
			try{
				reason = "JobLock already acquired by: " + jobLockDao.get(jobLock.getKey()).getServerName();
			}catch(Exception ex){
				reason = "Unable to acquire JobLock.";
			}
			return Outcome.failure(reason + " exception=\"" + e + "\"");
		}
	}

	private Outcome acquireTriggerLock(TriggerLockConfig triggerLockConfig, Instant triggerTime){
		TriggerLock triggerLock = toTriggerLock(triggerLockConfig, triggerTime);
		try{
			triggerLockDao.putAndAcquire(triggerLock);
			return Outcome.success();
		}catch(Exception e){
			logAction(triggerLockConfig.jobName, triggerTime, "did not acquire clusterTriggerLock");
			String reason;
			try{
				reason = "ClusterTriggerLock already acquired by: " + triggerLockDao
						.get(triggerLock.getKey()).getServerName();
			}catch(Exception ex){
				reason = "Unable to acquire ClusterTriggerLock.";
			}
			return Outcome.failure(reason + " exception= " + e);
		}
	}

	public void releaseJobLock(String jobName){
		var jobLockKey = new JobLockKey(jobName);
		jobLockDao.delete(jobLockKey);
		logAction(jobName, "released clusterJobLock");
	}

	/**
	 * Method is not transactionally safe.  Only use if an occasional extra unlock is ok.
	 */
	public void deleteJobLockIfExpired(String jobName){
		var clusterJobLockKey = new JobLockKey(jobName);
		var jobLock = jobLockDao.get(clusterJobLockKey);
		if(jobLock != null && MilliTime.now().isAfter(jobLock.getExpirationTime())){
			jobLockDao.delete(clusterJobLockKey);
			logger.warn("deleteIfExpired unlocked {}", jobLock.getClass().getName());
		}
	}

	public void releaseThisServersJobLocks(){
		jobLockDao.scan()
				.include(triggerLock -> triggerLock.getServerName().equals(serverName.get()))
				.each(lock -> logger.info("releasing clusterJobLock {}", lock.getKey().getJobName()))
				.map(Databean::getKey)
				.forEach(jobLockDao::delete);
	}

	public void releaseTriggerLock(String jobName, Instant triggerTime){
		var key = new TriggerLockKey(jobName, MilliTime.of(triggerTime));
		triggerLockDao.delete(key);
		logger.info("releasing clusterTriggerLock {}, {}", key.getJobName(), key.getTriggerTime());
	}

	public void tryReleasingJobAndTriggerLocks(TriggerLockConfig triggerLockConfig, Instant triggerTime){
		try{
			releaseTriggerLock(triggerLockConfig.jobName, triggerTime);
		}catch(Exception e){
			logger.warn("failed to release clusterTriggerLock {} - {}", triggerLockConfig.jobName, triggerTime, e);
		}
		try{
			releaseJobLock(triggerLockConfig.jobName);
		}catch(Exception e){
			logger.warn("failed to release jobLock for {}", triggerLockConfig.jobName, e);
		}
	}

	// To be used by detached-jobs once the detach job starts
	public void forceAcquireJobLock(TriggerLockConfig triggerLockConfig, Instant triggerTime, Duration delay){
		jobLockDao.find(new JobLockKey(triggerLockConfig.jobName))
				.ifPresentOrElse(
						jobLock -> jobLockDao.forcePut(new JobLock(
								jobLock.getKey().getJobName(),
								jobLock.getTriggerTime(),
								jobLock.getExpirationTime(),
								serverName.get())),
						() -> acquireJobLock(triggerLockConfig, triggerTime, delay));
	}

	private JobLock toJobLock(TriggerLockConfig triggerLockConfig, Instant triggerTime){
		return toTriggerLock(triggerLockConfig, triggerTime).toJobLock();
	}

	private TriggerLock toTriggerLock(TriggerLockConfig triggerLockConfig, Instant triggerTime){
		MilliTime dateTriggerTime = MilliTime.of(triggerTime);
		String lockName = triggerLockConfig.jobName;
		Instant deadline = triggerLockConfig.getSoftDeadline(triggerTime);//should we use hard or soft deadline?
		MilliTime expirationTime = MilliTime.of(deadline);
		return new TriggerLock(lockName, dateTriggerTime, expirationTime, serverName.get());
	}

	private void logAction(String jobName, Instant triggerTime, String action){
		String logName = jobName + "-" + formatTime(triggerTime);
		logAction(logName, action);
	}

	private void logAction(String logName, String action){
		logger.info("{} {} lockId {}", serverName.get(), action, logName);
	}

	private static void logStrangeTriggerTime(String jobName, Instant triggerTime){
		long ms = triggerTime.toEpochMilli() % 1000;
		if(ms != 0){
			logger.info("{} had unexpected partial second triggerTime:{} with {}ms", jobName, triggerTime, ms);
		}
	}

	public static String formatTime(Instant instant){
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(FORMATTER);
	}

}
