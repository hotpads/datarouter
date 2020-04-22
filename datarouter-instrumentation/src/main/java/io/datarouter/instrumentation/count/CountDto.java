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
package io.datarouter.instrumentation.count;

import java.util.Date;

public class CountDto{

	public final String name;
	public final String serviceName;
	public final Long periodMs;
	public final Date periodStartTime;
	public final Long periodStartMs;
	public final String serverName;
	public final Date created;
	public final Long value;

	public CountDto(String name, String serviceName, Long periodMs, Date periodStartTime, long periodStartMs,
			String serverName, Date created, Long value){
		this.name = name;
		this.serviceName = serviceName;
		this.periodMs = periodMs;
		this.periodStartTime = periodStartTime;
		this.periodStartMs = periodStartMs;
		this.serverName = serverName;
		this.created = created;
		this.value = value;
	}

}
