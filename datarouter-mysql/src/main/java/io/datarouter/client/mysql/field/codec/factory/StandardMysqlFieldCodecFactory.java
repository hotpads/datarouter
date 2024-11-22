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

import java.util.HashMap;
import java.util.Map;

import io.datarouter.client.mysql.field.MysqlFieldCodec;
import io.datarouter.client.mysql.field.StringMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.StringEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.ByteArrayEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.array.ByteArrayMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.InstantMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.LocalDateMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.LocalDateTimeMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.datetime.MilliTimestampMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.BooleanMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.DoubleMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.FloatMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.IntegerEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.IntegerMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.LongEncodedMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.LongMysqlFieldCodec;
import io.datarouter.client.mysql.field.codec.primitive.ShortMysqlFieldCodec;
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

public class StandardMysqlFieldCodecFactory implements MysqlFieldCodecFactory{

	private final Map<
			Class<? extends Field<?>>,
			MysqlFieldCodec<?,?>> codecByFieldClass;

	public StandardMysqlFieldCodecFactory(Map<
			Class<? extends Field<?>>,
			MysqlFieldCodec<?,?>> additional){
		codecByFieldClass = new HashMap<>();

		//simple
		addCodec(BooleanField.class, new BooleanMysqlFieldCodec());
		addCodec(ShortField.class, new ShortMysqlFieldCodec());
		addCodec(IntegerField.class, new IntegerMysqlFieldCodec());
		addCodec(LongField.class, new LongMysqlFieldCodec());
		addCodec(FloatField.class, new FloatMysqlFieldCodec());
		addCodec(DoubleField.class, new DoubleMysqlFieldCodec());
		addCodec(StringField.class, new StringMysqlFieldCodec());

		//encoded
		addCodec(IntegerEncodedField.class, new IntegerEncodedMysqlFieldCodec<>());
		addCodec(LongEncodedField.class, new LongEncodedMysqlFieldCodec<>());
		addCodec(ByteArrayEncodedField.class, new ByteArrayEncodedMysqlFieldCodec<>());
		addCodec(StringEncodedField.class, new StringEncodedMysqlFieldCodec<>());

		//time
		addCodec(LocalDateField.class, new LocalDateMysqlFieldCodec());
		addCodec(LocalDateTimeField.class, new LocalDateTimeMysqlFieldCodec());
		addCodec(InstantField.class, new InstantMysqlFieldCodec());
		addCodec(MilliTimestampEncodedField.class, new MilliTimestampMysqlFieldCodec<>());

		//array
		addCodec(ByteArrayField.class, new ByteArrayMysqlFieldCodec());

		additional.forEach(codecByFieldClass::put);
	}

	protected <F extends Field<?>,C extends MysqlFieldCodec<?,?>> void addCodec(
			Class<F> fieldClass,
			C codec){
		codecByFieldClass.put(fieldClass, codec);
	}

	@Override
	public boolean hasCodec(Class<?> fieldType){
		return codecByFieldClass.containsKey(fieldType);
	}

	@Override
	public <T,F extends Field<T>,C extends MysqlFieldCodec<T,F>> C createCodec(F field){
		@SuppressWarnings("unchecked") // safe because of the type safety in the addCodec()
		C codec = (C)codecByFieldClass.get(field.getClass());
		if(codec == null){
			throw new RuntimeException("no codec found for " + field.getClass());
		}
		return codec;
	}

}
