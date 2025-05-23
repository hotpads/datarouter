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

import static j2html.TagCreator.a;
import static j2html.TagCreator.br;
import static j2html.TagCreator.code;
import static j2html.TagCreator.div;
import static j2html.TagCreator.em;
import static j2html.TagCreator.head;
import static j2html.TagCreator.i;
import static j2html.TagCreator.img;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ol;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tag;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;
import static j2html.TagCreator.video;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.instrumentation.relay.dto.RelayMessageBlockAttrsDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageBlockDto;
import io.datarouter.instrumentation.relay.dto.RelayMessageMarkDto;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockColor;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockCols;
import io.datarouter.instrumentation.relay.type.RelayMessageBlockType;
import io.datarouter.scanner.Scanner;
import j2html.TagCreator;
import j2html.attributes.Attr;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import j2html.tags.specialized.HeadTag;

public class DatarouterRelayJ2HtmlRenderTool{

	public static final HeadTag HEAD = head(TagCreator.style("""
			a { text-decoration:none; }
			a:hover{ text-decoration:underline; }
			"""));

	private static final int PADDING_PX = 8;
	private static final Map<Integer,Integer> HEADER_FONT_SIZE_BY_LEVEL = Map.of(
			1, 24,
			2, 20,
			3, 16,
			4, 14,
			5, 12,
			6, 10);
	private static final String
			DOC_DEFAULT_STYLE = style(
					"max-width:1000px",
					"overflow:hidden",
					"border:1px solid #00000033",
					"border-radius:6px"),
			DOC_CONTENT_STYLE = style(
					"padding:12px"),
			TABLE_STYLE = style("width:100%", "border-collapse:collapse"),
			DEFINITION_LIST_STYLE = style("border-collapse:collapse"),
			TBODY_STYLE = style("vertical-align:top"),
			PARAGRAPH_STYLE = style("margin:0 0 0.35em"),
			HR_STYLE = style("border-top:1px solid #00000014"),
			HEADING_STYLE = style("line-height:1.167");

