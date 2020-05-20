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
package io.datarouter.loadtest.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class RandomValue extends BaseDatabean<RandomValueKey,RandomValue>{

	public static final Random RANDOM = new Random();

	public static final List<String> VALUES = new ArrayList<>();
	static{
		final int numValues = 100;
		final int valueLength = 1;
		for(int i = 0; i < numValues; ++i){
			StringBuilder sb = new StringBuilder();
			while(sb.length() < valueLength){
				sb.append(Long.toHexString(RANDOM.nextLong()));
			}
			VALUES.add(sb.substring(0, valueLength));
		}
	}

	private String value;


	public static class FieldKeys{
		public static final StringFieldKey value = new StringFieldKey("value")
				.withColumnName("v")
				.withSize(65536);
	}

	public static class RandomValueFielder extends BaseDatabeanFielder<RandomValueKey,RandomValue>{

		public RandomValueFielder(){
			super(RandomValueKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(RandomValue databean){
			return Arrays.asList(
					new StringField(FieldKeys.value, databean.value));
		}

	}

	public RandomValue(){
		super(new RandomValueKey(null));
	}

	public RandomValue(Integer keyInt){
		super(new RandomValueKey(keyInt));
		this.value = VALUES.get(RANDOM.nextInt(VALUES.size()));
	}

	@Override
	public Class<RandomValueKey> getKeyClass(){
		return RandomValueKey.class;
	}

	public String getV(){
		return value;
	}

	public Integer getK(){
		return getKey().getEntityKey().getKey();
	}

}
