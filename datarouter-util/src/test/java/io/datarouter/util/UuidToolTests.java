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
package io.datarouter.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UuidToolTests{

	// the UUID was generated at that timestamp (about 2:38:31 am UTC | Friday, September 18, 2020)
	private static final String V1_UUID = "0a897424-f958-11ea-b38f-d675adfec30a";
	private static final long TIMESTAMP_MS = 1_600_396_711_744L;

	@Test
	public void getTimestampTest(){
		long computed = UuidTool.getTimestamp(V1_UUID).get();
		Assert.assertEquals(computed, TIMESTAMP_MS);
	}

}
