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
package io.datarouter.gcp.spanner.field.date;

import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Key.Builder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Value;

import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerValueTool;
import io.datarouter.model.field.imp.comparable.MilliTimestampEncodedField;
import io.datarouter.types.MilliTime;

public class SpannerMilliTimestampFieldCodec<T> extends SpannerBaseFieldCodec<T,MilliTimestampEncodedField<T>>{

	public SpannerMilliTimestampFieldCodec(MilliTimestampEncodedField<T> field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.TIMESTAMP;
	}

	@Override
	public Value getSpannerValue(){
		MilliTime milliTime = field.getCodec().encode(field.getValue());
		Long epochMilli = milliTime == null ? null : milliTime.toEpochMilli();
		return SpannerValueTool.ofEpochMillis(epochMilli);
	}

	@Override
	public Builder setKey(Builder key){
		MilliTime milliTime = field.getCodec().encode(field.getValue());
		long epochMilli = milliTime.toEpochMilli();
		Timestamp timestamp = SpannerValueTool.toGoogleTimestampFromEpochMilli(epochMilli);
		return key.append(timestamp);
	}

	@Override
	public T getValueFromResultSet(ResultSet rs){
		com.google.cloud.Timestamp googleTimestamp = rs.getTimestamp(field.getKey().getColumnName());
		java.sql.Timestamp sqlTimestamp = googleTimestamp.toSqlTimestamp();
		long epochMilli = sqlTimestamp.getTime();
		MilliTime milliTime = MilliTime.ofEpochMilli(epochMilli);
		return field.getCodec().decode(milliTime);
	}

}