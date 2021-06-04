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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Java9;
import io.datarouter.util.process.AnsiParsingScanner.AnsiParsedLine;
import io.datarouter.util.tuple.Pair;

public class AnsiParsingScannerTests{

	@Test
	public void testNoHtml(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of(
				"no html",
				"normal text")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("no html", "normal text"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("no html", false),
				new Pair<>("normal text", false)));
	}

	@Test
	public void testHtml(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of("\u001B[92mHello World!")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Hello World!"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("<span class=\"sgr-92\">Hello World!</span>", true)));
	}

	@Test
	public void testMixedContent(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of(
				"\u001B[01mNeeds html",
				"No html",
				"\u001B[92mBack to html")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Needs html", "No html", "Back to html"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("<span class=\"sgr-01\">Needs html</span>", true),
				new Pair<>("No html", false),
				new Pair<>("<span class=\"sgr-92\">Back to html</span>", true)));
	}

	@Test
	public void testMultiModifierHtml(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of("\u001B[33;01;47mMany classes")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Many classes"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("<span class=\"sgr-33 sgr-01 sgr-47\">Many classes</span>", true)));
	}

	@Test
	public void testStackedModifierHtml(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of("\u001B[33m\u001B[44mStacked classes")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Stacked classes"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("<span class=\"sgr-33 sgr-44\">Stacked classes</span>", true)));
	}

	@Test
	public void testChangingStyleHtml(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of("\u001B[33mHello \u001B[44mWorld\u001B[0m!")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Hello World!"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("<span class=\"sgr-33\">Hello </span><span class=\"sgr-33 sgr-44\">World</span>!", true)));
	}

	@Test
	public void testTrailingHtml(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of("Hello \u001B[44mWorld!")
				.link(scanner -> new AnsiParsingScanner(scanner, "sgr-", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Hello World!"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("Hello <span class=\"sgr-44\">World!</span>", true)));
	}

	@Test
	public void testSpecifyClassPrefix(){
		List<Pair<String,Boolean>> consumedLogs = new ArrayList<>();
		Consumer<AnsiParsedLine> logConsumer = line -> consumedLogs.add(new Pair<>(line.line, line.isHtml));

		List<String> output = Scanner.of("\u001B[92mHello World!")
				.link(scanner -> new AnsiParsingScanner(scanner, "different-prefix_", logConsumer))
				.list();

		Assert.assertEquals(output, Java9.listOf("Hello World!"));
		Assert.assertEquals(consumedLogs, Java9.listOf(
				new Pair<>("<span class=\"different-prefix_92\">Hello World!</span>", true)));
	}

}
