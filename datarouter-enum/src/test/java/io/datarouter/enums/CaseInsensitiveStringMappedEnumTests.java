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

public class CaseInsensitiveStringMappedEnumTests{

	private enum Fruit{
		APPLE("apple"),
		BANANA("banana"),
		CHERRY("Cherry");//capitalized

		private static final CaseInsensitiveStringMappedEnum<Fruit> BY_PERSISTENT_STRING_CASE_INSENSITIVE
				= new CaseInsensitiveStringMappedEnum<>(values(), value -> value.persistentString);

		private final String persistentString;

		Fruit(String persistentString){
			this.persistentString = persistentString;
		}

	}

	@Test
	public void testToKey(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.toKey(Fruit.BANANA), "banana");
		//toKey should keep the original value
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.toKey(Fruit.CHERRY), "Cherry");
	}

	@Test
	public void testFromOrNull(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrNull("banana"), Fruit.BANANA);
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrNull("BANANA"), Fruit.BANANA);
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrNull("cherry"), Fruit.CHERRY);
		Assert.assertNull(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrNull("x"));
	}

	@Test
	public void testFromOrDefault(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrElse("x", Fruit.BANANA), Fruit.BANANA);
		Assert.assertThrows(
				NullPointerException.class,
				() -> Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrElse("x", null));
	}

	@Test
	public void testFromOrThrow(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrThrow("banana"), Fruit.BANANA);
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrThrow("BananA"), Fruit.BANANA);
		Assert.assertThrows(
				IllegalArgumentException.class,
				() -> Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.fromOrThrow("x"));
	}

	@Test
	public void testFrom(){
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.from("banana").get(), Fruit.BANANA);
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.from("bANana").get(), Fruit.BANANA);
		Assert.assertSame(Fruit.BY_PERSISTENT_STRING_CASE_INSENSITIVE.from("x"), Optional.empty());
	}

}
