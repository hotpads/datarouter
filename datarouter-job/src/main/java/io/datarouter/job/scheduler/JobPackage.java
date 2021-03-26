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
package io.datarouter.job.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.core.util.CronExpression;

import io.datarouter.job.BaseJob;
import io.datarouter.job.lock.TriggerLockConfig;

//TODO Job system currently limited to one class simpleName
public class JobPackage implements Comparable<JobPackage>{

	private final Optional<CronExpression> cronExpression;

	public final String jobCategoryName;
	public final Supplier<Boolean> shouldRunSupplier;
	public final Class<? extends BaseJob> jobClass;
	public final Optional<TriggerLockConfig> triggerLockConfig;
	public final boolean shouldRunDetached;

	public static JobPackage createParallel(
			String jobCategoryName,
			CronExpression cronExpression,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass){
		return new JobPackage(jobCategoryName, cronExpression, shouldRunSupplier, jobClass, null);
	}

	public static JobPackage createWithLock(
			String jobCategoryName,
			CronExpression cronExpression,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			TriggerLockConfig triggerLockConfig){
		return new JobPackage(jobCategoryName, cronExpression, shouldRunSupplier, jobClass, triggerLockConfig);
	}

	public static JobPackage createDetached(
			String jobCategoryName,
			CronExpression cronExpression,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			TriggerLockConfig triggerLockConfig){
		return new JobPackage(jobCategoryName, cronExpression, shouldRunSupplier, jobClass, triggerLockConfig, true);
	}

	public static JobPackage createManualFromScheduledPackage(JobPackage scheduled){
		//if the job normally requires locking, then the manual trigger will respect that, but there is no deadline
		TriggerLockConfig manualTriggerLockConfig = scheduled.triggerLockConfig
				.map(trigger -> new TriggerLockConfig(trigger.jobName, null, Duration.ofSeconds(Long.MAX_VALUE), false))
				.orElse(null);
		return new JobPackage(scheduled.jobCategoryName, null, () -> true, scheduled.jobClass, manualTriggerLockConfig);
	}

	private JobPackage(
			String jobCategoryName,
			CronExpression cronExpression,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			TriggerLockConfig triggerLockConfig){
		this(jobCategoryName, cronExpression, shouldRunSupplier, jobClass, triggerLockConfig, false);
	}

	private JobPackage(
			String jobCategoryName,
			CronExpression cronExpression,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			TriggerLockConfig triggerLockConfig,
			boolean shouldRunDetached){
		this.jobCategoryName = jobCategoryName;
		this.cronExpression = Optional.ofNullable(cronExpression);
		this.shouldRunSupplier = shouldRunSupplier;
		this.jobClass = jobClass;
		this.triggerLockConfig = Optional.ofNullable(triggerLockConfig);
		this.shouldRunDetached = shouldRunDetached;
	}

	public boolean shouldRun(){
		return shouldRunSupplier.get();
	}

	public Optional<String> getCronExpressionString(){
		return cronExpression.map(CronExpression::toString);
	}

	public Optional<Date> getNextValidTimeAfter(Date date){
		return cronExpression.map(cronExpression -> cronExpression.getNextValidTimeAfter(date));
	}

	public boolean usesLocking(){
		return triggerLockConfig.isPresent();
	}

	public Optional<Instant> getSoftDeadline(Date triggerTime){
		return triggerLockConfig.map(config -> config.getSoftDeadline(triggerTime));
	}

	public Optional<Instant> getHardDeadline(Date triggerTime){
		return triggerLockConfig.map(config -> config.getHardDeadline(triggerTime));
	}

	public Optional<Boolean> getWarnOnReachingDuration(){
		return triggerLockConfig.map(config -> config.warnOnReachingMaxDuration);
	}

	@Override
	public int compareTo(JobPackage other){
		return jobClass.getSimpleName().compareTo(other.jobClass.getSimpleName());
	}

	@Override
	public int hashCode(){
		return jobClass.getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		JobPackage other = (JobPackage)obj;
		return 0 == compareTo(other);
	}

	@Override
	public String toString(){
		return jobClass.getSimpleName();
	}

}
