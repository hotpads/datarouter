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

import org.testng.Assert;
import org.testng.annotations.Test;

public class XmlStringToolTests{

	@Test
	public void testEscapeXml(){
		Assert.assertEquals(XmlStringTool.escapeXml("Baseboard &amp; Crown Moldings Throughout; "),
				"Baseboard &amp; Crown Moldings Throughout; ", "test &amp; for &amp;");
		Assert.assertEquals(XmlStringTool.escapeXml("Baseboard &Amp; Crown Moldings Throughout; "),
				"Baseboard &amp; Crown Moldings Throughout; ", "test &amp; for &Amp;");
		Assert.assertEquals(XmlStringTool.escapeXml("Baseboard & Crown Moldings Throughout; "),
				"Baseboard &amp; Crown Moldings Throughout; ", "test &amp; for &");
		Assert.assertEquals(XmlStringTool.escapeXml(
				"<wee>steve \"steve-o\" o'malley & fred \"the dagger\" dirkowitz</wee>"),
				"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley "
				+ "&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;", "test <>\"'&");
		Assert.assertEquals(XmlStringTool.escapeXml("&lt;wee&gt;steve &quot;steve-o&quot; o&apos;"
				+ "malley &amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;"),
				"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley "
				+ "&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
				"test &lt;&gt;&quot;&apos;&amp;");
		Assert.assertEquals(XmlStringTool.escapeXml("&lT;wee&Gt;steve &QUOT;steve-o&qUoT; o&aPoS;"
				+ "malley &aMP; fred &Quot;the dagger&Quot; " + "dirkowitz&LT;/wee&gt;"),
				"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley "
						+ "&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
				"test &lt;&gt;&quot;&apos;&amp; with various cases");
	}

}
