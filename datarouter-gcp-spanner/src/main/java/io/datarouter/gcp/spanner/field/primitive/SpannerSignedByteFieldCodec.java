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
package io.datarouter.gcp.spanner.field.primitive;

import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Value;

import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.model.field.imp.comparable.SignedByteField;

public class SpannerSignedByteFieldCodec extends SpannerBaseFieldCodec<Byte,SignedByteField>{

	public SpannerSignedByteFieldCodec(SignedByteField field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.INT64;
	}

	@Override
	public Value getSpannerValue(){
		return Value.int64(field.getValue());
	}

	@Override
	public Builder setKey(Builder key){
		return key.append(field.getValue());
	}

	@Override
	public Byte getValueFromResultSet(ResultSet rs){
		Long value = rs.getLong(field.getKey().getColumnName());
		return value.byteValue();
	}

}
