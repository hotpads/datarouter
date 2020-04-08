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
package io.datarouter.job.util;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.core.util.CronExpression;

import io.datarouter.util.string.StringTool;

public class CronExpressionTool{

	/*--------------- validate -------------------*/

	public static boolean hasUnevenInterval(int buckets, String cronPart){
		return parseInterval(cronPart)
				.map(interval -> buckets % interval != 0)
				.orElse(false);
	}

	/*--------------- parse -------------------*/

	public static CronExpression parse(String cronString){
		try{
			return new CronExpression(cronString);
		}catch(ParseException e){
			throw new IllegalArgumentException(e);
		}
	}

	private static Optional<Integer> parseInterval(String cronPart){
		String intervalString = StringTool.getStringAfterLastOccurrence("/", cronPart);
		return intervalString.isEmpty() ? Optional.empty() : Optional.of(Integer.valueOf(intervalString));
	}

	/*---------------- interval -----------------*/

	public static Duration durationBetweenNextTwoTriggers(String cronString){
		return durationBetweenNextTwoTriggers(parse(cronString));
	}

	public static Duration durationBetweenNextTwoTriggers(CronExpression cronExpression){
		Instant firstValidTime = cronExpression.getNextValidTimeAfter(new Date()).toInstant();
		Instant secondValidTime = cronExpression.getNextValidTimeAfter(Date.from(firstValidTime)).toInstant();
		return Duration.between(firstValidTime, secondValidTime);
	}

}