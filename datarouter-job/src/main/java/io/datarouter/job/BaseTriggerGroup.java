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
package io.datarouter.job;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.core.util.CronExpression;

import io.datarouter.job.detached.DetachedJobResource;
import io.datarouter.job.lock.TriggerLockConfig;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.util.CronExpressionTool;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.string.StringTool;

public abstract class BaseTriggerGroup implements PluginConfigValue<BaseTriggerGroup>{

	public static final PluginConfigKey<BaseTriggerGroup> KEY = new PluginConfigKey<>(
			"triggerGroup",
			PluginConfigType.CLASS_LIST);

	private final String categoryName;
	private final List<BaseTriggerGroup> subGroups;
	private final List<JobPackage> jobPackages;
	private final Map<String,Class<? extends BaseJob>> requestTriggeredJobs;
	private final ZoneId zoneId;
	public final Tag tag;

	public BaseTriggerGroup(String categoryName, ZoneId zoneId){
		this(categoryName, Tag.APP, zoneId);
	}

	public BaseTriggerGroup(String categoryName, Tag tag, ZoneId zoneId){
		this.categoryName = categoryName;
		this.zoneId = zoneId;
		this.subGroups = new ArrayList<>();
		this.jobPackages = new ArrayList<>();
		this.requestTriggeredJobs = new HashMap<>();
		this.tag = tag;
	}

	protected void include(BaseTriggerGroup triggerGroup){
		subGroups.add(triggerGroup);
	}

	/*----------------- register -----------------*/

	protected void registerContinuous(
			String cronString,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass){
		registerParallel(cronString, shouldRunSupplier, jobClass);
	}

	protected void registerParallel(
			String cronString,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass){
		CronExpression cronExpression = CronExpressionTool.parse(cronString, zoneId);
		validateParallelTriggerCron(jobClass, cronExpression);
		jobPackages.add(JobPackage.createParallel(categoryName, cronExpression, shouldRunSupplier, jobClass));
	}

	protected void registerLocked(
			String cronString,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			boolean warnOnReachingDuration){
		CronExpression cronExpression = CronExpressionTool.parse(cronString, zoneId);
		Duration lockDuration = CronExpressionTool.durationBetweenNextTwoTriggers(cronExpression);
		registerLockedWithName(
				cronString,
				shouldRunSupplier,
				jobClass,
				lockName(jobClass),
				lockDuration,
				warnOnReachingDuration);
	}

	protected void registerLockedWithName(
			String cronString,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			String lockName,
			Duration lockDuration,
			boolean warnOnReachingDuration){
		CronExpression cronExpression = CronExpressionTool.parse(cronString, zoneId);
		validateLockedTriggerCron(jobClass, cronExpression);
		var triggerLockConfig = new TriggerLockConfig(
				lockName,
				cronExpression,
				lockDuration,
				warnOnReachingDuration);
		jobPackages.add(JobPackage.createWithLock(
				categoryName,
				cronExpression,
				shouldRunSupplier,
				jobClass,
				triggerLockConfig));
	}

	protected void registerDetached(
			String cronString,
			Supplier<Boolean> shouldRunSupplier,
			Class<? extends BaseJob> jobClass,
			boolean warnOnReachingDuration){
		registerDetached(cronString, shouldRunSupplier, jobClass, warnOnReachingDuration, null);
	}

	protected void registerDetached(
				String cronString,
				Supplier<Boolean> shouldRunSupplier,
				Class<? extends BaseJob> jobClass,
				boolean warnOnReachingDuration,
				DetachedJobResource detachedJobResource){
		CronExpression cronExpression = CronExpressionTool.parse(cronString, zoneId);
		validateLockedTriggerCron(jobClass, cronExpression);
		Duration lockDuration = CronExpressionTool.durationBetweenNextTwoTriggers(cronExpression);
		var triggerLockConfig = new TriggerLockConfig(
				lockName(jobClass),
				cronExpression,
				lockDuration,
				warnOnReachingDuration);
		jobPackages.add(JobPackage.createDetached(
				categoryName,
				cronExpression,
				shouldRunSupplier,
				jobClass,
				triggerLockConfig,
				detachedJobResource));
	}

