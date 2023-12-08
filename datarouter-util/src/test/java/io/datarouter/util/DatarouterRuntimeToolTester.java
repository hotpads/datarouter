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

import java.time.Duration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DatarouterRuntimeToolTester{

	@Test
	public void testPwd(){
		RunNativeDto result = DatarouterRuntimeTool.runNative("pwd");
		Assert.assertEquals(result.exitVal(), 0);
		Assert.assertFalse(result.stdout().isEmpty());
		Assert.assertTrue(result.stderr().isEmpty());
	}

	@Test(expectedExceptions = RuntimeException.class,
			expectedExceptionsMessageRegExp = "^Process interrupted from timeout.*")
	public void testThrow(){
		DatarouterRuntimeTool.runNative(Duration.ofMillis(10), "sleep 10");
	}

}
