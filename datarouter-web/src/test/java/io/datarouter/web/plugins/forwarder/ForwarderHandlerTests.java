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
package io.datarouter.web.plugins.forwarder;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;

public class ForwarderHandlerTests{

	@Test
	public void testValidateCallbackUrl(){
		String currentHost = "test.domain.com";
		Scanner.of(
				"/",
				"/a",
				"http://localhost:3000/a",
				"http://test.domain.com/",
				"https://test.domain.com/a?k=v")
				.forEach(host -> ForwarderHandlerPage.validateCallbackUrl(host, currentHost));
		Scanner.of(
				"",
				"a",
				"file://test.domain.com/",
				"https://wrong.domain.com")
				.forEach(host -> Assert.assertThrows(
						() -> ForwarderHandlerPage.validateCallbackUrl(host, currentHost)));
	}

}
