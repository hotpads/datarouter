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
package io.datarouter.web.html.j2html;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class J2HtmlTool{

	public static String renderWithLineBreaks(DomContent... items){
		return Arrays.stream(items)
				.map(DomContent::render)
				.collect(Collectors.joining("\n", "", "\n"));
	}

	// wrapper around TagCreator.each to help compiler with unknown arguments
	public static DomContent each(
			Collection<ContainerTag<?>> collection,
			Function<? super ContainerTag<?>, DomContent> mapper){
		return each(collection, mapper);
	}

	/**
	 * Multiple items not wrapped in a parent tag.
	 */
	public static DomContent siblings(Collection<? extends DomContent> items){
		return TagCreator.each(items, Function.identity());
	}

}
