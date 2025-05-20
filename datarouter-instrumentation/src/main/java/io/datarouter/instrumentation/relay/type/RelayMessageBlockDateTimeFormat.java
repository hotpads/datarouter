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
package io.datarouter.instrumentation.relay.type;

import io.datarouter.enums.StringMappedEnum;

public enum RelayMessageBlockDateTimeFormat{
	SHORT_DATE("short-date"),
	LONG_DATE("long-date"),
	SHORT_FULL_DATE_TIME("short-full-date-time"),
	LONG_FULL_DATE_TIME("long-full-date-time"),
	SHORT_GENERAL_DATE_TIME("short-general-date-time"),
	LONG_GENERAL_DATE_TIME("long-general-date-time"),
	MONTH_DAY("month-day"),
	RFC1123("rfc1123"),
	SORTABLE_DATE_TIME("sortable-date-time"),
	SHORT_TIME("short-time"),
	LONG_TIME("long-time"),
	YEAR_MONTH("year-month"),
	;

	public static final StringMappedEnum<RelayMessageBlockDateTimeFormat> BY_FORMAT
			= new StringMappedEnum<>(values(), el -> el.format);

	private final String format;

	RelayMessageBlockDateTimeFormat(String format){
		this.format = format;
	}

}
