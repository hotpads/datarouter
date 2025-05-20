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
package io.datarouter.metric.publisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationConstants.MetricInstrumentationConstants;
import io.datarouter.util.string.StringTool;

public class MetricSanitizer{
	private static final Logger logger = LoggerFactory.getLogger(MetricSanitizer.class);

	public static boolean shouldReject(String name){
		if(name.contains("\n") || name.contains("\t")){
			Exception stackTrace = new IllegalArgumentException();
			logger.warn("discarding bad count name={}", name, stackTrace);
			return true;
		}
		return false;
	}

	public static String sanitizeName(String name){
		String pruned = StringTool.removeNonStandardCharacters(name);
		int max = MetricInstrumentationConstants.MAX_SIZE_METRIC_NAME;
		if(pruned.length() <= max){
			return pruned;
		}
		String placeholder = "trimmed";
		String output = pruned.substring(0, max - placeholder.length()) + placeholder;
		logger.warn("trimmed input={}, output={}", name, output);
		return output;
	}

	public static long sanitizeTime(long timestamp){
		Instant now = Instant.now();
		Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES);
		if(timestamp >= fiveMinutesAgo.toEpochMilli() && timestamp <= now.toEpochMilli()){
			return timestamp;
		}
		return Instant.now().toEpochMilli();
	}
}
