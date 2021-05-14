/**
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
package io.datarouter.util.process;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.Scanner;

public class AnsiParsingScanner extends BaseLinkedScanner<String,String>{

	private static final Pattern RESET = Pattern.compile("\u001B\\[0?m");
	private static final Pattern SGR = Pattern.compile("\u001B\\[(?<color>0?[\\d;]+)m");

	private final String htmlClassNamePrefix;
	private final Consumer<AnsiParsedLine> lineConsumer;

	public AnsiParsingScanner(Scanner<String> input, String htmlClassNamePrefix, Consumer<AnsiParsedLine> lineConsumer){
		super(input);
		this.htmlClassNamePrefix = htmlClassNamePrefix;
		this.lineConsumer = lineConsumer;
	}

	private String logLineAndStripColoring(String line){
		AnsiParsedLine parsed = parseLine(line, htmlClassNamePrefix);
		lineConsumer.accept(parsed);
		return parsed.text;
	}

	@Override
	protected boolean advanceInternal(){
		if(input.advance()){
			current = logLineAndStripColoring(input.current());
			return true;
		}
		return false;
	}

	public static AnsiParsedLine parseLine(String line, String htmlClassNamePrefix){
		StringBuilder text = new StringBuilder();
		StringBuilder html = new StringBuilder();

		Matcher resetMatcher = RESET.matcher(line);
		Matcher sgrMatcher = SGR.matcher(line);

		boolean isHtml = false;
		int depth = 0;
		int lineIndex = 0;
		while(true){
			boolean resetMatch = resetMatcher.find(lineIndex);
			boolean sgrMatch = sgrMatcher.find(lineIndex);

			if(resetMatch && (!sgrMatch || sgrMatcher.start() >= resetMatcher.start())){
				appendBeforeMatch(line, lineIndex, resetMatcher, text, html);
				appendHtmlClosingTag(depth, html);
				depth = 0;
				lineIndex = resetMatcher.end();
			}else if(sgrMatch){
				isHtml = true;
				appendBeforeMatch(line, lineIndex, sgrMatcher, text, html);
				String color = sgrMatcher.group("color");
				html.append("<span class=\"" + String.join(" ", parseColorClasses(color, htmlClassNamePrefix)) + "\">");
				depth++;
				lineIndex = sgrMatcher.end();
			}else{
				appendSubstring(line, lineIndex, line.length(), text, html);
				appendHtmlClosingTag(depth, html);
				depth = 0;
				break;
			}
		}
		String strippedLine = text.toString();
		return new AnsiParsedLine(isHtml, isHtml ? html.toString() : strippedLine, strippedLine);
	}

	private static void appendSubstring(String line, int lineStartIndex, int lineEndIndex, StringBuilder...sbs){
		String sinceLastMatch = line.substring(lineStartIndex, lineEndIndex);
		for(StringBuilder sb : sbs){
			sb.append(sinceLastMatch);
		}
	}

	private static void appendBeforeMatch(String line, int lineIndex, Matcher matchedMatcher, StringBuilder...sbs){
		appendSubstring(line, lineIndex, matchedMatcher.start(), sbs);
	}

	private static void appendHtmlClosingTag(int depth, StringBuilder sb){
		for(int i = 0; i < depth; ++i){
			sb.append("</span>");
		}
	}

	private static List<String> parseColorClasses(String color, String htmlClassNamePrefix){
		return Scanner.of(color.split(";"))
				.exclude(String::isEmpty)
				.map(htmlClassNamePrefix::concat)
				.list();
	}

	public static class AnsiParsedLine{

		public final boolean isHtml;
		public final String line;
		public final String text;

		private AnsiParsedLine(boolean isHtml, String line, String text){
			this.isHtml = isHtml;
			this.line = line;
			this.text = text;
		}

	}

}
