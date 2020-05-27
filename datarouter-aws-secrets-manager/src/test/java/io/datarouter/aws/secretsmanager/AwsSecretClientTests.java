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
package io.datarouter.aws.secretsmanager;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AwsSecretClientTests{

	@Test
	public void testValidateName(){
		// ascii
		AwsSecretClient.validateNameStatic("abcxyz012789ABCXYZ/_+=.@-");
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(new String(new char['a' - 1])));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(new String(new char['z' + 1])));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(new String(new char['A' - 1])));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(new String(new char['Z' + 1])));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(new String(new char['0' - 1])));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(new String(new char['9' + 1])));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic("\n"));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic("$"));

		// unicode
		AwsSecretClient.validateNameStatic("\u0061");// a
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic("\u00E1"));// á

		// hyphen then 6 characters
		AwsSecretClient.validateNameStatic("-1234567");
		AwsSecretClient.validateNameStatic("-12345");
		AwsSecretClient.validateNameStatic("1-1234567");
		AwsSecretClient.validateNameStatic("1-12345");
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic("-123456"));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic("1-123456"));

		// empty or null
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(null));
		Assert.assertThrows(() -> AwsSecretClient.validateNameStatic(""));
	}

}
