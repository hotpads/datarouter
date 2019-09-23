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
package io.datarouter.client.mysql.caseinsensitive;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.storage.config.Config;
import io.datarouter.util.tuple.Range;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class CaseInsensitiveScanIntegrationTests{

	@Inject
	private DatarouterTestCaseInsensitiveRouter caseInsensitiveTestRouter;

	// currently this lead to infinite loop: DATAROUTER-1129
	@Test(enabled = false)
	public void testMultiScan(){
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("A"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("b"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("C"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("d"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("E"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("f"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("G"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("h"));
		caseInsensitiveTestRouter.caseInsensitiveTestDatabean.put(new CaseInsensitiveTestDatabean("I"));
		Config config = new Config().setOutputBatchSize(2);
		List<Range<CaseInsensitiveTestPrimaryKey>> ranges = Arrays.asList(
				new Range<>(new CaseInsensitiveTestPrimaryKey("b"), new CaseInsensitiveTestPrimaryKey("d")),
				new Range<>(new CaseInsensitiveTestPrimaryKey("f"), new CaseInsensitiveTestPrimaryKey("h")));
		List<CaseInsensitiveTestDatabean> fetched = caseInsensitiveTestRouter.caseInsensitiveTestDatabean
				.scanMulti(ranges, config)
				.list();
		Assert.assertEquals(fetched.size(), 4);
	}

}
