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
package io.datarouter.filesystem.util;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;

public class PathbeanBatchingScannerTests{

	@Test
	public void test1(){
		List<Pathbean> pathbeans = Scanner.iterate(0L, i -> i + 1)
				.limit(12)
				.map(size -> new Pathbean(PathbeanKey.of("asdf"), size))
				.list();
		List<List<Pathbean>> batches = Scanner.of(pathbeans)
				.link(pathbeanScanner -> new PathbeanBatchingScanner(pathbeanScanner, 9, 3))
				.list();
		int batchId = -1;
		Assert.assertEquals(batches.get(++batchId).size(), 3);//0, 1, 2 (max 3 files)
		Assert.assertEquals(batches.get(++batchId).size(), 2);//3, 4 (max 9 bytes)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//5 (max 9 bytes)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//6 (max 9 bytes)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//7 (max 9 bytes)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//8 (max 9 bytes)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//9 (max 9 bytes)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//10 (at least one file)
		Assert.assertEquals(batches.get(++batchId).size(), 1);//11 (at least one file)
		Assert.assertEquals(batches.size(), 9);
	}

}
