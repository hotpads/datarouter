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

import static j2html.TagCreator.caption;
import static j2html.TagCreator.each;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;
import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import j2html.tags.specialized.TrTag;

public class J2HtmlTable<T>{

	private final List<String> classes = new ArrayList<>();
	private final List<String> styles = new ArrayList<>();
	private final List<J2HtmlTableColumn<T>> columns = new ArrayList<>();
	private Optional<String> caption = Optional.empty();
	private Optional<String> id = Optional.empty();

	public J2HtmlTable<T> withClasses(String... classes){
		this.classes.addAll(Arrays.asList(classes));
		return this;
	}

	public J2HtmlTable<T> withStyles(String... styles){
		this.styles.addAll(Arrays.asList(styles));
		return this;
	}

	public J2HtmlTable<T> withColumn(String name, Function<T,String> valueFunction){
		Function<T,TdTag> function = dto -> Optional.ofNullable(valueFunction.apply(dto))
				.map(Object::toString)
				.map(TagCreator::td)
				.orElseGet(TagCreator::td);
		columns.add(new J2HtmlTableColumn<>(name, function));
		return this;
	}

	public <V> J2HtmlTable<T> withColumn(
			String name,
			Function<T,V> valueExtractorFn,
			Function<V,String> valueToStringFn){
		Function<T,TdTag> function = dto -> Optional.ofNullable(valueExtractorFn.apply(dto))
				.map(valueToStringFn)
				.map(TagCreator::td)
				.orElseGet(TagCreator::td);
		columns.add(new J2HtmlTableColumn<>(name, function));
		return this;
	}

	public J2HtmlTable<T> withHtmlColumn(String name, Function<T,TdTag> valueFunction){
		columns.add(new J2HtmlTableColumn<>(name, valueFunction));
		return this;
	}

	public J2HtmlTable<T> withHtmlColumn(DomContent name, Function<T,TdTag> valueFunction){
		columns.add(new J2HtmlTableColumn<>(name, valueFunction));
		return this;
	}

	public J2HtmlTable<T> withCaption(String caption){
		this.caption = Optional.of(caption);
		return this;
	}

	public J2HtmlTable<T> withId(String id){
		this.id = Optional.of(id);
		return this;
	}

	private static class J2HtmlTableColumn<T>{

		private DomContent name;
		private Function<T,TdTag> valueFunction;

		public J2HtmlTableColumn(String name, Function<T,TdTag> valueFunction){
			this(th(name), valueFunction);
		}

		public J2HtmlTableColumn(DomContent name, Function<T,TdTag> valueFunction){
			this.name = name;
			this.valueFunction = valueFunction;
		}

	}

	public static class J2HtmlTableRow<T>{

		private final T value;
		private final List<String> styles;
		private final List<String> classes;

		public J2HtmlTableRow(T value){
			this.value = value;
			this.styles = new ArrayList<>();
			this.classes = new ArrayList<>();
		}

		public J2HtmlTableRow<T> withStyle(String style){
			styles.add(style);
			return this;
		}

		public J2HtmlTableRow<T> withClasses(String... classes){
			this.classes.addAll(Arrays.asList(classes));
			return this;
		}

	}

	public TableTag build(Collection<T> rows){
		return build(rows, J2HtmlTableRow::new);
	}

	public TableTag build(Collection<T> values, Function<T,J2HtmlTableRow<T>> rowFunction){
		List<J2HtmlTableRow<T>> rows = Scanner.of(values)
				.map(rowFunction)
				.list();
		boolean includeHeader = Scanner.of(columns)
				.map(column -> column.name)
				.anyMatch(Objects::nonNull);
		var thead = thead(tr(each(columns, column -> column.name)));
		var tbody = tbody(each(rows, this::makeTr));
		return table()
				.withClasses(classes.toArray(String[]::new))
				.withStyle(String.join(";", styles))
				//The conditional still calls the value whether or not its present.
				.condWith(caption.isPresent(), caption(caption.orElse("")))
				.withCondId(id.isPresent(), id.orElse(""))
				.condWith(includeHeader, thead)
				.with(tbody);
	}

	private TrTag makeTr(J2HtmlTableRow<T> row){
		return tr(makeTds(row.value))
				.withCondStyle(!row.styles.isEmpty(), String.join(";", row.styles))
				.withCondClass(!row.classes.isEmpty(), String.join(" ", row.classes));
	}

	private DomContent makeTds(T value){
		return each(columns, column -> column.valueFunction.apply(value));
	}

}
