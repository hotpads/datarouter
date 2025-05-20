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
package io.datarouter.instrumentation.relay.rml;

import java.util.List;

import io.datarouter.instrumentation.relay.dto.RelayMessageBlockDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageMarkDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockMediaType;

public class Rml{

	public static RmlDoc doc(RmlBlock... content){
		return doc(List.of(content));
	}

	public static RmlDoc doc(List<RmlBlock> content){
		return new RmlDoc(content);
	}

	public static RmlHeading heading(int level, RmlBlock... content){
		return heading(level, List.of(content));
	}

	public static RmlHeading heading(int level, List<RmlBlock> content){
		return new RmlHeading(level, content);
	}

	public static RmlParagraph paragraph(RmlBlock... content){
		return paragraph(List.of(content));
	}

	public static RmlParagraph paragraph(List<RmlBlock> content){
		return new RmlParagraph(content);
	}

	public static RmlFields fields(RmlBlock... content){
		return fields(List.of(content));
	}

	public static RmlFields fields(List<RmlBlock> content){
		return new RmlFields(content);
	}

	public static RmlTable table(RmlBlock... content){
		return table(List.of(content));
	}

	public static RmlTable table(List<RmlBlock> content){
		return new RmlTable(content);
	}

	public static RmlTableRow tableRow(RmlBlock... content){
		return tableRow(List.of(content));
	}

	public static RmlTableRow tableRow(List<RmlBlock> content){
		return new RmlTableRow(content);
	}

	public static RmlTableHeader tableHeader(RmlBlock... content){
		return tableHeader(List.of(content));
	}

	public static RmlTableHeader tableHeader(List<RmlBlock> content){
		return new RmlTableHeader(content);
	}

	public static RmlTableCell tableCell(RmlBlock... content){
		return tableCell(List.of(content));
	}

	public static RmlTableCell tableCell(List<RmlBlock> content){
		return new RmlTableCell(content);
	}

	public static RmlLink link(String href, RmlBlock... content){
		return link(href, List.of(content));
	}

	public static RmlLink link(String href, List<RmlBlock> content){
		return new RmlLink(href, content);
	}

	public static RmlListItem listItem(RmlBlock... content){
		return listItem(List.of(content));
	}

	public static RmlListItem listItem(List<RmlBlock> content){
		return new RmlListItem(content);
	}

	public static RmlOrderedList orderedList(RmlBlock... content){
		return orderedList(List.of(content));
	}

	public static RmlOrderedList orderedList(List<RmlBlock> content){
		return new RmlOrderedList(content);
	}

	public static RmlUnorderedList unorderedList(RmlBlock... content){
		return unorderedList(List.of(content));
	}

	public static RmlUnorderedList unorderedList(List<RmlBlock> content){
		return new RmlUnorderedList(content);
	}

	public static RmlDefinitionList definitionList(RmlBlock... content){
		return definitionList(List.of(content));
	}

	public static RmlDefinitionList definitionList(List<RmlBlock> content){
		return new RmlDefinitionList(content);
	}

	public static RmlDefinitionTerm definitionTerm(RmlBlock... content){
		return definitionTerm(List.of(content));
	}

	public static RmlDefinitionTerm definitionTerm(List<RmlBlock> content){
		return new RmlDefinitionTerm(content);
	}

	public static RmlDefinitionDescription definitionDescription(RmlBlock... content){
		return definitionDescription(List.of(content));
	}

	public static RmlDefinitionDescription definitionDescription(List<RmlBlock> content){
		return new RmlDefinitionDescription(content);
	}

	public static RmlText text(String text){
		return text(text, List.of());
	}

	public static RmlText text(String text, RelayMessageMarkDto... marks){
		return text(text, List.of(marks));
	}

	public static RmlText text(String text, List<RelayMessageMarkDto> marks){
		return new RmlText(text, marks);
	}

	public static RmlTimestamp timestamp(String text, Long epochMs){
		return new RmlTimestamp(text, epochMs);
	}

	public static RmlHardBreak hardBreak(){
		return new RmlHardBreak();
	}

	public static RmlRule rule(){
		return new RmlRule();
	}

	public static RmlButton button(
			String href,
			RmlBlock... content){
		return button(href, List.of(content));
	}

	public static RmlButton button(
			String href,
			List<RmlBlock> content){
		return new RmlButton(href, content);
	}

	public static RmlCodeBlock codeBlock(String code){
		return new RmlCodeBlock(code);
	}

	public static RmlContainer container(RmlBlock... content){
		return container(List.of(content));
	}

	public static RmlContainer container(List<RmlBlock> content){
		return new RmlContainer(content);
	}

	public static RmlMedia media(RelayMessageBlockMediaType mediaType, String src, String alt){
		return new RmlMedia(mediaType, src, alt);
	}

	public static RmlMention mention(String text, String username, String href){
		return new RmlMention(text, username, href);
	}

	public static RmlBlockQuote blockQuote(RmlBlock... content){
		return blockQuote(List.of(content));
	}

	public static RmlBlockQuote blockQuote(List<RmlBlock> content){
		return new RmlBlockQuote(content);
	}

	public static RmlBlockDto blockDto(RelayMessageBlockDto dto){
		return new RmlBlockDto(dto);
	}

	/**
	 * Not recommended! Please use standard RML block types. This is here to support old notifications that are already
	 * written in HTML. New work should be written in standard RML.
	 */
	@Deprecated
	public static RmlHtml html(String html){
		return new RmlHtml(html);
	}

}
