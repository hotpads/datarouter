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
package io.datarouter.instrumentation.tablecount;

import java.util.Date;

public class TableCountDto{

	public final String serviceName;
	public final String clientName;
	public final String tableName;
	public final Long numRows;
	public final Date dateUpdated;
	public final Long countTimeMs;
	public final Long numSpans;
	public final Long numSlowSpans;

	public TableCountDto(
			String serviceName,
			String clientName,
			String tableName,
			Long numRows,
			Date dateUpdated,
			Long countTimeMs,
			Long numSpans,
			Long numSlowSpans){
		this.serviceName = serviceName;
		this.clientName = clientName;
		this.tableName = tableName;
		this.numRows = numRows;
		this.dateUpdated = dateUpdated;
		this.countTimeMs = countTimeMs;
		this.numSpans = numSpans;
		this.numSlowSpans = numSlowSpans;
	}

}
