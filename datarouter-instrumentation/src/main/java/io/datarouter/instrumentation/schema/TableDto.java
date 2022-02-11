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
package io.datarouter.instrumentation.schema;

import java.time.Instant;
import java.util.List;

public class TableDto{

	public final String serviceName;
	public final String clientName;
	public final String clientType;
	public final String tableName;
	public final List<FieldDto> fields;
	public final List<List<String>> sampleData;
	public final Instant created;
	@Deprecated
	public final Boolean isSystemTable;

	public TableDto(
			String serviceName,
			String clientName,
			String clientType,
			String tableName,
			List<FieldDto> fields,
			List<List<String>> sampleData,
			Instant created,
			Boolean isSystemTable){
		this.serviceName = serviceName;
		this.clientName = clientName;
		this.clientType = clientType;
		this.tableName = tableName;
		this.fields = fields;
		this.sampleData = sampleData;
		this.created = created;
		this.isSystemTable = isSystemTable;
	}

}
