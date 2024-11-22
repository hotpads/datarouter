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
package io.datarouter.relay;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.instrumentation.relay.dto.RelayMessageBlockAttrsDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageBlockDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockColor;
import io.datarouter.scanner.Scanner;

public class DatarouterRelayRenderTool{

	public static String renderTextOnly(RelayMessageBlockDto dto){
		Stream<String> children = Objects.requireNonNullElse(dto.content(), List.<RelayMessageBlockDto>of())
				.stream()
				.map(DatarouterRelayRenderTool::renderTextOnly);

		return switch(dto.type()){
		case DOC, HEADING, HARD_BREAK, PARAGRAPH, RULE, LIST_ITEM, CONTAINER, MEDIA, TABLE_HEADER, TABLE_CELL, BUTTON ->
				children.collect(Collectors.joining());
		case TEXT, CODE_BLOCK, HTML, TIMESTAMP -> dto.text();
		case MENTION -> dto.attrs().text();
		case FIELDS, TABLE_ROW, ORDERED_LIST, UNORDERED_LIST -> children.collect(Collectors.joining(", "));
		case TABLE -> children.collect(Collectors.joining(" | ", "[", "]"));
		};
	}

	public static RelayMessageBlockDto getShortForm(RelayMessageBlockDto dto){
		RelayMessageBlockDto shortBlock = Optional.ofNullable(dto.attrs())
				.map(RelayMessageBlockAttrsDto::shortFormAlt)
				.orElse(dto);

		return shortBlock.content() == null ? shortBlock : new RelayMessageBlockDto(
				shortBlock.type(),
				shortBlock.text(),
				Scanner.of(shortBlock.content())
						.map(DatarouterRelayRenderTool::getShortForm)
						.list(),
				shortBlock.marks(),
				shortBlock.attrs());
	}

	public static String getStyleColor(RelayMessageBlockColor color){
		return switch(color){
		case DEFAULT -> "#e0e0e0";
		case PRIMARY -> "#006df0";
		case SECONDARY -> "#ffeb3b";
		case SUCCESS -> "#2e7d32";
		case WARNING -> "#ed6c02";
		case INFO -> "#03a9f4";
		case DANGER -> "#d32f2f";
		};
	}

	public static String getStyleContrastTextColor(RelayMessageBlockColor color){
		return switch(color){
		case DEFAULT, SECONDARY -> "#000";
		case PRIMARY, SUCCESS, WARNING, INFO, DANGER -> "#fff";
		};
	}

}
