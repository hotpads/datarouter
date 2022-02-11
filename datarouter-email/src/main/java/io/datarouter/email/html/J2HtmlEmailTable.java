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
package io.datarouter.email.html;

import static j2html.TagCreator.each;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import j2html.TagCreator;
import j2html.attributes.Attr;
import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import j2html.tags.specialized.TdTag;
import j2html.tags.specialized.TrTag;

public class J2HtmlEmailTable<T>{

	private final List<J2HtmlEmailTableColumn<T>> j2HtmlEmailTableColumns = new ArrayList<>();

	/**
	 * @param valueFunction extracts text to be escaped before becoming a cell
	 */
	public J2HtmlEmailTable<T> withColumn(String name, Function<T,Object> valueFunction){
		j2HtmlEmailTableColumns.add(J2HtmlEmailTableColumn.ofText(name, valueFunction));
		return this;
	}

	public J2HtmlEmailTable<T> withColumn(J2HtmlEmailTableColumn<T> column){
		j2HtmlEmailTableColumns.add(column);
		return this;
	}

	public static class J2HtmlEmailTableColumn<T>{

		private final String name;
		private final Function<T,DomContent> valueFunction;
		private final List<String> styles;

		public J2HtmlEmailTableColumn(String name, Function<T,DomContent> valueFunction){
			this.name = name;
			this.valueFunction = valueFunction;
			this.styles = new ArrayList<>();
		}

		public static <T> J2HtmlEmailTableColumn<T> ofNumber(String name, Function<T,Number> valueFunction){
			return J2HtmlEmailTableColumn.ofText(name, valueFunction.andThen(NumberFormatter::addCommas))
					.withStyle("text-align:right");
		}

		public static <T> J2HtmlEmailTableColumn<T> ofText(String name, Function<T,Object> valueFunction){
			return new J2HtmlEmailTableColumn<>(name, row -> {
				try{
					Object value = valueFunction.apply(row);
					String text = Optional.ofNullable(value).map(Object::toString).orElse("");
					return TagCreator.text(text);
				}catch(Exception e){
					throw new RuntimeException("error column=" + name + " row=" + GsonTool.GSON.toJson(row), e);
				}
			});
		}

		public J2HtmlEmailTableColumn<T> withStyle(String style){
			styles.add(style);
			return this;
		}

	}

	public static class J2HtmlEmailTableRow<T>{

		private final T value;
		private final List<String> styles;

		public J2HtmlEmailTableRow(T value){
			this.value = value;
			this.styles = new ArrayList<>();
		}

		public J2HtmlEmailTableRow<T> withStyle(String style){
			styles.add(style);
			return this;
		}

	}

	public TableTag build(Collection<T> values){
		return build(values, J2HtmlEmailTableRow::new);
	}

	public TableTag build(Collection<T> values, Function<T,J2HtmlEmailTableRow<T>> rowFunction){
		Collection<J2HtmlEmailTableRow<T>> rows = Scanner.of(values).map(rowFunction).list();
		boolean includeHead = Scanner.of(j2HtmlEmailTableColumns)
				.map(column -> column.name)
				.anyMatch(Objects::nonNull);
		var thead = thead(tr(each(j2HtmlEmailTableColumns, column -> th(column.name)
				.withStyle(String.join(";", column.styles) + ";"))));
		var tbody = tbody(each(rows, this::makeTr));
		return table()
			.attr(Attr.BORDER, 1)
			.attr("cellpadding", 5)
			.withStyle("border-collapse:collapse;")
			.condWith(includeHead, thead)
			.with(tbody);
	}

	private TrTag makeTr(J2HtmlEmailTableRow<T> row){
		var tableRow = TagCreator.tr();
		if(!row.styles.isEmpty()){
			tableRow.withStyle(String.join(";", row.styles) + ";");
		}
		return tableRow.with(each(j2HtmlEmailTableColumns, column -> makeTd(column, row.value)));
	}

	private TdTag makeTd(J2HtmlEmailTableColumn<T> column, T row){
		var td = TagCreator.td(column.valueFunction.apply(row));
		if(!column.styles.isEmpty()){
			td.withStyle(String.join(";", column.styles) + ";");
		}
		return td;
	}

}