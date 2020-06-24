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
package io.datarouter.instrumentation.schema;

import java.util.List;
import java.util.Optional;

public class FieldDto{

	public final String name;
	public final String type;
	public final boolean isKey;
	public final List<FieldAttributeDto> attributes;
	public final boolean isNullable;
	public final int order;
	public final boolean isFixedLength;
	public final Optional<Integer> size;

	public FieldDto(String name, String type, boolean isKey, List<FieldAttributeDto> attributes, boolean isNullable,
			int order, boolean isFixedLength, Optional<Integer> size){
		this.name = name;
		this.type = type;
		this.isKey = isKey;
		this.attributes = attributes;
		this.isNullable = isNullable;
		this.order = order;
		this.isFixedLength = isFixedLength;
		this.size = size;
	}

}
