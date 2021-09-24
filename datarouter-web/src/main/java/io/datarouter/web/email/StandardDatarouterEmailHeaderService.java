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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.util.tuple.Pair;
import io.datarouter.util.tuple.Twin;
import io.datarouter.web.config.service.ServiceName;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

@Singleton
public class StandardDatarouterEmailHeaderService{

	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerName serverName;
	@Inject
	private EnvironmentName environmentName;

	public ContainerTag<?> makeStandardHeader(){
		return makeStandardHeaderWithSupplements(List.of());
	}

	public ContainerTag<?> makeStandardHeaderWithSupplements(List<Pair<String,DomContent>> supplements){
		List<Pair<String,DomContent>> rows = new ArrayList<>();
		rows.add(new Pair<>("environment", makeText(environmentName.get())));
		rows.add(new Pair<>("service", makeText(serviceName.get())));
		rows.add(new Pair<>("serverName", makeText(serverName.get())));
		supplements.forEach(row -> rows.add(new Pair<>(row.getLeft(), row.getRight())));
		var table = new J2HtmlEmailTable<Pair<String,DomContent>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, Pair::getRight))
				.build(rows);
		return div(table, br());
	}

	public ContainerTag<?> makeStandardHeaderWithSupplementsText(List<Twin<String>> supplements){
		List<Pair<String,DomContent>> rows = new ArrayList<>();
		rows.add(new Pair<>("environment", makeText(environmentName.get())));
		rows.add(new Pair<>("service", makeText(serviceName.get())));
		rows.add(new Pair<>("serverName", makeText(serverName.get())));
		supplements.forEach(row -> rows.add(new Pair<>(row.getLeft(), text(row.getRight()))));
		var table = new J2HtmlEmailTable<Pair<String,DomContent>>()
				.withColumn(new J2HtmlEmailTableColumn<>(null, row -> makeDivBoldRight(row.getLeft())))
				.withColumn(new J2HtmlEmailTableColumn<>(null, Pair::getRight))
				.build(rows);
		return div(table, br());
	}

	private DomContent makeDivBoldRight(String text){
		return div(text)
				.withStyle("font-weight:bold;text-align:right;");
	}

	private DomContent makeText(String text){
		return text(text);
	}

}
