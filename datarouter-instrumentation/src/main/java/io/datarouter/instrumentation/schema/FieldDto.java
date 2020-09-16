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
import java.util.Objects;
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

	public FieldDto(
			String name,
			String type,
			boolean isKey,
			List<FieldAttributeDto> attributes,
			boolean isNullable,
			int order,
			boolean isFixedLength,
			Optional<Integer> size){
		this.name = name;
		this.type = type;
		this.isKey = isKey;
		this.attributes = attributes;
		this.isNullable = isNullable;
		this.order = order;
		this.isFixedLength = isFixedLength;
		this.size = size;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof FieldDto){
			FieldDto that = (FieldDto)obj;
			return this.name.equals(that.name)
					&& this.type.equals(that.type)
					&& this.isKey == that.isKey
					&& this.attributes.equals(that.attributes)
					&& this.isNullable == that.isNullable
					&& this.order == that.order
					&& this.isFixedLength == that.isFixedLength
					&& this.size.equals(that.size);
		}else{
			return false;
		}
	}

	@Override
	public int hashCode(){
		return Objects.hash(
				this.name,
				this.type,
				this.isKey,
				this.attributes,
				this.isNullable,
				this.order,
				this.isFixedLength,
				this.size);
	}

}
