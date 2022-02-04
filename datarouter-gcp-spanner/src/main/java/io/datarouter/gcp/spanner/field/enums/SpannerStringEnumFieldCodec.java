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
package io.datarouter.gcp.spanner.field.enums;

import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Value;

import io.datarouter.enums.StringEnum;
import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.model.field.imp.enums.StringEnumField;

public class SpannerStringEnumFieldCodec<E extends StringEnum<E>> extends SpannerBaseFieldCodec<E,StringEnumField<E>>{

	public SpannerStringEnumFieldCodec(StringEnumField<E> field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.STRING;
	}

	@Override
	public Value getSpannerValue(){
		return Value.string(field.getValue().getPersistentString());
	}

	@Override
	public Builder setKey(Builder key){
		return key.append(field.getValue().getPersistentString());
	}

	@Override
	public E getValueFromResultSet(ResultSet rs){
		String value = rs.getString(field.getKey().getColumnName());
		return StringEnum.fromPersistentStringSafe(field.getSampleValue(), value);
	}

}
