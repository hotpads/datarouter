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
package io.datarouter.util.web;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.DisplayablePersistentString;
import io.datarouter.util.enums.StringEnum;

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
			return DatarouterEnumTool.getEnumFromString(values(), text, null, false);
		}
	}

	@Test
	public void testGetHtmlSelectOptions(){
		Assert.assertEquals(EnumTool.getHtmlSelectOptions(Fruit.values(), Fruit.UNKNOWN.persistentString).size(), Fruit
				.values().length - 1);
		Assert.assertEquals(EnumTool.getHtmlSelectOptions(Fruit.values(), Fruit.UNKNOWN.persistentString,
				Fruit.EFRUIT.persistentString).size(), Fruit.values().length - 2);
		Assert.assertEquals(EnumTool.getHtmlSelectOptions(Fruit.values()).size(), Fruit.values().length);
		Assert.assertEquals(EnumTool.getHtmlSelectOptions(Fruit.values(), (String)null).size(), Fruit.values().length);
	}

	@Test
	public void testFromPersistentString(){
		Assert.assertEquals(DatarouterEnumTool.getEnumFromString(Fruit.values(), "fig", null), Fruit.FIG);
		Assert.assertNull(DatarouterEnumTool.getEnumFromString(Fruit.values(), "FIG", null));
		Assert.assertNull(DatarouterEnumTool.getEnumFromString(Fruit.values(), "fiG", null, true));
		Assert.assertEquals(DatarouterEnumTool.getEnumFromString(Fruit.values(), "fiG", null, false), Fruit.FIG);
	}

	@Test
	public void testGetEnumFromName(){
		Assert.assertEquals(EnumTool.getEnumFromName(Fruit.values(), "fig", Fruit.UNKNOWN), Fruit.FIG);
		Assert.assertEquals(EnumTool.getEnumFromName(Fruit.values(), "pineapple", Fruit.UNKNOWN), Fruit.UNKNOWN);
		Assert.assertEquals(EnumTool.getEnumFromName(Fruit.values(), "fiG", Fruit.UNKNOWN), Fruit.FIG);
	}

	@Test
	public void testGetEnumFromDisplay(){
		Assert.assertEquals(EnumTool.getEnumFromDisplay(Fruit.values(), "fig", Fruit.UNKNOWN), Fruit.FIG);
		Assert.assertEquals(EnumTool.getEnumFromDisplay(Fruit.values(), "pineapple", Fruit.UNKNOWN), Fruit.UNKNOWN);
		Assert.assertEquals(EnumTool.getEnumFromDisplay(Fruit.values(), "fiG", Fruit.UNKNOWN), Fruit.FIG);
	}

}
