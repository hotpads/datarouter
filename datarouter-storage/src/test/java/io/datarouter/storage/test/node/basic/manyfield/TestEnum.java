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
package io.datarouter.storage.test.node.basic.manyfield;

import io.datarouter.enums.IntegerEnum;
import io.datarouter.enums.MappedEnum;
import io.datarouter.enums.StringEnum;

public enum TestEnum implements IntegerEnum<TestEnum>, StringEnum<TestEnum>{

	dog(19, "dog"),
	cat(20, "cat"),
	beast(21, "beast"),
	fish(22, "fish");

	public static final MappedEnum<Integer,TestEnum> BY_PERSISTENT_INTEGER = new MappedEnum<>(
			values(),
			value -> value.persistentInteger);
	public static final MappedEnum<String,TestEnum> BY_PERSISTENT_STRING = new MappedEnum<>(
			values(),
			value -> value.persistentString);

	private final int persistentInteger;
	private final String persistentString;

	TestEnum(int persistentInteger, String persistentString){
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}

	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}

	@Override
	public TestEnum fromPersistentInteger(Integer value){
		return BY_PERSISTENT_INTEGER.fromOrNull(value);
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public TestEnum fromPersistentString(String value){
		return BY_PERSISTENT_STRING.fromOrNull(value);
	}

}
