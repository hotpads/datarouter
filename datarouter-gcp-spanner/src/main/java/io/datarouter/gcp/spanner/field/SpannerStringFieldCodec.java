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
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.util.CommonFieldSizes;

public class SpannerStringFieldCodec extends SpannerBaseFieldCodec<String,StringField>{

	public SpannerStringFieldCodec(StringField field){
		super(field);
	}

	@Override
	public SpannerColumnType getSpannerColumnType(){
		return SpannerColumnType.STRING;
	}

	@Override
	public Value getSpannerValue(){
		String value = field.getValue();
		if(value == null){
			return Value.string(null);
		}
		if(value.length() > CommonFieldSizes.MAX_CHARACTERS_SPANNER){
			String message = String.format("column=%s with length=%s exceeds Spanner's max length=%s",
					field.getKey().getColumnName(),
					value.length(),
					CommonFieldSizes.MAX_CHARACTERS_SPANNER);
			throw new IllegalArgumentException(message);
		}
		return Value.string(value);
	}

	@Override
	public Builder setKey(Builder key){
		return key.append(field.getValue());
	}

	@Override
	public String getValueFromResultSet(ResultSet rs){
		return rs.getString(field.getKey().getColumnName());
	}

}
