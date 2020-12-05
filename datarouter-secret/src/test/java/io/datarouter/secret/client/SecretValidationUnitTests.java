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
package io.datarouter.secret.client;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SecretValidationUnitTests{

	@Test
	public void testValidateName(){
		// ascii and whitespace
		Secret.validateName("abcxyz012789ABCXYZ/_+=.@-");
		Assert.assertThrows(() -> Secret.validateName(new String(new char['a' - 1])));
		Assert.assertThrows(() -> Secret.validateName(new String(new char['z' + 1])));
		Assert.assertThrows(() -> Secret.validateName(new String(new char['A' - 1])));
		Assert.assertThrows(() -> Secret.validateName(new String(new char['Z' + 1])));
		Assert.assertThrows(() -> Secret.validateName(new String(new char['0' - 1])));
		Assert.assertThrows(() -> Secret.validateName(new String(new char['9' + 1])));
		Assert.assertThrows(() -> Secret.validateName("\n"));
		Assert.assertThrows(() -> Secret.validateName("\t"));

		// unicode
		Secret.validateName("\u0061");// a
		Secret.validateName("\u00E1");// á

		// empty or null
		Assert.assertThrows(() -> Secret.validateName(null));
		Assert.assertThrows(() -> Secret.validateName(""));
	}

	@Test
	public void testValidateSecret(){
		Secret.validateSecret(new Secret("abcxyz012789ABCXYZ/_+=.@-", ""));
		Secret.validateSecret(new Secret("abcxyz012789ABCXYZ/_+=.@-", "something"));
		Assert.assertThrows(() -> Secret.validateSecret(new Secret("abcxyz012789ABCXYZ/_+=.@-", null)));

		Assert.assertThrows(() -> Secret.validateSecret(new Secret(null, "")));//etc. from validateName
	}

}
