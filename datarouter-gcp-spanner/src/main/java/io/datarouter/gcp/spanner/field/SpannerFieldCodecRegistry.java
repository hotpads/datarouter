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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.datarouter.gcp.spanner.field.array.SpannerByteArrayEncodedFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerByteArrayFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerInstantFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLocalDateFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLocalDateTimeFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerMilliTimestampFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerBooleanFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerDoubleFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerFloatFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerIntegerEncodedFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerIntegerFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerLongEncodedFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerLongFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerShortFieldCodec;
import io.datarouter.gcp.spanner.field.string.SpannerStringEncodedFieldCodec;
import io.datarouter.gcp.spanner.field.string.SpannerStringFieldCodec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.LocalDateField;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.FloatField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.IntegerEncodedField;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.MilliTimestampEncodedField;
import io.datarouter.model.field.imp.comparable.ShortField;
import io.datarouter.model.field.imp.custom.LocalDateTimeField;
import io.datarouter.util.lang.ReflectionTool;

public class SpannerFieldCodecRegistry implements SpannerFieldCodecs{

	private final Map<Class<? extends Field<?>>,Class<? extends SpannerBaseFieldCodec<?,?>>> codecByFieldClass;

	public SpannerFieldCodecRegistry(){
		codecByFieldClass = new HashMap<>();

		//simple
		addCodec(StringField.class, SpannerStringFieldCodec.class);
		addCodec(BooleanField.class, SpannerBooleanFieldCodec.class);
		addCodec(DoubleField.class, SpannerDoubleFieldCodec.class);
		addCodec(FloatField.class, SpannerFloatFieldCodec.class);
		addCodec(IntegerField.class, SpannerIntegerFieldCodec.class);
		addCodec(LongField.class, SpannerLongFieldCodec.class);
		addCodec(ShortField.class, SpannerShortFieldCodec.class);

		//encoded
		addCodec(IntegerEncodedField.class, SpannerIntegerEncodedFieldCodec.class);
		addCodec(LongEncodedField.class, SpannerLongEncodedFieldCodec.class);
		addCodec(ByteArrayEncodedField.class, SpannerByteArrayEncodedFieldCodec.class);
		addCodec(StringEncodedField.class, SpannerStringEncodedFieldCodec.class);

		//time
		addCodec(InstantField.class, SpannerInstantFieldCodec.class);
		addCodec(LocalDateField.class, SpannerLocalDateFieldCodec.class);
		addCodec(LocalDateTimeField.class, SpannerLocalDateTimeFieldCodec.class);
		addCodec(MilliTimestampEncodedField.class, SpannerMilliTimestampFieldCodec.class);

		//array
		addCodec(ByteArrayField.class, SpannerByteArrayFieldCodec.class);
	}

	public <F extends Field<?>,
			C extends SpannerBaseFieldCodec<?,?>>
	SpannerFieldCodecRegistry addCodec(Class<F> fieldClass, Class<C> codecClass){
		codecByFieldClass.put(fieldClass, codecClass);
		return this;
	}

	@Override
	public SpannerBaseFieldCodec<?,?> createCodec(Field<?> field){
		Class<? extends SpannerBaseFieldCodec<?,?>> spannerCodecClass = codecByFieldClass.get(field.getClass());
		if(spannerCodecClass == null){
			String message = String.format("unkown field class=%s", field.getClass().getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		return ReflectionTool.createWithParameters(
				spannerCodecClass,
				Collections.singletonList(field));
	}

}
