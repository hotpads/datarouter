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
package io.datarouter.client.mysql.field.codec.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.StringMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.ByteArrayEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.ByteArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.IntArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.LongArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.custom.LongDateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.DateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.InstantMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.LocalDateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.LocalDateTimeMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.enums.IntegerEnumMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.enums.StringEnumMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.list.DelimitedStringListMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.BooleanMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.DoubleMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.FloatMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.IntegerEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.IntegerMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.LongEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.LongMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.ShortMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.SignedByteMysqlFieldCodec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.LocalDateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.IntArrayField;
import io.datarouter.model.field.imp.array.LongArrayField;
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
import io.datarouter.model.field.imp.list.DelimitedStringListField;

@SuppressWarnings("deprecation")
public class StandardMysqlFieldCodecFactory implements MysqlFieldCodecFactory{

	private final Map<
			Class<? extends Field<?>>,
			Function<? extends Field<?>,? extends MysqlFieldCodec<?>>> codecClassByFieldClass;

	@SuppressWarnings("unchecked")
	public StandardMysqlFieldCodecFactory(Map<
			Class<? extends Field<?>>,
			Function<? extends Field<?>,? extends MysqlFieldCodec<?>>> additional){
		codecClassByFieldClass = new HashMap<>();

		//simple
		addCodec(BooleanField.class, BooleanMysqlFieldCodec::new);
		addCodec(SignedByteField.class, SignedByteMysqlFieldCodec::new);
		addCodec(ShortField.class, ShortMysqlFieldCodec::new);
		addCodec(IntegerField.class, IntegerMysqlFieldCodec::new);
		addCodec(LongField.class, LongMysqlFieldCodec::new);
		addCodec(FloatField.class, FloatMysqlFieldCodec::new);
		addCodec(DoubleField.class, DoubleMysqlFieldCodec::new);
		addCodec(StringField.class, StringMysqlFieldCodec::new);

		//encoded
		addCodec(IntegerEncodedField.class, IntegerEncodedMysqlFieldCodec::new);
		addCodec(LongEncodedField.class, LongEncodedMysqlFieldCodec::new);
		addCodec(ByteArrayEncodedField.class, ByteArrayEncodedMysqlFieldCodec::new);

		//time
		addCodec(DateField.class, DateMysqlFieldCodec::new);
		addCodec(LongDateField.class, LongDateMysqlFieldCodec::new);
		addCodec(LocalDateField.class, LocalDateMysqlFieldCodec::new);
		addCodec(LocalDateTimeField.class, LocalDateTimeMysqlFieldCodec::new);
		addCodec(InstantField.class, InstantMysqlFieldCodec::new);

		//enum
		addCodec(IntegerEnumField.class, IntegerEnumMysqlFieldCodec::new);
		addCodec(StringEnumField.class, StringEnumMysqlFieldCodec::new);

		//array
		addCodec(ByteArrayField.class, ByteArrayMysqlFieldCodec::new);
		addCodec(IntArrayField.class, IntArrayMysqlFieldCodec::new);
		addCodec(LongArrayField.class, LongArrayMysqlFieldCodec::new);

		//list
		addCodec(DelimitedStringListField.class, DelimitedStringListMysqlFieldCodec::new);

		additional.forEach(codecClassByFieldClass::put);
	}

	protected <F extends Field<?>,C extends MysqlFieldCodec<?>> void addCodec(
			Class<F> fieldClass,
			Function<F,C> codecSupplier){
		codecClassByFieldClass.put(fieldClass, codecSupplier);
	}

	@Override
	public boolean hasCodec(Class<?> fieldType){
		return codecClassByFieldClass.containsKey(fieldType);
	}

	@Override
	public <C extends MysqlFieldCodec<?>,F extends Field<?>> C createCodec(F field){
		@SuppressWarnings("unchecked") // safe because of the type safety in the addCodec()
		Function<F,C> codecSupplier = (Function<F,C>)codecClassByFieldClass.get(field.getClass());
		if(codecSupplier == null){
			throw new RuntimeException("no codec found for " + field.getClass());
		}
		return codecSupplier.apply(field);
	}

	@Override
	public List<MysqlFieldCodec<?>> createCodecs(Collection<Field<?>> fields){
		List<MysqlFieldCodec<?>> codecs = new ArrayList<>(fields.size());
		fields.forEach(field -> codecs.add(createCodec(field)));
		return codecs;
	}

}
