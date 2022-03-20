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
package io.datarouter.gcp.spanner.field.list;

import java.util.List;

import com.google.cloud.ByteArray;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Value;

import io.datarouter.bytes.ByteTool;
import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.model.field.imp.list.ByteListField;

@Deprecated//use ByteArrayField
public class SpannerByteListFieldCodec extends SpannerBaseFieldCodec<List<Byte>,ByteListField>{

	public SpannerByteListFieldCodec(ByteListField field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.BYTES;
	}

	@Override
	public Value getSpannerValue(){
		byte[] primitiveBytes = ByteTool.fromBoxedBytes(field.getValue());
		ByteArray googleByteArray = ByteArray.copyFrom(primitiveBytes);
		return Value.bytes(googleByteArray);
	}

	@Override
	public Builder setKey(Builder key){
		throw new RuntimeException("Invalid Key type: " + field.getKey().getName());
	}

	@Override
	public List<Byte> getValueFromResultSet(ResultSet rs){
		ByteArray googleByteArray = rs.getBytes(field.getKey().getColumnName());
		byte[] primitiveBytes = googleByteArray.toByteArray();
		return ByteTool.toBoxedBytes(primitiveBytes);
	}

}
