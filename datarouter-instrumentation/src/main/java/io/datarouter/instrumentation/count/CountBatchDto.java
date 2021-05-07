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

import java.util.Map;

public class CountBatchDto{

	public final Long id;
	public final String serviceName;
	public final String serverName;
	public final Map<Long,Map<String,Long>> counts;

	public CountBatchDto(Long id, String serviceName, String serverName, Map<Long,Map<String,Long>> counts){
		this.id = id;
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.counts = counts;
	}

}
