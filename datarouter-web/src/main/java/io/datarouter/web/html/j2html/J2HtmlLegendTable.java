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

import static j2html.TagCreator.b;
import static j2html.TagCreator.caption;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.join;
import static j2html.TagCreator.p;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.List;

import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class J2HtmlLegendTable{

	private final List<String> classes = new ArrayList<>();
	private final List<J2HtmlTableEntry> entries = new ArrayList<>();

	private String tableHeader;
	private String caption;
	private boolean singleRow = true;

	public J2HtmlLegendTable withClass(String clazz){
		this.classes.add(clazz);
		return this;
	}

	public J2HtmlLegendTable withEntry(String name, long value){
		return withEntry(name, value + "");
	}

	public J2HtmlLegendTable withEntry(String name, int value){
		return withEntry(name, value + "");
	}

	public J2HtmlLegendTable withEntry(String name, String value){
		entries.add(new J2HtmlTableEntry(name, value));
		return this;
	}

	public J2HtmlLegendTable withEntry(String name, String value, String classes){
		entries.add(new J2HtmlTableEntry(name, value, classes));
		return this;
	}

	public J2HtmlLegendTable withHeader(String tableHeader){
		this.tableHeader = tableHeader;
		return this;
	}

	public J2HtmlLegendTable withCaption(String caption){
		this.caption = caption;
		return this;
	}

	public J2HtmlLegendTable withSingleRow(boolean singleRow){
		this.singleRow = singleRow;
		return this;
	}

	public DivTag build(){
		var table = table()
				.withClasses(classes.toArray(String[]::new))
				.condWith(caption != null, caption(caption));
		entries.stream()
				.map(this::makeTr)
				.forEach(table::with);
		if(tableHeader == null){
			return div(table);
		}
		return div(h4(tableHeader), table);
	}

	private TrTag makeTr(J2HtmlTableEntry entry){
		TrTag row = tr();
		if(entry.classes != null){
			row.withClass(entry.classes);
		}
		if(singleRow){
			row.with(td(p(join(b(entry.key), " - ", entry.value))));
		}else{
			row.with(td(p(b(entry.key), td(entry.value))));
		}
		return row;
	}

	private static class J2HtmlTableEntry{

		private final String key;
		private final String value;
		private final String classes;

		public J2HtmlTableEntry(String key, String value){
			this(key, value, null);
		}

		public J2HtmlTableEntry(String key, String value, String classes){
			this.key = key;
			this.value = value;
			this.classes = classes;
		}

	}

}
