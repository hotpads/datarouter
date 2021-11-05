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
import java.util.Optional;

import j2html.tags.ContainerTag;

public interface DailyDigest{

	Comparator<DailyDigest> COMPARATOR = Comparator
			.comparing(DailyDigest::getGrouping)
			.thenComparing(DailyDigest::getTitle);

	Optional<ContainerTag<?>> getPageContent(ZoneId zoneId);
	Optional<ContainerTag<?>> getEmailContent(ZoneId zoneId);

	DailyDigestGrouping getGrouping();
	String getTitle();
	DailyDigestType getType();

	default String getId(){
		return getTitle().replace(" ", "_");
	}

	enum DailyDigestType{
		ACTIONABLE("Actionable"),
		SUMMARY("Summary"),
		;

		public final String display;

		DailyDigestType(String display){
			this.display = display;
		}

	}

}
