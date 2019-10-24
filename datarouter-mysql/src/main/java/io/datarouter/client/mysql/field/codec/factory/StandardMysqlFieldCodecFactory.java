/**
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
package io.datarouter.client.mysql.field.codec.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.StringMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.BooleanArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.ByteArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.DelimitedStringArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.DoubleArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.IntegerArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.PrimitiveIntegerArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.PrimitiveLongArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.UInt63ArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.UInt7ArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.custom.LongDateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.DateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.InstantMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.LocalDateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.LocalDateTimeMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.enums.IntegerEnumMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.enums.StringEnumMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.enums.VarIntEnumMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.positive.UInt15MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.positive.UInt31MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.positive.UInt63MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.positive.UInt7MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.positive.UInt8MysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.positive.VarIntMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.BooleanMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.CharacterMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.DoubleMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.FloatMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.IntegerMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.LongMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.ShortMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.SignedByteMysqlFieldCodec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.LocalDateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.array.BooleanArrayField;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.DelimitedStringArrayField;
import io.datarouter.model.field.imp.array.DoubleArrayField;
import io.datarouter.model.field.imp.array.IntegerArrayField;
import io.datarouter.model.field.imp.array.PrimitiveIntegerArrayField;
import io.datarouter.model.field.imp.array.PrimitiveLongArrayField;
import io.datarouter.model.field.imp.array.UInt63ArrayField;
import io.datarouter.model.field.imp.array.UInt7ArrayField;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.CharacterField;
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
import io.datarouter.model.field.imp.enums.VarIntEnumField;
import io.datarouter.model.field.imp.positive.UInt15Field;
import io.datarouter.model.field.imp.positive.UInt31Field;
import io.datarouter.model.field.imp.positive.UInt63Field;
import io.datarouter.model.field.imp.positive.UInt7Field;
import io.datarouter.model.field.imp.positive.UInt8Field;
import io.datarouter.model.field.imp.positive.VarIntField;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.lang.ReflectionTool;

@Singleton
public class StandardMysqlFieldCodecFactory implements MysqlFieldCodecFactory{

	private final Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> codecClassByFieldClass;

	public StandardMysqlFieldCodecFactory(
			Map<Class<? extends Field<?>>,Class<? extends MysqlFieldCodec<?>>> additional){
		codecClassByFieldClass = new HashMap<>();

		addCodec(BooleanField.class, BooleanMysqlFieldCodec.class);
		addCodec(CharacterField.class, CharacterMysqlFieldCodec.class);
		addCodec(SignedByteField.class, SignedByteMysqlFieldCodec.class);
		addCodec(ShortField.class, ShortMysqlFieldCodec.class);
		addCodec(IntegerField.class, IntegerMysqlFieldCodec.class);
		addCodec(LongField.class, LongMysqlFieldCodec.class);
		addCodec(FloatField.class, FloatMysqlFieldCodec.class);
		addCodec(DoubleField.class, DoubleMysqlFieldCodec.class);

		addCodec(StringField.class, StringMysqlFieldCodec.class);
		addCodec(DateField.class, DateMysqlFieldCodec.class);
		addCodec(LongDateField.class, LongDateMysqlFieldCodec.class);
		addCodec(LocalDateField.class, LocalDateMysqlFieldCodec.class);
		addCodec(LocalDateTimeField.class, LocalDateTimeMysqlFieldCodec.class);
		addCodec(InstantField.class, InstantMysqlFieldCodec.class);

		//enums
		addCodec(IntegerEnumField.class, IntegerEnumMysqlFieldCodec.class);
		addCodec(StringEnumField.class, StringEnumMysqlFieldCodec.class);
		addCodec(VarIntEnumField.class, VarIntEnumMysqlFieldCodec.class);

		//BaseListMysqlFieldCodec
		addCodec(BooleanArrayField.class, BooleanArrayMysqlFieldCodec.class);
		addCodec(DelimitedStringArrayField.class, DelimitedStringArrayMysqlFieldCodec.class);
		addCodec(DoubleArrayField.class, DoubleArrayMysqlFieldCodec.class);
		addCodec(IntegerArrayField.class, IntegerArrayMysqlFieldCodec.class);
		addCodec(UInt63ArrayField.class, UInt63ArrayMysqlFieldCodec.class);
		addCodec(UInt7ArrayField.class, UInt7ArrayMysqlFieldCodec.class);

		//primitive arrays
		addCodec(ByteArrayField.class, ByteArrayMysqlFieldCodec.class);
		addCodec(PrimitiveIntegerArrayField.class, PrimitiveIntegerArrayMysqlFieldCodec.class);
		addCodec(PrimitiveLongArrayField.class, PrimitiveLongArrayMysqlFieldCodec.class);

		//positive numbers only
		addCodec(UInt15Field.class, UInt15MysqlFieldCodec.class);
		addCodec(UInt31Field.class, UInt31MysqlFieldCodec.class);
		addCodec(UInt63Field.class, UInt63MysqlFieldCodec.class);
		addCodec(UInt7Field.class, UInt7MysqlFieldCodec.class);
		addCodec(UInt8Field.class, UInt8MysqlFieldCodec.class);
		addCodec(VarIntField.class, VarIntMysqlFieldCodec.class);

		additional.forEach((fieldClass, codecClass) -> addCodec(fieldClass, codecClass));
	}

	protected <F extends Field<?>,C extends MysqlFieldCodec<?>> void addCodec(Class<F> fieldClass, Class<C> codecClass){
		codecClassByFieldClass.put(fieldClass, codecClass);
	}

	@Override
	public boolean hasCodec(Class<?> fieldType){
		return codecClassByFieldClass.containsKey(fieldType);
	}

	@Override
	public MysqlFieldCodec<?> createCodec(Field<?> field){
		Class<? extends MysqlFieldCodec<?>> codecType = codecClassByFieldClass.get(field.getClass());
		if(codecType == null){
			throw new RuntimeException("no codec found for " + field.getClass());
		}
		return ReflectionTool.createWithParameters(codecType, Arrays.asList(field));
	}

	@Override
	public List<MysqlFieldCodec<?>> createCodecs(Collection<Field<?>> fields){
		List<MysqlFieldCodec<?>> codecs = ListTool.createArrayListWithSize(fields);
		for(Field<?> field : IterableTool.nullSafe(fields)){
			codecs.add(createCodec(field));
		}
		return codecs;
	}

}
