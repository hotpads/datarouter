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

import io.datarouter.gcp.spanner.field.array.SpannerBooleanArrayFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerByteArrayFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerDelimitedStringArrayFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerDoubleArrayFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerIntegerArrayFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerUInt63ArrayFieldCodec;
import io.datarouter.gcp.spanner.field.array.SpannerUInt7ArrayFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerDateFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerInstantFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLocalDateFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLocalDateTimeFieldCodec;
import io.datarouter.gcp.spanner.field.date.SpannerLongDateFieldCodec;
import io.datarouter.gcp.spanner.field.enums.SpannerIntegerEnumFieldCodec;
import io.datarouter.gcp.spanner.field.enums.SpannerStringEnumFieldCodec;
import io.datarouter.gcp.spanner.field.positive.SpannerUInt63FieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerBooleanFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerDoubleFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerFloatFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerIntegerFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerLongFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerShortFieldCodec;
import io.datarouter.gcp.spanner.field.primitive.SpannerSignedByteFieldCodec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.LocalDateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.array.BooleanArrayField;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.DelimitedStringArrayField;
import io.datarouter.model.field.imp.array.DoubleArrayField;
import io.datarouter.model.field.imp.array.IntegerArrayField;
import io.datarouter.model.field.imp.array.UInt63ArrayField;
import io.datarouter.model.field.imp.array.UInt7ArrayField;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.DoubleField;
import io.datarouter.model.field.imp.comparable.FloatField;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.ShortField;
import io.datarouter.model.field.imp.comparable.SignedByteField;
import io.datarouter.model.field.imp.custom.LocalDateTimeField;
import io.datarouter.model.field.imp.custom.LongDateField;
import io.datarouter.model.field.imp.enums.IntegerEnumField;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.lang.ReflectionTool;

@Singleton
public class SpannerFieldCodecRegistry{

	private final Map<Class<? extends Field<?>>,Class<? extends SpannerBaseFieldCodec<?,?>>> codecByFieldClass;

	@SuppressWarnings("unchecked")
	public SpannerFieldCodecRegistry(){
		//TODO create rest of fields and codecs
		codecByFieldClass = new HashMap<>();
		addCodec(StringField.class, SpannerStringFieldCodec.class);
		addCodec(BooleanField.class, SpannerBooleanFieldCodec.class);
		addCodec(DoubleField.class, SpannerDoubleFieldCodec.class);
		addCodec(FloatField.class, SpannerFloatFieldCodec.class);
		addCodec(IntegerField.class, SpannerIntegerFieldCodec.class);
		addCodec(LongField.class, SpannerLongFieldCodec.class);
		addCodec(ShortField.class, SpannerShortFieldCodec.class);

		addCodec(UInt63Field.class, SpannerUInt63FieldCodec.class);

		addCodec(SignedByteField.class, SpannerSignedByteFieldCodec.class);

		addCodec(IntegerEnumField.class, SpannerIntegerEnumFieldCodec.class);
		addCodec(StringEnumField.class, SpannerStringEnumFieldCodec.class);

		addCodec(DateField.class, SpannerDateFieldCodec.class);
		addCodec(InstantField.class, SpannerInstantFieldCodec.class);
		addCodec(LocalDateField.class, SpannerLocalDateFieldCodec.class);
		addCodec(LocalDateTimeField.class, SpannerLocalDateTimeFieldCodec.class);
		addCodec(LongDateField.class, SpannerLongDateFieldCodec.class);

		addCodec(BooleanArrayField.class, SpannerBooleanArrayFieldCodec.class);
		addCodec(ByteArrayField.class, SpannerByteArrayFieldCodec.class);
		addCodec(DelimitedStringArrayField.class, SpannerDelimitedStringArrayFieldCodec.class);
		addCodec(UInt7ArrayField.class, SpannerUInt7ArrayFieldCodec.class);
		addCodec(UInt63ArrayField.class, SpannerUInt63ArrayFieldCodec.class);
		addCodec(DoubleArrayField.class, SpannerDoubleArrayFieldCodec.class);
		addCodec(IntegerArrayField.class, SpannerIntegerArrayFieldCodec.class);
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
