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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import io.datarouter.enums.Displayable;
import io.datarouter.enums.DisplayablePersistentString;

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

}
