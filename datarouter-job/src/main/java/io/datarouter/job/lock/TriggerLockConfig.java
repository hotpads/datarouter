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
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.core.util.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerLockConfig{
	private static final Logger logger = LoggerFactory.getLogger(TriggerLockConfig.class);

	// ask the job to stop before the next trigger, otherwise kill it at the next trigger time
	public static final Duration GRACEFUL_STOP_WINDOW = Duration.ofSeconds(5);
	// this should be longer than the GRACEFUL_STOP_WINDOW to allow the job to run before being stopped
	public static final Duration MIN_PERIOD_BETWEEN_LOCKED_TRIGGERS = Duration.ofSeconds(10);
	// this limit is based on MySQL maximum DATETIME of '9999-12-31 23:59:59'
	protected static final Instant MAX_DATE_INSTANT = Instant.ofEpochSecond(253402300799L);
	protected static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE);

	public final String jobName;
	private final Optional<CronExpression> cronExpression;
	private final Optional<Duration> customMaxDuration;
	public final boolean warnOnReachingMaxDuration;

	public TriggerLockConfig(
			String name,
			CronExpression cronExpression,
			Duration customMaxDuration,
			boolean warnOnReachingMaxDuration){
		this.jobName = name;
		this.cronExpression = Optional.ofNullable(cronExpression);
		this.customMaxDuration = Optional.ofNullable(customMaxDuration);
		this.warnOnReachingMaxDuration = warnOnReachingMaxDuration;
	}

	public Instant getSoftDeadline(Instant triggerTime){
		return safePlus(triggerTime, getSoftMaxDuration(triggerTime));
	}

	public Instant getHardDeadline(Instant triggerTime){
		return safePlus(triggerTime, getHardMaxDuration(triggerTime));
	}

	private Duration getSoftMaxDuration(Instant triggerTime){
		return getHardMaxDuration(triggerTime).minus(GRACEFUL_STOP_WINDOW);
	}

	private Duration getHardMaxDuration(Instant triggerTime){
		return customMaxDuration.orElseGet(
				() -> cronExpression.map(exp -> exp.getNextValidTimeAfter(Date.from(triggerTime)))
						.map(Date::toInstant)
						.map(instant -> {
							Duration duration = Duration.between(triggerTime, instant);
							logger.debug("hard max duration nextValidTime={} duration={}",
									instant,
									duration);
							return duration;
						})
						.orElse(MAX_DURATION));
	}

	//this prevents overflows of MAX_DATE_INSTANT
	protected static Instant safePlus(Instant instant, Duration duration){
		if(instant.isAfter(MAX_DATE_INSTANT)){
			instant = MAX_DATE_INSTANT;
		}
		long secDifferenceBetweenMaxAndDuration = MAX_DURATION.minus(duration).getSeconds();
		if(instant.getEpochSecond() > secDifferenceBetweenMaxAndDuration){
			return MAX_DATE_INSTANT;
		}
		return instant.plus(duration);
	}

}
