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
package io.datarouter.util.string;

import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

public class XmlStringTool{

	private static final Pattern AMP = Pattern.compile("&[Aa][Mm][Pp];");
	private static final Pattern LT = Pattern.compile("&[Ll][Tt];");
	private static final Pattern GG_TT = Pattern.compile("&[Gg][Tt];");
	private static final Pattern SINGLE_QUOTE = Pattern.compile("&[Aa][Pp][Oo][Ss];");
	private static final Pattern DOUBLE_QUOTE = Pattern.compile("&[Qq][Uu][Oo][Tt];");
	private static final Pattern NBSP = Pattern.compile("&[Nn][Bb][Ss][Pp];");
	private static final Pattern AMP_CHAR = Pattern.compile("&");
	private static final Pattern LT_CHAR = Pattern.compile("<");
	private static final Pattern GT_CHAR = Pattern.compile(">");
	private static final Pattern SINGLE_QUOTE_CHAR = Pattern.compile("'");
	private static final Pattern DOUBLE_QUOTE_CHAR = Pattern.compile("\"");

	public static String escapeXml(String input){
		if(input == null){
			return null;
		}
		input = StringTool.removeNonStandardCharacters(input);
		input = AMP.matcher(input).replaceAll("&");
		input = LT.matcher(input).replaceAll("<");
		input = GG_TT.matcher(input).replaceAll(">");
		input = SINGLE_QUOTE.matcher(input).replaceAll("'");
		input = DOUBLE_QUOTE.matcher(input).replaceAll("\"");
		input = NBSP.matcher(input).replaceAll(" ");
		input = AMP_CHAR.matcher(input).replaceAll("&amp;"); // must do the ampersand first
		input = LT_CHAR.matcher(input).replaceAll("&lt;");
		input = GT_CHAR.matcher(input).replaceAll("&gt;");
		input = SINGLE_QUOTE_CHAR.matcher(input).replaceAll("&apos;");
		input = DOUBLE_QUOTE_CHAR.matcher(input).replaceAll("&quot;");

		return input;
	}

	public static class XmlStringToolTests{

		@Test
		public void testEscapeXml(){
			Assert.assertEquals(escapeXml("Baseboard &amp; Crown Moldings Throughout; "),
					"Baseboard &amp; Crown Moldings Throughout; ", "test &amp; for &amp;");
			Assert.assertEquals(escapeXml("Baseboard &Amp; Crown Moldings Throughout; "),
					"Baseboard &amp; Crown Moldings Throughout; ", "test &amp; for &Amp;");
			Assert.assertEquals(escapeXml("Baseboard & Crown Moldings Throughout; "),
					"Baseboard &amp; Crown Moldings Throughout; ", "test &amp; for &");
			Assert.assertEquals(escapeXml("<wee>steve \"steve-o\" o'malley & fred \"the dagger\" dirkowitz</wee>"),
					"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley "
					+ "&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;", "test <>\"'&");
			Assert.assertEquals(escapeXml("&lt;wee&gt;steve &quot;steve-o&quot; o&apos;"
					+ "malley &amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;"),
					"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley "
					+ "&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
					"test &lt;&gt;&quot;&apos;&amp;");
			Assert.assertEquals(escapeXml("&lT;wee&Gt;steve &QUOT;steve-o&qUoT; o&aPoS;"
					+ "malley &aMP; fred &Quot;the dagger&Quot; " + "dirkowitz&LT;/wee&gt;"),
					"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley "
							+ "&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
					"test &lt;&gt;&quot;&apos;&amp; with various cases");
		}

	}

}
