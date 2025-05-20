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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.instrumentation.relay.rml.Rml;

public class DatarouterRelayRenderToolTests{

	@Test
	public void test(){
		String textOnly = DatarouterRelayRenderTool.renderTextOnly(Rml.doc(
				Rml.heading(1, Rml.text("Heading")),
				Rml.paragraph(Rml.text("Paragraph "), Rml.text("link").link("link")),
				Rml.unorderedList(
						Rml.listItem(Rml.text("Item 1")),
						Rml.listItem(Rml.text("Item 2"))))
				.build());

		Assert.assertEquals(textOnly, "Heading Paragraph link Item 1, Item 2");
	}

}
