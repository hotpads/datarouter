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
package io.datarouter.aws.s3;

import org.testng.Assert;
import org.testng.annotations.Test;

public class S3ToolTests{

	@Test
	public void testReplaceS3KeyNonSafeCharacters(){
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("abc", "-"), "abc");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("ABC", "-"), "ABC");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("123", "-"), "123");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("!-_.*'()", "-"), "!-_.*'()");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("abcABC123!-_.*'()", "-"), "abcABC123!-_.*'()");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("~!23", "-"), "-!23");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("abc+ABC@123", "-"), "abc-ABC-123");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("abc^ABC|123", "-"), "abc-ABC-123");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("+Appl_2018June.pdf", "-"), "-Appl_2018June.pdf");
		Assert.assertEquals(S3Tool.replaceS3KeyNonSafeCharacters("file name with a plus 2+ 2.png", "-"),
				"file-name-with-a-plus-2--2.png");
	}

}
