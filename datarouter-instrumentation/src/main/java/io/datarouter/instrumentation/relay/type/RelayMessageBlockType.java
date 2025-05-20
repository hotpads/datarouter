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
package io.datarouter.instrumentation.relay.type;

import io.datarouter.enums.StringMappedEnum;

public enum RelayMessageBlockType{
	BLOCK_QUOTE("blockQuote"),
	BUTTON("button"),
	CODE_BLOCK("codeBlock"),
	CONTAINER("container"),
	DOC("doc"),
	FIELDS("fields"),
	HARD_BREAK("hardBreak"),
	HEADING("heading"),
	HTML("html"),
	LINK("link"),
	LIST_ITEM("listItem"),
	MEDIA("media"),
	MENTION("mention"),
	ORDERED_LIST("orderedList"),
	PARAGRAPH("paragraph"),
	RULE("rule"),
	TABLE("table"),
	TABLE_CELL("tableCell"),
	TABLE_HEADER("tableHeader"),
	TABLE_ROW("tableRow"),
	TEXT("text"),
	TIMESTAMP("timestamp"),
	UNORDERED_LIST("unorderedList"),
	DEFINITION_LIST("definitionList"),
	DEFINITION_TERM("definitionTerm"),
	DEFINITION_DESCRIPTION("definitionDescription"),
	;

	public static final StringMappedEnum<RelayMessageBlockType> BY_TYPE
			= new StringMappedEnum<>(values(), el -> el.type);

	private final String type;

	RelayMessageBlockType(String type){
		this.type = type;
	}

}
