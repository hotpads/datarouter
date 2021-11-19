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
package io.datarouter.secret.op.adapter;

import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.SecretJsonSerializer;

public class SecretOpSerializationAdapterUnitTests{

	private static final SecretOpReason reason = SecretOpReason.automatedOp(SecretOpSerializationAdapterUnitTests.class
			.getName());
	private static final SecretOpConfig serializeConfig = SecretOpConfig.builder(reason).build();
	private static final SecretOpConfig skipConfig = SecretOpConfig.builder(reason).disableSerialization().build();
	private static final SecretJsonSerializer jsonSerializer = new SecretJsonSerializer.GsonToolJsonSerializer();

	private static class SerializationDto{

		private final int id;
		private final String name;

		public SerializationDto(int id){
			this.id = id;
			this.name = String.valueOf(id);
		}

		@Override
		public boolean equals(Object obj){
			try{
				SerializationDto other = (SerializationDto)obj;
				return Objects.equals(this.id, other.id) && Objects.equals(this.name, other.name);
			}catch(RuntimeException e){
				return false;
			}
		}

		@Override
		public int hashCode(){
			return super.hashCode();
		}

	}

	@Test
	public void testConstructors(){
		new SerializingAdapter<>(null, skipConfig);
		Assert.assertThrows(() -> new SerializingAdapter<>(null, serializeConfig));
		new DeserializingAdapter<>(null, String.class, skipConfig);
		Assert.assertThrows(() -> new DeserializingAdapter<>(null, Object.class, skipConfig));
		Assert.assertThrows(() -> new DeserializingAdapter<>(null, Object.class, serializeConfig));
	}

	@Test
	public void testSerialization(){
		TypedSecret<SerializationDto> dtoSecret = new TypedSecret<>("secret", new SerializationDto(5));
		var dtoSerializer = new SerializingAdapter<SerializationDto>(jsonSerializer, serializeConfig);
		var dtoDeserializer = new DeserializingAdapter<>(
				jsonSerializer,
				SerializationDto.class,
				serializeConfig);
		TypedSecret<SerializationDto> roundTrippedDto = dtoDeserializer.adapt(dtoSerializer.adapt(dtoSecret));
		Assert.assertEquals(roundTrippedDto.getValue(), dtoSecret.getValue());
		Assert.assertFalse(roundTrippedDto.getValue() == dtoSecret.getValue());
	}

	@Test
	public void testDisableSerialization(){
		String original = "original";
		TypedSecret<String> originalStringSecret = new TypedSecret<>("secret", original);
		var stringSerializer = new SerializingAdapter<String>(jsonSerializer, skipConfig);
		var stringDeserializer = new DeserializingAdapter<>(jsonSerializer, String.class, skipConfig);
		TypedSecret<String> roundTrippedString = stringDeserializer.adapt(stringSerializer.adapt(originalStringSecret));
		Assert.assertEquals(roundTrippedString.getValue(), originalStringSecret.getValue());
		Assert.assertTrue(roundTrippedString.getValue() == original);
	}

}
