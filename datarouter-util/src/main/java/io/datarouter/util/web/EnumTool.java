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
package io.datarouter.util.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.Displayable;
import io.datarouter.util.enums.DisplayablePersistentString;
import io.datarouter.util.enums.StringEnum;
import io.datarouter.util.string.StringTool;

public class EnumTool{

	public static List<HtmlSelectOptionBean> getHtmlSelectOptions(
			Iterable<? extends DisplayablePersistentString> values){
		return getHtmlSelectOptions(values, Collections.emptyList());
	}

	public static List<HtmlSelectOptionBean> getHtmlSelectOptions(DisplayablePersistentString[] values,
			String... ignoredValues){
		return getHtmlSelectOptions(Arrays.asList(values), Arrays.asList(ignoredValues));
	}

	private static List<HtmlSelectOptionBean> getHtmlSelectOptions(
			Iterable<? extends DisplayablePersistentString> values, Collection<String> ignoredValues){
		List<HtmlSelectOptionBean> options = new ArrayList<>();
		for(DisplayablePersistentString type : values){
			if(ignoredValues.contains(type.getPersistentString())){
				continue;
			}
			options.add(new HtmlSelectOptionBean(type.getDisplay(), type.getPersistentString()));
		}
		return options;
	}

	public static <T extends Displayable> T getEnumFromDisplay(T[] values, String display, T defaultEnum){
		if(display == null){
			return defaultEnum;
		}
		return Stream.of(values)
				.filter(type -> type.getDisplay().equalsIgnoreCase(display))
				.findFirst()
				.orElse(defaultEnum);
	}

	public static <T extends Enum<?>> T getEnumFromName(T[] values, String name, T defaultEnum){
		if(name == null){
			return defaultEnum;
		}
		return Stream.of(values)
				.filter(type -> type.name().equalsIgnoreCase(name))
				.findFirst()
				.orElse(defaultEnum);
	}

	public static <T extends StringEnum<T>> Set<T> getStringEnumFromFreeText(T[] values, String freeText){
		Set<T> result = new HashSet<>();
		if(StringTool.isEmpty(freeText) || values == null || values.length == 0){
			return result;
		}
		String[] elements = freeText.split("[,\\s]+");
		T sample = values[0];
		for(String element : elements){
			element = element.trim();
			if(StringTool.isEmpty(element)){
				continue;
			}
			T elementValue = sample.fromPersistentString(element);
			if(elementValue == null){
				continue;
			}
			// This is a valid value - is it in the desired list?
			for(T value : values){
				if(elementValue == value){
					result.add(elementValue);
					break;
				}
			}
		}
		return result;
	}


	public static class EnumToolTests{
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
		public void testStringEnumFromFreeText(){
			List<Fruit> ts = new ArrayList<>();
			ts.add(Fruit.APPLE);
			ts.add(Fruit.BANANA);
			ts.add(Fruit.DATE);
			ts.add(Fruit.CHERRY);
			ts.add(Fruit.GRAPE);
			String sample = "";
			for(Fruit test : ts){
				sample = sample + "," + test.getPersistentString();
			}
			Set<Fruit> set = EnumTool.getStringEnumFromFreeText(Fruit.values(), sample);
			for(Fruit test : Fruit.values()){
				if(ts.contains(test)){
					Assert.assertTrue(set.contains(test));
				}else{
					Assert.assertFalse(set.contains(test));
				}
			}
		}

		@Test
		public void testGetHtmlSelectOptions(){
			Assert.assertEquals(getHtmlSelectOptions(Fruit.values(), Fruit.UNKNOWN.persistentString).size(), Fruit
					.values().length - 1);
			Assert.assertEquals(getHtmlSelectOptions(Fruit.values(), Fruit.UNKNOWN.persistentString,
					Fruit.EFRUIT.persistentString).size(), Fruit.values().length - 2);
			Assert.assertEquals(getHtmlSelectOptions(Fruit.values()).size(), Fruit.values().length);
			Assert.assertEquals(getHtmlSelectOptions(Fruit.values(), (String)null).size(), Fruit.values().length);
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
			Assert.assertEquals(getEnumFromName(Fruit.values(), "fig", Fruit.UNKNOWN), Fruit.FIG);
			Assert.assertEquals(getEnumFromName(Fruit.values(), "pineapple", Fruit.UNKNOWN), Fruit.UNKNOWN);
			Assert.assertEquals(getEnumFromName(Fruit.values(), "fiG", Fruit.UNKNOWN), Fruit.FIG);
		}

		@Test
		public void testGetEnumFromDisplay(){
			Assert.assertEquals(getEnumFromDisplay(Fruit.values(), "fig", Fruit.UNKNOWN), Fruit.FIG);
			Assert.assertEquals(getEnumFromDisplay(Fruit.values(), "pineapple", Fruit.UNKNOWN), Fruit.UNKNOWN);
			Assert.assertEquals(getEnumFromDisplay(Fruit.values(), "fiG", Fruit.UNKNOWN), Fruit.FIG);
		}
	}

}
