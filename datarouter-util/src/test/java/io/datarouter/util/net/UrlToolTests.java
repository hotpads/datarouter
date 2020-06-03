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
package io.datarouter.util.net;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UrlToolTests{

	private static final String NORMAL_URL = "https://localhost:8443/context/path?first=1&space= &otherSpace=%20&otherO"
			+ "therSpace=+&last=2";

	private static final String RECURSIVE_URL = "https://localhost:8443/context/path?first=1&space= &otherSpace=%20&oth"
			+ "erOtherSpace=+&url=https%3A%2F%2Flocalhost%3A8443%2Fcontext%2Fpath%3Ffirst%3D1%26space%3D%20%26otherSpac"
			+ "e%3D%2520%26otherOtherSpace%3D%2B%26last%3D2&last=2";

	@Test
	public void testEncodeDecode(){
		Assert.assertEquals(UrlTool.decode(UrlTool.encode(NORMAL_URL)), NORMAL_URL);
		Assert.assertEquals(UrlTool.decode(UrlTool.encode(RECURSIVE_URL)), RECURSIVE_URL);
	}

}
