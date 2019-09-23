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
package io.datarouter.util.duration;

public enum DurationUnit{
	YEARS(0, "year"),
	MONTHS(1, "month"),
	DAYS(2, "day"),
	HOURS(3, "hour"),
	MINUTES(4, "minute"),
	SECONDS(5, "second"),
	MILLISECONDS(6, "millisecond"),
	;

	private final Integer index;
	private final String display;

	DurationUnit(Integer index, String display){
		this.index = index;
		this.display = display;
	}

	public Integer getIndex(){
		return index;
	}

	public String getDisplay(){
		return display;
	}

	public String getDisplayPlural(){
		return display + "s";
	}

}
