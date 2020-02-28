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

import java.util.Arrays;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.service.RoleEnum;

public class BaseRouteSetTests{

	public static final String ANON_PATH = "/anon";

	public static final <T extends RoleEnum<T>> String getPathForRole(RoleEnum<T> role){
		return "/" + role.getPersistentString();
	}

	public static final BaseRouteSet testRouteSet = new BaseRouteSet(""){
		{
			handle(ANON_PATH).allowAnonymous();
			Arrays.stream(DatarouterUserRole.values())
					.forEach(role -> handle(getPathForRole(role)).allowRoles(role));
		}
	};

	@Test
	public void testMatches(){
		String prefix = "fjalfdja";
		String suffix = "dfadfqeq";

		Pattern prefixPattern = Pattern.compile(prefix + BaseRouteSet.MATCHING_ANY);
		Assert.assertTrue(prefixPattern.matcher(prefix + "qefadfaf").matches());
		Assert.assertTrue(prefixPattern.matcher(prefix + "/qefadfaf").matches());
		Assert.assertTrue(prefixPattern.matcher(prefix + "/qef/adfaf").matches());

		Assert.assertFalse(prefixPattern.matcher("/asae" + prefix + "/qef/adfaf").matches());
		Assert.assertFalse(prefixPattern.matcher("/asae/" + prefix + "/qef/adfaf").matches());

		Pattern suffixPattern = Pattern.compile(BaseRouteSet.MATCHING_ANY + suffix);
		Assert.assertTrue(suffixPattern.matcher("fjalfdja" + suffix).matches());
		Assert.assertTrue(suffixPattern.matcher("/fjalfdja" + suffix).matches());
		Assert.assertTrue(suffixPattern.matcher("/fjalfdja/" + suffix).matches());
		Assert.assertTrue(suffixPattern.matcher("/fjal/fdja" + suffix).matches());

		Assert.assertFalse(suffixPattern.matcher(suffix + "adfa").matches());
		Assert.assertFalse(suffixPattern.matcher("fjalfdja" + suffix + "adfa").matches());

		Pattern oneDirectoryPattern = Pattern.compile(BaseRouteSet.REGEX_ONE_DIRECTORY);
		Assert.assertTrue(oneDirectoryPattern.matcher("").matches());
		Assert.assertTrue(oneDirectoryPattern.matcher("abcd").matches());
		Assert.assertTrue(oneDirectoryPattern.matcher("/").matches());
		Assert.assertTrue(oneDirectoryPattern.matcher("/abcd").matches());

		Assert.assertFalse(oneDirectoryPattern.matcher("//abcd").matches());
		Assert.assertFalse(oneDirectoryPattern.matcher("/abcd/").matches());
		Assert.assertFalse(oneDirectoryPattern.matcher("/abc/efg").matches());
	}

}
