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
package io.datarouter.storage.test.node.basic.manyfield;

import io.datarouter.enums.MappedEnum;
import io.datarouter.enums.StringMappedEnum;

public enum TestEnum{

	DOG(19, "dog"),
	CAT(20, "cat"),
	BEAST(21, "beast"),
	FISH(22, "fish");

	public static final MappedEnum<TestEnum,Integer> BY_PERSISTENT_INTEGER
			= new MappedEnum<>(values(), value -> value.persistentInteger);
	public static final StringMappedEnum<TestEnum> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString);

	public final int persistentInteger;
	public final String persistentString;

	TestEnum(int persistentInteger, String persistentString){
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}

}
