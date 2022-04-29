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
package io.datarouter.gcp.spanner.field;

import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Value;

import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.model.field.imp.StringEncodedField;

public class SpannerStringEncodedFieldCodec<T> extends SpannerBaseFieldCodec<T,StringEncodedField<T>>{

	public SpannerStringEncodedFieldCodec(StringEncodedField<T> field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.STRING;
	}

	@Override
	public Value getSpannerValue(){
		String stringValue = field.getCodec().encode(field.getValue());
		return Value.string(stringValue);
	}

	@Override
	public Builder setKey(Builder key){
		String stringValue = field.getCodec().encode(field.getValue());
		return key.append(stringValue);
	}

	@Override
	public T getValueFromResultSet(ResultSet rs){
		String columnName = field.getKey().getColumnName();
		String stringValue = rs.getString(columnName);
		return field.getCodec().decode(stringValue);
	}

}
