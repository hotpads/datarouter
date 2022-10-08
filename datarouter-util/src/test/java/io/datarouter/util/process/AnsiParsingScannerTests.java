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
package io.datarouter.util.process;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.process.AnsiParsingScanner.AnsiParsedLine;

public class AnsiParsingScannerTests{

	private record ConsumedLog(
			String line,
			boolean isHtml){
	}

	@Test
	public void testNoHtml(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of(
				"no html",
				"normal text")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("no html", "normal text"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("no html", false),
				new ConsumedLog("normal text", false)));
	}

	@Test
	public void testHtml(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of("\u001B[92mHello World!")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Hello World!"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("<span class=\"sgr-92\">Hello World!</span>", true)));
	}

	@Test
	public void testMixedContent(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of(
				"\u001B[01mNeeds html",
				"No html",
				"\u001B[92mBack to html")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Needs html", "No html", "Back to html"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("<span class=\"sgr-01\">Needs html</span>", true),
				new ConsumedLog("No html", false),
				new ConsumedLog("<span class=\"sgr-92\">Back to html</span>", true)));
	}

	@Test
	public void testMultiModifierHtml(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of("\u001B[33;01;47mMany classes")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Many classes"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("<span class=\"sgr-33 sgr-01 sgr-47\">Many classes</span>", true)));
	}

	@Test
	public void testStackedModifierHtml(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of("\u001B[33m\u001B[44mStacked classes")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Stacked classes"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("<span class=\"sgr-33 sgr-44\">Stacked classes</span>", true)));
	}

	@Test
	public void testChangingStyleHtml(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of("\u001B[33mHello \u001B[44mWorld\u001B[0m!")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Hello World!"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog(
						"<span class=\"sgr-33\">Hello </span><span class=\"sgr-33 sgr-44\">World</span>!",
						true)));
	}

	@Test
	public void testTrailingHtml(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of("Hello \u001B[44mWorld!")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Hello World!"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("Hello <span class=\"sgr-44\">World!</span>", true)));
	}

	@Test
	public void testSpecifyClassPrefix(){
		List<ConsumedLog> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new ConsumedLog(line.line(), line.isHtml()));

		List<String> output = Scanner.of("\u001B[92mHello World!")
				.link(scanner -> new AnsiParsingScanner(scanner, "different-prefix_", logConsumer))
				.list();

		Assert.assertEquals(output, List.of("Hello World!"));
		Assert.assertEquals(consumedLogs, List.of(
				new ConsumedLog("<span class=\"different-prefix_92\">Hello World!</span>", true)));
	}

}
