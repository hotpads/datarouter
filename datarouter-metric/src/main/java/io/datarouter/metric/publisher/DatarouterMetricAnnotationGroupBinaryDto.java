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
package io.datarouter.metric.publisher;

import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;

public class DatarouterMetricAnnotationGroupBinaryDto extends BinaryDto<DatarouterMetricAnnotationGroupBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String environment;
	@BinaryDtoField(index = 1)
	public final String serviceName;
	@BinaryDtoField(index = 2)
	public final String name;
	@BinaryDtoField(index = 3)
	public final String category;
	@BinaryDtoField(index = 4)
	public final String description;
	@BinaryDtoField(index = 5)
	public final String level;
	@BinaryDtoField(index = 6)
	public final Long timestamp;
	@BinaryDtoField(index = 7)
	public final String serverName;

	public DatarouterMetricAnnotationGroupBinaryDto(
			String environment,
			String serviceName,
			String name,
			String category,
			String description,
			String level,
			Long timestamp,
			String serverName){
		this.environment = environment;
		this.serviceName = serviceName;
		this.name = name;
		this.category = category;
		this.description = description;
		this.level = level;
		this.timestamp = timestamp;
		this.serverName = serverName;
	}
}
