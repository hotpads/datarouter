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
package io.datarouter.bytes.kvfile;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.kvfile.compact.KvFileCompactorFileCache;

public class KvFileCompactorTests{

	@Test
	private void testLevel(){
		Assert.assertEquals(KvFileCompactorFileCache.level(0), 0);
		Assert.assertEquals(KvFileCompactorFileCache.level(1), 0);
		Assert.assertEquals(KvFileCompactorFileCache.level(2), 1);
		Assert.assertEquals(KvFileCompactorFileCache.level(3), 2);
		Assert.assertEquals(KvFileCompactorFileCache.level(4), 2);
		Assert.assertEquals(KvFileCompactorFileCache.level(7), 3);
		Assert.assertEquals(KvFileCompactorFileCache.level(8), 3);
		Assert.assertEquals(KvFileCompactorFileCache.level(9), 4);
		Assert.assertEquals(KvFileCompactorFileCache.level(1_000), 10);
		Assert.assertEquals(KvFileCompactorFileCache.level(1_000_000), 20);
		Assert.assertEquals(KvFileCompactorFileCache.level(1_000_000_000), 30);
	}

}
