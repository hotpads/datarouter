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
package io.datarouter.storage.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.storage.file.PathbeanKey;

public class PathbeanKeyTests{

	@Test
	public void testConstruction(){
		var key1 = new PathbeanKey("path1/path2/path3/", "file");
		Assert.assertEquals(key1.getPath(), "path1/path2/path3/");
		Assert.assertEquals(key1.getFile(), "file");

		var key2 = PathbeanKey.of("path1/path2/file");
		Assert.assertEquals(key2.getPath(), "path1/path2/");
		Assert.assertEquals(key2.getFile(), "file");
	}

	@Test
	public void testFindFirstPathSegment(){
		var key1 = new PathbeanKey("path1/path2/path3/", "file");
		Assert.assertEquals(key1.findFirstPathSegment().get(), "path1");

		var key2 = PathbeanKey.of("path1/path2/file");
		Assert.assertEquals(key2.findFirstPathSegment().get(), "path1");

		var key3 = PathbeanKey.of("file");
		Assert.assertTrue(key3.findFirstPathSegment().isEmpty());
	}

}
