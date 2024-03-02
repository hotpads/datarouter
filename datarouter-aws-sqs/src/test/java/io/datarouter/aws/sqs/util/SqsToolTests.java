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
package io.datarouter.aws.sqs.util;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
public class SqsToolTests{

	@Test
	private void testRemoveUnsupportedCharactersNoCharactersRemoved(){
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters(""), "");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters(" "), " ");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("\ud7ff"), "\ud7ff");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("\ue000"), "\ue000");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("some regular text"), "some regular text");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("�double quotes�"), "�double quotes�");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("\u0074\u0065\u0078\u0074"),
				"\u0074\u0065\u0078\u0074");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("Will do ï¿¾\n\n[cid"), "Will do ï¿¾\n\n[cid");
	}

	@Test
	private void testRemoveUnsupportedCharactersCharactersRemoved(){
		Assert.assertTrue(SqsTool.removeUnsupportedCharacters("\ud800").isEmpty());
		Assert.assertTrue(SqsTool.removeUnsupportedCharacters("\ud801").isEmpty());
		Assert.assertTrue(SqsTool.removeUnsupportedCharacters("\udffe").isEmpty());
		Assert.assertTrue(SqsTool.removeUnsupportedCharacters("\udfff").isEmpty());
		Assert.assertTrue(SqsTool.removeUnsupportedCharacters("\ufffe").isEmpty());
		Assert.assertTrue(SqsTool.removeUnsupportedCharacters("\uffff").isEmpty());
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("Will do ￾"), "Will do ");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters("Will ￾ do"), "Will  do");
		Assert.assertEquals(SqsTool.removeUnsupportedCharacters(
				"\u0057\u0069\u006c\u006c\u0020\u0064\u006f\u0020\ufffe"),
				"\u0057\u0069\u006c\u006c\u0020\u0064\u006f\u0020");
	}

}
