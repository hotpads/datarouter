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
package io.datarouter.enums.web;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.enums.DisplayablePersistentString;
import io.datarouter.enums.StringEnum;

public class EnumToolTests{

	private enum Fruit implements DisplayablePersistentString, StringEnum<Fruit>{
		UNKNOWN("Unknown", "unknown"),
		APPLE("Apple", "apple"),
		BANANA("Banana", "banana"),
		CHERRY("cherry", "cherry"),
		DATE("Date", "date"),
		EFRUIT("Efruit", "efruit"),
		FIG("Fig", "fig"),
		GRAPE("Grape", "grape");

		private final String display;
		private final String persistentString;

		Fruit(String display, String persistentString){
			this.display = display;
			this.persistentString = persistentString;
		}

		@Override
		public String getDisplay(){
			return display;
		}

		@Override
		public String getPersistentString(){
			return persistentString;
		}

		@Override
		public Fruit fromPersistentString(String text){
			return StringEnum.getEnumFromStringCaseInsensitive(values(), text, null);
		}
	}

	@Test
	public void testFromPersistentString(){
		Assert.assertEquals(StringEnum.getEnumFromString(Fruit.values(), "fig", null), Fruit.FIG);
		Assert.assertNull(StringEnum.getEnumFromString(Fruit.values(), "FIG", null));
		Assert.assertNull(StringEnum.getEnumFromString(Fruit.values(), "fiG", null));
		Assert.assertEquals(StringEnum.getEnumFromStringCaseInsensitive(Fruit.values(), "fiG", null), Fruit.FIG);
	}

	@Test
	public void testGetEnumFromName(){
		Assert.assertEquals(EnumTool.getEnumFromNameCaseInsensitive(Fruit.values(), "fig", Fruit.UNKNOWN), Fruit.FIG);
		Assert.assertEquals(
				EnumTool.getEnumFromNameCaseInsensitive(Fruit.values(), "pineapple", Fruit.UNKNOWN),
				Fruit.UNKNOWN);
		Assert.assertEquals(EnumTool.getEnumFromNameCaseInsensitive(Fruit.values(), "fiG", Fruit.UNKNOWN), Fruit.FIG);
	}

}
