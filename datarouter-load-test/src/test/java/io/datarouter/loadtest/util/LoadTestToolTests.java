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
package io.datarouter.loadtest.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoadTestToolTests{

	@Test
	public void testNumBatches(){
		Assert.assertEquals(LoadTestTool.numBatches(9, 3), 3);
		Assert.assertEquals(LoadTestTool.numBatches(10, 3), 4);
	}

	@Test
	public void testAdjustedBatchSizeWithNoLeftovers(){
		Assert.assertEquals(LoadTestTool.adjustedBatchSize(9, 3, 0), 3);
	}

	@Test
	public void testAdjustedBatchSizeWithLeftovers(){
		Assert.assertEquals(LoadTestTool.adjustedBatchSize(10, 3, 0), 3);
		Assert.assertEquals(LoadTestTool.adjustedBatchSize(10, 3, 1), 3);
		Assert.assertEquals(LoadTestTool.adjustedBatchSize(10, 3, 2), 2);
		Assert.assertEquals(LoadTestTool.adjustedBatchSize(10, 3, 3), 2);
	}

}
