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
package io.datarouter.web.html.j2html;

import static j2html.TagCreator.caption;
import static j2html.TagCreator.each;
import static j2html.TagCreator.table;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class J2HtmlTable<T>{

	private final List<String> classes = new ArrayList<>();
	private final List<J2HtmlTableColumn<T>> columns = new ArrayList<>();
	private String caption;

	public J2HtmlTable<T> withClasses(String... classes){
		this.classes.addAll(Arrays.asList(classes));
		return this;
	}

	public J2HtmlTable<T> withColumn(String name, Function<T,Object> valueFunction){
		Function<T,DomContent> function = dto -> Optional.ofNullable(valueFunction.apply(dto))
				.map(Object::toString)
				.map(TagCreator::text)
				.orElseGet(() -> text(""));
		columns.add(new J2HtmlTableColumn<>(name, function));
		return this;
	}

	public J2HtmlTable<T> withHtmlColumn(String name, Function<T,DomContent> valueFunction){
		columns.add(new J2HtmlTableColumn<>(name, valueFunction));
		return this;
	}

	public J2HtmlTable<T> withCaption(String caption){
		this.caption = caption;
		return this;
	}

	private static class J2HtmlTableColumn<T>{
		private String name;
		private Function<T,DomContent> valueFunction;

		public J2HtmlTableColumn(String name, Function<T,DomContent> valueFunction){
			this.name = name;
			this.valueFunction = valueFunction;
		}
	}

	public ContainerTag build(Collection<T> rows){
		var thead = thead(tr(each(columns, column -> th(column.name))));
		var table = table()
				.withClasses(classes.toArray(String[]::new))
				.condWith(caption != null, caption(caption))
				.with(thead);
		rows.stream()
				.map(this::makeTr)
				.forEach(table::with);
		return table;
	}

	private ContainerTag makeTr(T dto){
		var tr = tr();
		columns.stream()
				.map(column -> column.valueFunction.apply(dto))
				.map(TagCreator::td)
				.forEach(tr::with);
		return tr;
	}

}
