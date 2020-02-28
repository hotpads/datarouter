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
package io.datarouter.web.dispatcher;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiKeyPredicateTests{

	@Test
	public void obfuscateTest(){
		Assert.assertEquals(ApiKeyPredicate.obfuscate("secret"), "se**et");
		Assert.assertEquals(ApiKeyPredicate.obfuscate("pzazz"), "pz**z");
		Assert.assertEquals(ApiKeyPredicate.obfuscate("1234"), "1**4");
		Assert.assertEquals(ApiKeyPredicate.obfuscate("αβγ"), "α**");
		Assert.assertEquals(ApiKeyPredicate.obfuscate("ab"), "**");
		Assert.assertEquals(ApiKeyPredicate.obfuscate("4T(F~Q2`\\e7<PW@Q"), "4T************@Q");
	}

}
