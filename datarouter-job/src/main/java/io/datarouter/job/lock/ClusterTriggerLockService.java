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
package io.datarouter.job.lock;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.job.storage.clusterjoblock.ClusterJobLock;
import io.datarouter.job.storage.clusterjoblock.ClusterJobLockKey;
import io.datarouter.job.storage.clusterjoblock.DatarouterClusterJobLockDao;
import io.datarouter.job.storage.clustertriggerlock.ClusterTriggerLock;
import io.datarouter.job.storage.clustertriggerlock.ClusterTriggerLockKey;
import io.datarouter.job.storage.clustertriggerlock.DatarouterClusterTriggerLockDao;
import io.datarouter.job.util.Outcome;
import io.datarouter.model.databean.Databean;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.DateTool;

@Singleton
public class ClusterTriggerLockService{
	private static final Logger logger = LoggerFactory.getLogger(ClusterTriggerLockService.class);

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterClusterJobLockDao jobLockDao;
	@Inject
	private DatarouterClusterTriggerLockDao triggerLockDao;

	public Outcome acquireJobAndTriggerLocks(TriggerLockConfig triggerLockConfig, Date triggerTime, Duration delay){
		var jobLockAcquired = acquireJobLock(triggerLockConfig, triggerTime, delay);
		if(jobLockAcquired.failed()){
			return jobLockAcquired;
		}
		var triggerLockAcquired = acquireTriggerLock(triggerLockConfig, triggerTime);
		if(triggerLockAcquired.failed()){
			releaseJobLock(triggerLockConfig, triggerTime);//could contend with another server?
			return triggerLockAcquired;
		}
		return Outcome.success();
	}

	private Outcome acquireJobLock(TriggerLockConfig triggerLockConfig, Date triggerTime, Duration delay){
		logStrangeTriggerTime(triggerLockConfig.jobName, triggerTime);
		ClusterJobLock jobLock = toJobLock(triggerLockConfig, triggerTime);
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
			return Outcome.failure(reason + "exception= " + e);
		}
	}

	private Outcome acquireTriggerLock(TriggerLockConfig triggerLockConfig, Date triggerTime){
		ClusterTriggerLock triggerLock = toTriggerLock(triggerLockConfig, triggerTime);
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

	public void releaseJobLock(TriggerLockConfig triggerLockConfig, Date triggerTime){
		ClusterJobLock jobLock = toJobLock(triggerLockConfig, triggerTime);
		jobLockDao.delete(jobLock.getKey());
		logAction(triggerLockConfig.jobName, triggerTime, "released clusterJobLock");
	}

	/**
	 * Method is not transactionally safe.  Only use if an occasional extra unlock is ok.
	 */
	public void deleteJobLockIfExpired(String jobName){
		ClusterJobLockKey clusterJobLockKey = new ClusterJobLockKey(jobName);
		ClusterJobLock jobLock = jobLockDao.get(clusterJobLockKey);
		if(jobLock != null && DateTool.hasPassed(jobLock.getExpirationTime())){
			jobLockDao.delete(clusterJobLockKey);
			logger.warn("deleteIfExpired unlocked {}", jobLock.getClass().getName());
		}
	}

	public void releaseThisServersJobLocks(){
		jobLockDao.scan()
				.include(triggerLock -> triggerLock.getServerName().equals(datarouterProperties.getServerName()))
				.each(lock -> logger.info("releasing clusterJobLock {}", lock.getKey().getJobName()))
				.map(Databean::getKey)
				.forEach(jobLockDao::delete);
	}

	public void releaseTriggerLock(String jobName, Date triggerTime){
		ClusterTriggerLockKey key = new ClusterTriggerLockKey(jobName, triggerTime);
		triggerLockDao.delete(key);
		logger.info("releasing clusterTriggerLock {}, {}", key.getJobName(), key.getTriggerTime());
	}

	private ClusterJobLock toJobLock(TriggerLockConfig triggerLockConfig, Date triggerTime){
		return toTriggerLock(triggerLockConfig, triggerTime).toClusterJobLock();
	}

	private ClusterTriggerLock toTriggerLock(TriggerLockConfig triggerLockConfig, Date triggerTime){
		String lockName = triggerLockConfig.jobName;
		String serverName = datarouterProperties.getServerName();
		Instant deadline = triggerLockConfig.getSoftDeadline(triggerTime);//should we use hard or soft deadline?
		Date expirationTime = Date.from(deadline);
		return new ClusterTriggerLock(lockName, triggerTime, expirationTime, serverName);
	}

	private void logAction(String jobName, Date triggerTime, String action){
		String logName = jobName + "-" + DateTool.formatAlphanumeric(triggerTime.getTime());
		logger.info("{} {} lockId {}", datarouterProperties.getServerName(), action, logName);
	}

	private static void logStrangeTriggerTime(String jobName, Date triggerTime){
		long ms = triggerTime.getTime() % 1000;
		if(ms != 0){
			logger.info("{} had unexpected partial second triggerTime:{} with {}ms", jobName, triggerTime, ms);
		}
	}

}
