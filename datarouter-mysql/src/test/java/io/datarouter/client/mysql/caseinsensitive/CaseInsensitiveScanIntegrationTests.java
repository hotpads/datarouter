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
package io.datarouter.client.mysql.caseinsensitive;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class CaseInsensitiveScanIntegrationTests{

	@Inject
	private DatarouterTestCaseInsensitiveDao dao;

	// currently this lead to infinite loop: DATAROUTER-1129
	@Test(enabled = false)
	public void testMultiScan(){
		dao.put(new CaseInsensitiveTestDatabean("A"));
		dao.put(new CaseInsensitiveTestDatabean("b"));
		dao.put(new CaseInsensitiveTestDatabean("C"));
		dao.put(new CaseInsensitiveTestDatabean("d"));
		dao.put(new CaseInsensitiveTestDatabean("E"));
		dao.put(new CaseInsensitiveTestDatabean("f"));
		dao.put(new CaseInsensitiveTestDatabean("G"));
		dao.put(new CaseInsensitiveTestDatabean("h"));
		dao.put(new CaseInsensitiveTestDatabean("I"));
		List<Range<CaseInsensitiveTestPrimaryKey>> ranges = List.of(
				new Range<>(new CaseInsensitiveTestPrimaryKey("b"), new CaseInsensitiveTestPrimaryKey("d")),
				new Range<>(new CaseInsensitiveTestPrimaryKey("f"), new CaseInsensitiveTestPrimaryKey("h")));
		dao.scanRanges(ranges, 2)
				.flush(fetched -> Assert.assertEquals(fetched.size(), 4));
	}

}
