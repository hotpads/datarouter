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
package io.datarouter.web.email;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.text;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import j2html.tags.DomContent;
import j2html.tags.Text;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class StandardDatarouterEmailHeaderService{

	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerName serverName;
	@Inject
	private EnvironmentName environmentName;

	public DivTag makeStandardHeader(){
		return makeStandardHeaderWithSupplements(List.of());
	}

	public DivTag makeStandardHeaderWithSupplements(List<EmailHeaderRow> supplements){
		List<HtmlEmailHeaderRow> rows = new ArrayList<>();
		rows.add(new HtmlEmailHeaderRow("environment", makeText(environmentName.get())));
		rows.add(new HtmlEmailHeaderRow("service", makeText(serviceName.get())));
		rows.add(new HtmlEmailHeaderRow("serverName", makeText(serverName.get())));
		supplements.forEach(row -> rows.add(new HtmlEmailHeaderRow(row.header(), text(row.value()))));
		var table = new J2HtmlEmailTable<HtmlEmailHeaderRow>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.header())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, HtmlEmailHeaderRow::value))
				.build(rows);
		return div(table, br());
	}

	public DivTag makeStandardHeaderWithSupplementsHtml(List<HtmlEmailHeaderRow> supplements){
		List<HtmlEmailHeaderRow> rows = new ArrayList<>();
		rows.add(new HtmlEmailHeaderRow("environment", makeText(environmentName.get())));
		rows.add(new HtmlEmailHeaderRow("service", makeText(serviceName.get())));
		rows.add(new HtmlEmailHeaderRow("serverName", makeText(serverName.get())));
		supplements.forEach(row -> rows.add(new HtmlEmailHeaderRow(row.header(), row.value())));
		var table = new J2HtmlEmailTable<HtmlEmailHeaderRow>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.header())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, HtmlEmailHeaderRow::value))
				.build(rows);
		return div(table, br());
	}

	private DivTag makeDivBoldRight(String text){
		return div(text)
				.withStyle("font-weight:bold;text-align:right;");
	}

	private Text makeText(String text){
		return text(text);
	}

	public record EmailHeaderRow(
			String header,
			String value){
	}

	public record HtmlEmailHeaderRow(
			String header,
			DomContent value){
	}

}
