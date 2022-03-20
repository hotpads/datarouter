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
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.datarouter.gcp.spanner.field.array.SpannerByteArrayFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerDateFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerInstantFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLocalDateFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLocalDateTimeFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLongDateFieldCodec;
import io.datarouter.gcp.spanner.field.enums.SpannerIntegerEnumFieldCodec;
import io.datarouter.gcp.spanner.field.enums.SpannerStringEnumFieldCodec;
import io.datarouter.gcp.spanner.field.list.SpannerByteListFieldCodec;
import io.datarouter.gcp.spanner.field.list.SpannerDelimitedStringListFieldCodec;
import io.datarouter.gcp.spanner.field.list.SpannerDoubleListFieldCodec;
import io.datarouter.gcp.spanner.field.list.SpannerIntListFieldCodec;
import io.datarouter.gcp.spanner.field.list.SpannerRawLongListFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerBooleanFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerDoubleFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerFloatFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerIntegerEncodedFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerIntegerFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerLongEncodedFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerLongFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerShortFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerSignedByteFieldCodec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.LocalDateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.FloatField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.IntegerEncodedField;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.ShortField;
import io.datarouter.model.field.imp.comparable.SignedByteField;
import io.datarouter.model.field.imp.custom.LocalDateTimeField;
import io.datarouter.model.field.imp.custom.LongDateField;
import io.datarouter.model.field.imp.enums.IntegerEnumField;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.list.ByteListField;
import io.datarouter.model.field.imp.list.DelimitedStringListField;
import io.datarouter.model.field.imp.list.DoubleListField;
import io.datarouter.model.field.imp.list.IntListField;
import io.datarouter.model.field.imp.list.RawLongListField;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;

@SuppressWarnings("deprecation")
@Singleton
public class SpannerFieldCodecRegistry{

	private final Map<Class<? extends Field<?>>,Class<? extends SpannerBaseFieldCodec<?,?>>> codecByFieldClass;

	@SuppressWarnings("unchecked")
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
		addCodec(SignedByteField.class, SpannerSignedByteFieldCodec.class);

		//encoded
		addCodec(IntegerEncodedField.class, SpannerIntegerEncodedFieldCodec.class);
		addCodec(LongEncodedField.class, SpannerLongEncodedFieldCodec.class);

		//enum
		addCodec(IntegerEnumField.class, SpannerIntegerEnumFieldCodec.class);
		addCodec(StringEnumField.class, SpannerStringEnumFieldCodec.class);

		//time
		addCodec(DateField.class, SpannerDateFieldCodec.class);
		addCodec(InstantField.class, SpannerInstantFieldCodec.class);
		addCodec(LocalDateField.class, SpannerLocalDateFieldCodec.class);
		addCodec(LocalDateTimeField.class, SpannerLocalDateTimeFieldCodec.class);
		addCodec(LongDateField.class, SpannerLongDateFieldCodec.class);

		//array
		addCodec(ByteArrayField.class, SpannerByteArrayFieldCodec.class);

		//list
		addCodec(DelimitedStringListField.class, SpannerDelimitedStringListFieldCodec.class);
		addCodec(IntListField.class, SpannerIntListFieldCodec.class);
		addCodec(DoubleListField.class, SpannerDoubleListFieldCodec.class);
		addCodec(RawLongListField.class, SpannerRawLongListFieldCodec.class);
		addCodec(ByteListField.class, SpannerByteListFieldCodec.class);
	}

	private <F extends Field<?>,C extends SpannerBaseFieldCodec<?,F>> void addCodec(
			Class<F> fieldClass,
			Class<C> codecClass){
		codecByFieldClass.put(fieldClass, codecClass);
	}

	public SpannerBaseFieldCodec<?,?> createCodec(Field<?> field){
		return ReflectionTool.createWithParameters(
				codecByFieldClass.get(field.getClass()),
				Collections.singletonList(field));
	}

	public List<? extends SpannerBaseFieldCodec<?,?>> createCodecs(List<Field<?>> fields){
		return Scanner.of(fields).map(this::createCodec).list();
	}

}
