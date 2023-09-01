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
package io.datarouter.web.html.form;

import java.time.ZoneId;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.time.ZoneIds;

public class HtmlFormTimezoneSelect extends HtmlFormSelect{

	public static final String TIMEZONE_FIELD_NAME = "timezone";
	public static final String TIMEZONE_JS = """
			document.addEventListener("DOMContentLoaded", function(){
					const fieldEl = document.getElementsByName("%s")[0];
					if (!fieldEl.querySelector("[selected]")) {
						fieldEl.value = Intl.DateTimeFormat().resolvedOptions().timeZone;
					}
			});""".formatted(TIMEZONE_FIELD_NAME);

	public static final String HIDDEN_TIMEZONE_JS = """
			document.addEventListener("DOMContentLoaded", function(){
					const fieldEl = document.getElementsByName("%s")[0];
					if (!fieldEl.value) {
						fieldEl.value = Intl.DateTimeFormat().resolvedOptions().timeZone;
					}
			});""".formatted(TIMEZONE_FIELD_NAME);

	public HtmlFormTimezoneSelect(){
		withName(TIMEZONE_FIELD_NAME);
		withValues(Scanner.of(ZoneIds.ZONE_IDS)
				.map(ZoneId::getId)
				.sort()
				.list());
	}
}
