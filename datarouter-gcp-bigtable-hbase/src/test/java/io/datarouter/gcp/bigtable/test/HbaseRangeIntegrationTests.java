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
package io.datarouter.gcp.bigtable.test;

import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.gcp.bigtable.config.DatarouterBigTableTestNgModuleFactory;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterBigTableTestNgModuleFactory.class)
public class HbaseRangeIntegrationTests{

	private static final Collection<TestDatabean> DATABEANS = List.of(
			new TestDatabean("10", null, null),
			new TestDatabean("11", null, null),
			new TestDatabean("12", null, null),
			new TestDatabean("120", null, null),
			new TestDatabean("121", null, null),
			new TestDatabean("122", null, null),
			new TestDatabean("123", null, null),
			new TestDatabean("13", null, null),
			new TestDatabean("14", null, null),
			new TestDatabean("15", null, null));

	@Inject
	private BigTableRangeTestDatabeanDao testDatabeanDao;

	@Test
	public void testRange(){
		testDatabeanDao.deleteAll();
		testDatabeanDao.putMulti(DATABEANS);
		var list = testDatabeanDao.scanKeys(new Range<>(new TestDatabeanKey("12"), false, null, false))
				.list();
		Assert.assertFalse(list.isEmpty());
		Assert.assertEquals(list.get(0), new TestDatabeanKey("120"));
	}

}
