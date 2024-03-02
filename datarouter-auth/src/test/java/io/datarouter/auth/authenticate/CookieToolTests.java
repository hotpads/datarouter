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
package io.datarouter.auth.authenticate;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.auth.util.CookieTool;

public class CookieToolTests{

	@Test
	public void getMapFromString(){
		String string = "key1: val1;key2: val2";
		Map<String,String> res = CookieTool.getMapFromString(string, ";", ": ");
		Assert.assertEquals(res.size(), 2);
		Assert.assertEquals(res.get("key2"), "val2");
	}

}
