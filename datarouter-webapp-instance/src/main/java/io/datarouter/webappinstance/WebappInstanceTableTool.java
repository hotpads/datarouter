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
package io.datarouter.webappinstance;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.util.DateTool;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.webappinstance.WebappInstanceTableService.WebappInstanceColumn;

public class WebappInstanceTableTool{

	public static <T> WebappInstanceColumn<T> buildDate(Function<T,Instant> getBuildDate, ZoneId zoneId){
		return new WebappInstanceColumn<T>("Build Date", inst -> ZonedDateFormatterTool
				.formatInstantWithZone(getBuildDate.apply(inst), zoneId))
				.withShowUsageStats(true)
				.withSortableValue(inst -> toSortable(getBuildDate.apply(inst)))
				.withTitle(inst -> ZonedDateFormatterTool.formatInstantWithZone(getBuildDate.apply(inst), zoneId))
				.withCellClass(
						"table-danger",
						inst -> WebappInstanceTableService.isOldWebappInstance(getBuildDate.apply(inst)))
				.withCellClass(
						"table-warning",
						inst -> WebappInstanceTableService.isStaleWebappInstance(getBuildDate.apply(inst)));
	}

	public static <T> WebappInstanceColumn<T> lastUpdated(Function<T,Instant> getLastUpdated){
		return new WebappInstanceColumn<T>("Last Updated", inst -> Optional.ofNullable(getLastUpdated.apply(inst))
				.map(DateTool::getAgoString)
				.orElse("inactive"))
				.withSortableValue(inst -> toSortable(getLastUpdated.apply(inst)))
				.withCellClass(
						"table-warning",
						inst -> WebappInstanceTableService.getHighlightRefreshedLast(getLastUpdated.apply(inst)));
	}

	public static <T> WebappInstanceColumn<T> uptime(Function<T,Instant> getBuildDate, ZoneId zoneId){
		return new WebappInstanceColumn<T>("Uptime", inst -> DateTool.getAgoString(getBuildDate.apply(inst)))
				.withSortableValue(inst -> toSortable(getBuildDate.apply(inst)))
				.withTitle(inst -> ZonedDateFormatterTool.formatInstantWithZone(getBuildDate.apply(inst), zoneId));
	}

	public static long toSortable(Instant instant){
		return Optional.ofNullable(instant)
				.map(Instant::toEpochMilli)
				.orElse(-1L);
	}

}
