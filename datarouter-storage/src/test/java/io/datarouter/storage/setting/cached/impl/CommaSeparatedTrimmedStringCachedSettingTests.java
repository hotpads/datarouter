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
package io.datarouter.storage.setting.cached.impl;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CommaSeparatedTrimmedStringCachedSettingTests{

	@Test
	private void testParseStringValue(){
		Set<String> expectedValues = Set.of("1","2","3","4","5","6","7");
		var commaSeparatedStringCachedSetting = new CommaSeparatedTrimmedStringCachedSetting(null, null, null);
		Set<String> parsedValues = commaSeparatedStringCachedSetting.parseStringValue("1,2, 3, 4 ,5 , 6, 7 ");
		Assert.assertEquals(parsedValues, expectedValues);
	}

	@Test
	private void testToStringValue(){
		Set<String> existingSetOfValues = Set.of("6","2","5","4","3","1");
		String expectedValue = "1,2,3,4,5,6";
		var commaSeparatedStringCachedSetting = new CommaSeparatedTrimmedStringCachedSetting(null, null, null);
		String stringValue = commaSeparatedStringCachedSetting.toStringValue(existingSetOfValues);
		Assert.assertEquals(stringValue, expectedValue);
	}

}
