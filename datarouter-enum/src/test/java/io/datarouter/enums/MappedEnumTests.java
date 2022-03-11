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
package io.datarouter.enums;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MappedEnumTests{

	private enum Fruit{
		APPLE("apple"),
		BANANA("banana");

		private static final MappedEnum<String,Fruit> BY_PERSISTENT_STRING = new MappedEnum<>(
				values(),
				value -> value.persistentString);

		private final String persistentString;

		Fruit(String persistentString){
			this.persistentString = persistentString;
		}

	}

	@Test
	public void testGetOrDefault(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING.getOrDefault("x", Fruit.BANANA), Fruit.BANANA);
	}

	@Test
	public void testGetOrThrow(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING.getOrThrow("banana"), Fruit.BANANA);
		Assert.assertThrows(IllegalArgumentException.class, () -> Fruit.BY_PERSISTENT_STRING.getOrThrow("x"));
	}

	@Test
	public void testFind(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING.find("banana").get(), Fruit.BANANA);
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING.find("x"), Optional.empty());
	}

}
