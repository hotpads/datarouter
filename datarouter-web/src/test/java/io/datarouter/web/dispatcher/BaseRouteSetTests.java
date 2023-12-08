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
package io.datarouter.web.dispatcher;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.auth.role.DatarouterUserRole;
import io.datarouter.auth.role.RoleEnum;

public class BaseRouteSetTests{

	public static final String ANON_PATH = "/anon";

	public static <T extends RoleEnum<T>> String getPathForRole(RoleEnum<T> role){
		return "/" + role.getPersistentString();
	}

	public static final BaseRouteSet testRouteSet = new BaseRouteSet(){
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

		Pattern childPathsAndQueryParams = Pattern.compile(prefix + BaseRouteSet.MATCHING_CHILD_PATHS_AND_QUERY_PARAMS);
		Assert.assertTrue(childPathsAndQueryParams.matcher(prefix).matches());
		Assert.assertTrue(childPathsAndQueryParams.matcher(prefix + "/").matches());
		Assert.assertTrue(childPathsAndQueryParams.matcher(prefix + "/sub-path/sub_sub_path").matches());
		Assert.assertTrue(childPathsAndQueryParams.matcher(prefix + "?queryParam=value").matches());

		Assert.assertFalse(childPathsAndQueryParams.matcher(prefix + prefix).matches());
		Assert.assertFalse(childPathsAndQueryParams.matcher(prefix + "a/").matches());
		Assert.assertFalse(childPathsAndQueryParams.matcher("").matches());
		Assert.assertFalse(childPathsAndQueryParams.matcher("/abc/efg").matches());
	}

}