	public static DomContent render(RelayMessageBlockDto dto){
		List<RelayMessageBlockDto> childrenBlocks = Objects.requireNonNullElse(dto.content(), List.of());

		if(dto.type() == RelayMessageBlockType.DOC){
			// try to determine header blocks with a background color to omit from content padding
			List<RelayMessageBlockDto> unpaddedBlocks = Scanner.of(childrenBlocks)
					.advanceWhile(block -> Optional.ofNullable(block.attrs())
							.map(RelayMessageBlockAttrsDto::backgroundColor)
							.isPresent())
					.list();
			List<RelayMessageBlockDto> paddedBlocks = childrenBlocks.subList(
					unpaddedBlocks.size(),
					childrenBlocks.size());

			return div()
					.with(unpaddedBlocks.stream()
							.map(DatarouterRelayJ2HtmlRenderTool::render)
							.toList())
					.with(div()
							.with(paddedBlocks.stream()
									.map(DatarouterRelayJ2HtmlRenderTool::render)
									.toList())
							.withStyle(DOC_CONTENT_STYLE))
					.withStyle(parseAttrStyles(dto).docStyle().orElse(null));
		}

		List<DomContent> children = childrenBlocks.stream()
				.map(DatarouterRelayJ2HtmlRenderTool::render)
				.toList();

		StyleAttrs attrStyles = parseAttrStyles(dto);
		RelayMessageBlockColor contextColorOrDefault = attrStyles.contextColorOrDefault();
		Optional<String> blockStyle = attrStyles.blockStyle();

		return switch(dto.type()){
		case DOC -> throw new IllegalArgumentException("Doc is rendered above");
		case HEADING -> tag("h" + dto.attrs().level())
				.with(children)
				.withStyle(style(
						blockStyle.orElse(null),
						"margin:0",
						HEADING_STYLE,
						Optional.ofNullable(HEADER_FONT_SIZE_BY_LEVEL.get(dto.attrs().level()))
								.map("font-size:%dpx"::formatted)
								.orElse(null)));
		case BLOCK_QUOTE -> pre().with(p().with(children))
				.withStyle(style(
						"background-color:#f6f6f6",
						"border-radius:10px",
						"border-color:#dadada",
						"border-width:1px,",
						"padding:%dpx %dpx %dpx %dpx".formatted(
								PADDING_PX / 2,
								PADDING_PX * 2,
								PADDING_PX / 2,
								PADDING_PX * 2)));
		case BUTTON -> a()
				.with(children)
				.withStyle(style(
						"background-color:%s".formatted(DatarouterRelayRenderTool.getStyleColor(contextColorOrDefault)),
						"border-radius:4px",
						"padding:3px 16px",
						"color:%s".formatted(contextColorOrDefault == RelayMessageBlockColor.DEFAULT
								? "inherit"
								: "#fff"),
						"text-decoration:none",
						"display:inline-block"))
				.withHref(dto.attrs().href());
		case TEXT -> blockStyle
				.<DomContent>map(style -> span(renderText(dto)).withStyle(style))
				.orElseGet(() -> renderText(dto));
		case PARAGRAPH -> p()
				.with(children)
				.withStyle(style(
						PARAGRAPH_STYLE,
						blockStyle.orElse(null)));
		case MENTION -> a(dto.text())
				.withHref(dto.attrs().href())
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case CODE_BLOCK -> pre(dto.text())
				.withStyle(style(
						"background-color:#091e420f",
						blockStyle.orElse(null)));
		case FIELDS -> table()
				.withStyle(TABLE_STYLE)
				.with(tbody()
						.withStyle(TBODY_STYLE)
						.with(Scanner.of(children)
								.batch(2)
								.map(row -> Scanner.iterate(0, i -> i + 1)
										.limit(row.size())
										.map(idx -> td(row.get(idx))
												.withStyle(style(
														"width:50%",
														idx % 2 == 0 ? "margin-right:16px" : null)))
										.list())
								.map(tds -> tr().with(tds))
								.list()))
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case TABLE -> table()
				.attr(Attr.BORDER, "1")
				.attr("cellpadding", "5")
				.with(children)
				.withStyle(style(
						"border-collapse:collapse",
						blockStyle.orElse(null)));
		case TABLE_ROW -> tr()
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case TABLE_HEADER -> th()
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case TABLE_CELL -> td()
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case ORDERED_LIST -> ol()
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case UNORDERED_LIST -> ul()
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case LINK -> a()
				.withHref(dto.attrs().href())
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case LIST_ITEM -> li()
				.with(children)
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case DEFINITION_LIST -> table()
				.withStyle(DEFINITION_LIST_STYLE)
				.with(tbody()
						.withStyle(TBODY_STYLE)
						.with(Scanner.of(children)
								.batch(2)
								.map(row -> Scanner.iterate(0, i -> i + 1)
										.limit(row.size())
										.map(idx -> td(row.get(idx))
												.withStyle(style(
														idx % 2 == 0 ? "padding-right:6px" : null)))
										.list())
								.map(tds -> tr().with(tds))
								.list()))
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case HARD_BREAK -> br();
		case RULE -> div().withStyle(HR_STYLE);
		case HTML -> span(new UnescapedText(dto.text()))
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case TIMESTAMP -> span(dto.text())
				.withCondStyle(blockStyle.isPresent(), blockStyle.orElse(null));
		case CONTAINER, DEFINITION_TERM, DEFINITION_DESCRIPTION -> blockStyle.isPresent()
				? span().with(children).withStyle(blockStyle.get())
				: TagCreator.each(children.stream());
		case MEDIA -> (switch(dto.attrs().mediaType()){
				case IMAGE -> img().withSrc(dto.attrs().href());
				case VIDEO -> video().withSrc(dto.attrs().href());
				})
				.withStyle(style(
						"max-width:100%",
						"vertical-align:middle",
						blockStyle.orElse(null)));
		};
	}

	private static DomContent renderText(RelayMessageBlockDto text){
		DomContent content = text(text.text());

		List<RelayMessageMarkDto> marks = Objects.requireNonNullElse(text.marks(), List.of());
		for(RelayMessageMarkDto mark : marks){
			content = switch(mark.type()){
			case CODE -> code(content);
			case EM -> em(content);
			case LINK -> a(content).withHref(mark.attrs().href());
			case UNDERLINE -> span(content).withStyle("text-decoration:underline");
			case STRIKE -> span(content).withStyle("text-decoration:line-through");
			case STRONG -> strong(content);
			case ITALIC -> i(content);
			case MONOSPACE -> span(content).withStyle(style("font-family:monospace", "white-space:pre-wrap"));
			case TEXT_COLOR -> TagCreator.span(content).withStyle("color:" + mark.attrs().color());
			};
		}

		return content;
	}

