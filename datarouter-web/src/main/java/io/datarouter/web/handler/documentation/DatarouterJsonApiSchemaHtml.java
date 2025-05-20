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
package io.datarouter.web.handler.documentation;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.s;
import static j2html.TagCreator.span;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.util.List;

import io.datarouter.scanner.Scanner;
import j2html.tags.DomContent;
import j2html.tags.specialized.SpanTag;

public class DatarouterJsonApiSchemaHtml{

	public static DomContent makeSchemaContent(List<ApiDocSchemaDto> schemas, String apiName){
		return div()
				.with(buildInfoTableStyle())
				.withClass("mx-2 my-2 d-flex justify-content-center")
				.with(div()
						.withClass("d-lg-flex d-block")
						.withStyle("max-width: 1920px")
						.with(div()
								.withStyle("width: 350px; flex-shrink: 0")
								.with(buildTableOfContents(schemas)))
						.with(div()
								.withClass("mx-2")
								.with(h1(apiName + " Schema Documentation"),
										div()
												.with(buildSchemas(schemas)))));
	}

	public static List<DomContent> buildSchemas(List<ApiDocSchemaDto> schemas){
		return Scanner.of(schemas)
				.map(schema -> {
					if(schema.getType().equals("enum")){
						return buildEnumContent(schema);
					}else{
						return buildSchemaTableContent(schema);
					}
				})
				.list();
	}

	public static DomContent buildInfoTableStyle(){
		return style(
				"""
				.dr-info-table {
					width: auto;
					margin: 0;
					border-collapse: collapse;
				}
				.dr-info-table tr,
				.dr-info-table td {
					border: none;
					padding: 0;
					background-color: transparent !important;
				}
				.dr-info-table td {
					padding-right: 8px;
					padding-bottom: 4px;
				}
				.dr-info-table td.field {
					font-weight: 500;
				}
				.dr-info-table td:last-child {
					word-break: break-word;
				}
				.dr-info-table tr:last-child td{
					padding-bottom: 0;
				}
				""");
	}

	public static DomContent toFieldType(ApiDocSchemaDto schema, String path){
		String arrayString = schema.isArray() ? "[]" : "";
		if("parameter".equals(schema.getType())){
			SpanTag content = span();
			if(schema.hasFields()){
				content.with(a(schema.toSimpleClassName() + arrayString)
						.withCondTarget(!path.isEmpty(), "_blank")
						.withHref(path + "#" + schema.getClassName()));
			}else{
				content.with(span(schema.toSimpleClassName() + arrayString));
			}
			content.with(span("<"));
			for(int i = 0; i < schema.getParameters().size(); i++){
				ApiDocSchemaDto parameter = schema.getParameters().get(i);
				content.with(toFieldType(parameter, path));
				if(i < schema.getParameters().size() - 1){
					content.with(span(","));
				}
			}
			content.with(span(">"));
			return content;
		}
		if(schema.getClassName() != null){
			return a(schema.toSimpleClassName() + arrayString)
					.withCondTarget(!path.isEmpty(), "_blank")
					.withHref(path + "#" + schema.getClassName());
		}
		return span(schema.getType() + arrayString);
	}

	private static DomContent buildTableOfContents(List<ApiDocSchemaDto> schemas){
		return div()
				.withStyle("max-height: calc(100vh - 100px); overflow-y: auto; position: sticky; top: 0")
				.withClass("border-right border-bottom")
				.with(Scanner.of(schemas)
						.map(schema -> div()
								.withClass("mb-1")
								.withStyle("white-space: nowrap; overflow: hidden; text-overflow: ellipsis")
								.with(a(schema.toSimpleClassName())
										.withHref("#" + schema.getClassName())))
						.list());
	}

	private static DomContent buildEnumContent(ApiDocSchemaDto schema){
		return div().withClass("py-3")
				.with(
						buildHeader(schema),
						buildClassName(schema),
						pre(schema.toEnumString())
								.withStyle("white-space: break-spaces"));
	}

	private static DomContent buildSchemaTableContent(ApiDocSchemaDto schema){
		String columnWidth = String.format("%.2f%%", 100.0 / 3);
		var tableHead = thead(
				tr(
						th("Name")
								.withStyle(String.format("width: %s", columnWidth)),
						th("Type")
								.withStyle(String.format("width: %s", columnWidth)),
						th("Field Info")
								.withStyle(String.format("width: %s", columnWidth))));
		var tableBody = tbody();
		var fields = schema.getFields();
		if(fields != null){
			tableBody.with(
					Scanner.of(fields)
							.map(field -> tr(
									td()
											.condWith(field.isDeprecated(), s(field.getName()))
											.condWith(!field.isDeprecated(), text(field.getName()))
											.withStyle("word-break: break-all"),
									td()
											.withStyle("word-spacing: -3px; word-break: break-all")
											.with(toFieldType(field, "")),
									td()
											.withClass("font-weight-light")
											.withStyle("word-break: break-all")
											.with(buildInfo(field))))
							.list());
		}
		return div().with(
				buildHeader(schema),
				buildClassName(schema),
				table()
						.withStyle("table-layout: fixed; width: 100%")
						.withClass("table table-striped")
						.with(tableHead, tableBody));
	}

	private static DomContent buildInfo(ApiDocSchemaDto field){
		return table()
				.withClass("dr-info-table")
				.with(
						field.isDeprecated() ? tr(td(span("Deprecated")
								.withClass("badge badge-warning"))
								.withClass("field"), td("")) : null,
						field.isOptional() ? tr(td(span("Optional")
								.withClass("badge badge-info")), td("")) : null,
						field.getMaxLength() != null ? tr(td("Max Length:").withClass("field"),
								td(field.getMaxLength().toString()))
								: null,
						field.getMin() != null ? tr(td("Min:").withClass("field"), td(field.getMin().toString()))
								: null,
						field.getMax() != null ? tr(td("Max:").withClass("field"), td(field.getMax().toString()))
								: null,
						field.getDefaultValue() != null ? tr(td("Default:").withClass("field"),
								td(field.getDefaultValue())) : null,
						field.getDescription() != null ? tr(td(field.getDescription()).withColspan("2")) :
								null);
	}

	private static DomContent buildHeader(ApiDocSchemaDto schema){
		return h4()
				.withId(schema.getClassName())
				.with(a(schema.toSimpleClassName())
						.withHref("#" + schema.getClassName()));
	}

	private static DomContent buildClassName(ApiDocSchemaDto schema){
		return p(schema.getClassName())
				.withStyle("word-break: break-all");
	}
}
