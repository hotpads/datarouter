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
package io.datarouter.metric.service;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.metric.service.IndexUsageService.IndexMapping;
import io.datarouter.storage.dao.TestIndexUsageDao;
import io.datarouter.storage.node.op.index.IndexUsage.IndexUsageType;

public class IndexUsageServiceTests{

	@Test
	public void testGetIndexNames() throws Exception{
		List<IndexMapping> result = new IndexUsageService().getIndexNames(List.of(TestIndexUsageDao.class));
		Assert.assertEquals(result.size(), 4);
		Assert.assertEquals(result.getFirst().indexName(), "IgnoreSampleKey");
		Assert.assertEquals(result.getFirst().usageType(), IndexUsageType.IGNORE_USAGE);
		Assert.assertEquals(result.get(1).indexName(), "MultiSampleKey");
		Assert.assertEquals(result.get(2).indexName(), "StandardSampleKey");
		Assert.assertEquals(result.get(3).indexName(), "UniqueSampleKey");
		Assert.assertEquals(result.get(3).usageType(), IndexUsageType.IN_USE);
	}

}
