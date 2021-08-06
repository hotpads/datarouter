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
package io.datarouter.util.time;

import java.time.ZoneId;
import java.util.List;

import io.datarouter.util.Java9;

public class ZoneIds{

	public static final ZoneId AMERICA_ANCHORAGE = ZoneId.of("America/Anchorage");
	public static final ZoneId AMERICA_CHICAGO = ZoneId.of("America/Chicago");
	public static final ZoneId AMERICA_DENVER = ZoneId.of("America/Denver");
	public static final ZoneId AMERICA_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
	public static final ZoneId AMERICA_NEW_YORK = ZoneId.of("America/New_York");

	public static final ZoneId EUROPE_PARIS = ZoneId.of("Europe/Paris");

	public static final ZoneId PACIFIC_HONOLULU = ZoneId.of("Pacific/Honolulu");

	public static final ZoneId UTC = ZoneId.of("UTC");

	public static final List<ZoneId> ZONE_IDS = Java9.listOf(
			AMERICA_ANCHORAGE,
			AMERICA_CHICAGO,
			AMERICA_DENVER,
			AMERICA_LOS_ANGELES,
			AMERICA_NEW_YORK,
			EUROPE_PARIS,
			PACIFIC_HONOLULU,
			UTC);

}
