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
import com.google.cloud.spanner.Mutation.WriteBuilder;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.Value;

import io.datarouter.gcp.spanner.ddl.SpannerColumn;
import io.datarouter.gcp.spanner.ddl.SpannerColumnType;
import io.datarouter.model.field.Field;

public abstract class SpannerBaseFieldCodec<T,F extends Field<T>>{

	protected final F field;

	public SpannerBaseFieldCodec(F field){
		this.field = field;
	}

	public abstract SpannerColumnType getSpannerColumnType();
	public abstract Value getSpannerValue();
	public abstract Builder setKey(Builder key);
	public abstract T getValueFromResultSet(ResultSet rs);

	public final WriteBuilder setMutation(WriteBuilder mutation){
		Value spannerValue = field.getValue() == null
				? getSpannerColumnType().nullValue()
				: getSpannerValue();
		return mutation.set(field.getKey().getColumnName()).to(spannerValue);
	}

	public SpannerColumn getSpannerColumn(Boolean allowNullable){
		return new SpannerColumn(
				field.getKey().getColumnName(),
				getSpannerColumnType(),
				allowNullable && field.getKey().isNullable());
	}

	public void setField(Object fieldToSet, ResultSet rs){
		if(rs.isNull(field.getKey().getColumnName())){
			return;
		}
		T value = getValueFromResultSet(rs);
		field.setUsingReflection(fieldToSet, value);
	}

	public Field<T> getField(){
		return field;
	}

	public String getParameterName(int index, boolean withAmpersand){
		String paramName = "p" + index;//docs are wrong - cannot start with a digit
		return withAmpersand ? "@" + paramName : paramName;
	}

	public void setParameterValue(Statement.Builder statementBuilder, int index){
		 statementBuilder.bind(getParameterName(index, false)).to(getSpannerValue());
	}

}
