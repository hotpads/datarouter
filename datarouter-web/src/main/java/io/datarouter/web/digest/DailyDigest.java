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
package io.datarouter.web.digest;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;

public interface DailyDigest extends PluginConfigValue<DailyDigest>{

	PluginConfigKey<DailyDigest> KEY = new PluginConfigKey<>("dailyDigest", PluginConfigType.CLASS_LIST);

	Comparator<DailyDigest> COMPARATOR = Comparator.comparing(DailyDigest::getGrouping)
			.thenComparing(DailyDigest::getTitle);

	String getTitle();
	DailyDigestType getType();
	DailyDigestGrouping getGrouping();

	Optional<RmlBlock> getRelayContent(ZoneId zoneId);
	List<DailyDigestPlatformTask> getTasks(ZoneId zoneId);

	enum DailyDigestType{
		ACTIONABLE("Actionable"),
		STALE_TABLES("Stale Tables"),
		SUMMARY("Summary"),
		;

		public final String display;

		DailyDigestType(String display){
			this.display = display;
		}

	}

	@Override
	default PluginConfigKey<DailyDigest> getKey(){
		return KEY;
	}

	public record DailyDigestPlatformTask(
			List<String> key,
			List<String> group,
			String title,
			RmlBlock description){
	}

}