	protected void registerRequestTriggered(
			String persistentString,
			Class<? extends BaseJob> jobClass){
		requestTriggeredJobs.put(persistentString, jobClass);
	}

	/*----------------- misc --------------------*/

	public List<JobPackage> getJobPackages(){
		List<JobPackage> jobPackagesIncludingSubGroups = new ArrayList<>();
		subGroups.stream()
				.map(BaseTriggerGroup::getJobPackages)
				.forEach(jobPackagesIncludingSubGroups::addAll);
		jobPackagesIncludingSubGroups.addAll(jobPackages);
		return jobPackagesIncludingSubGroups;
	}

	public Map<String,Class<? extends BaseJob>> getRequestTriggeredJobs(){
		return requestTriggeredJobs;
	}

	public static String lockName(Class<? extends BaseJob> jobClass){
		return jobClass.getSimpleName();
	}

	/*------------------ validate -----------------*/

	private void validateParallelTriggerCron(
			Class<? extends BaseJob> jobClass,
			CronExpression cronExpression){
		validateNoRepeatAfterWildcard(jobClass, cronExpression.getCronExpression());
		validateRepeatInterval(jobClass, cronExpression.getCronExpression());
	}

	private void validateLockedTriggerCron(
			Class<? extends BaseJob> jobClass,
			CronExpression cronExpression){
		validateNoRepeatAfterWildcard(jobClass, cronExpression.getCronExpression());
		validateRepeatInterval(jobClass, cronExpression.getCronExpression());
		validateMinTimeBetweenLockedTriggers(jobClass, cronExpression);
	}

	private void validateNoRepeatAfterWildcard(
			Class<? extends BaseJob> jobClass,
			String cronString){
		if(cronString.contains("/") && StringTool.getStringBeforeFirstOccurrence('/', cronString).contains("*")){
			String message = String.format(
					"cron %s for %s in %s contains a * before a /.  Please use exact times.",
					cronString,
					jobClass.getSimpleName(),
					getClass().getSimpleName());
			throw new IllegalArgumentException(message);
		}
	}

	private void validateRepeatInterval(
			Class<? extends BaseJob> jobClass,
			String cronString){
		String[] tokens = cronString.split(" ");
		String problem = null;
		if(CronExpressionTool.hasUnevenInterval(60, tokens[0])){
			problem = "second interval is not a factor of 60";
		}
		if(CronExpressionTool.hasUnevenInterval(60, tokens[1])){
			problem = "minute interval is not a factor of 60";
		}
		if(CronExpressionTool.hasUnevenInterval(24, tokens[2])){
			problem = "hour interval is not a factor of 24";
		}
		if(problem == null){
			return;
		}
		String message = String.format(
				"cron %s for %s in %s %s.",
				cronString,
				jobClass.getSimpleName(),
				getClass().getSimpleName(),
				problem);
		throw new IllegalArgumentException(message);
	}

	private void validateMinTimeBetweenLockedTriggers(
			Class<? extends BaseJob> jobClass,
			CronExpression cronExpression){
		Duration timeBetween = CronExpressionTool.durationBetweenNextTwoTriggers(cronExpression);
		if(ComparableTool.lt(timeBetween, TriggerLockConfig.MIN_PERIOD_BETWEEN_LOCKED_TRIGGERS)){
			String message = String.format(
					"cron %s for %s in %s is too frequent with %ss between triggers.  Please "
							+ "reschedule locked triggers at least %s seconds apart",
					cronExpression.getCronExpression(),
					jobClass.getSimpleName(),
					getClass().getSimpleName(),
					timeBetween.getSeconds(),
					TriggerLockConfig.MIN_PERIOD_BETWEEN_LOCKED_TRIGGERS.getSeconds());
			throw new IllegalArgumentException(message);
		}
	}

	@Override
	public PluginConfigKey<BaseTriggerGroup> getKey(){
		return KEY;
	}

}