	private static StyleAttrs parseAttrStyles(RelayMessageBlockDto dto){
		Optional<RelayMessageBlockColor> contextColor = Optional.ofNullable(dto.attrs())
				.map(RelayMessageBlockAttrsDto::contextColor);
		RelayMessageBlockColor contextColorOrDefault = contextColor.orElse(RelayMessageBlockColor.DEFAULT);
		Optional<String> contextStyleColor = contextColor.map(DatarouterRelayRenderTool::getStyleColor);
		Optional<String> backgroundColor = Optional.ofNullable(dto.attrs())
				.map(RelayMessageBlockAttrsDto::backgroundColor)
				.map(color -> "background-color:%s;color:%s".formatted(
						DatarouterRelayRenderTool.getStyleColor(color),
						DatarouterRelayRenderTool.getStyleContrastTextColor(color)));
		Optional<String> alignStyle = alignStyle(dto);
		Optional<String> paddingStyle = paddingStyle(dto);
		Optional<String> docStyle = mergeStyle(List.of(
				Optional.of(DOC_DEFAULT_STYLE),
				paddingStyle,
				alignStyle,
				contextStyleColor.map("border-top:6px solid %s"::formatted)));
		Optional<String> blockStyle = mergeStyle(List.of(
				contextStyleColor.map("border-left:4px solid %s"::formatted),
				backgroundColor,
				paddingStyle,
				alignStyle,
				sizeStyle(dto),
				shapeStyle(dto),
				colsStyle(dto)));
		return new StyleAttrs(contextColorOrDefault, contextStyleColor, docStyle, blockStyle);
	}

	private static Optional<String> mergeStyle(List<Optional<String>> styles){
		String css = Scanner.of(styles)
				.concatOpt(Function.identity())
				.collect(Collectors.joining(";"));
		return css.isEmpty() ? Optional.empty() : Optional.of(css);
	}

	private static Optional<String> colsStyle(RelayMessageBlockDto dto){
		return Optional.of(dto)
				.map(RelayMessageBlockDto::attrs)
				.map(RelayMessageBlockAttrsDto::cols)
				.map(RelayMessageBlockCols::percent)
				.map(percent -> (int)Math.floor(percent))
				.map("display:inline-block;vertical-align:top;width:%d%%"::formatted);
	}

	private static Optional<String> alignStyle(RelayMessageBlockDto dto){
		return Optional.of(dto)
				.map(RelayMessageBlockDto::attrs)
				.map(RelayMessageBlockAttrsDto::align)
				.map(align -> switch(align){
				case LEFT -> "left";
				case RIGHT -> "right";
				case CENTER -> "center";
				})
				.map("text-align:"::concat);
	}

	private static Optional<String> paddingStyle(RelayMessageBlockDto dto){
		return Optional.of(dto)
				.map(RelayMessageBlockDto::attrs)
				.map(RelayMessageBlockAttrsDto::padding)
				.map(padding -> "display:block;padding:%dpx %dpx %dpx %dpx".formatted(
						PADDING_PX * Objects.requireNonNullElse(padding.top(), 0),
						PADDING_PX * Objects.requireNonNullElse(padding.right(), 0),
						PADDING_PX * Objects.requireNonNullElse(padding.bottom(), 0),
						PADDING_PX * Objects.requireNonNullElse(padding.left(), 0)));
	}

	private static Optional<String> sizeStyle(RelayMessageBlockDto dto){
		return Optional.of(dto)
				.map(RelayMessageBlockDto::attrs)
				.map(RelayMessageBlockAttrsDto::size)
				.map(size -> style(
						Optional.ofNullable(size.width()).map("width:"::concat).orElse(null),
						Optional.ofNullable(size.height()).map("height:"::concat).orElse(null)));
	}

	private static Optional<String> shapeStyle(RelayMessageBlockDto dto){
		return Optional.of(dto)
				.map(RelayMessageBlockDto::attrs)
				.map(RelayMessageBlockAttrsDto::shape)
				.map(size -> style(
						Optional.ofNullable(size.borderRadius()).map("border-radius:"::concat).orElse(null)));
	}

	private static String style(String... styles){
		return Scanner.of(styles)
				.exclude(Objects::isNull)
				.collect(Collectors.joining(";"));
	}

	private record StyleAttrs(
			RelayMessageBlockColor contextColorOrDefault,
			Optional<String> contextStyleColor,
			Optional<String> docStyle,
			Optional<String> blockStyle){
	}

}
