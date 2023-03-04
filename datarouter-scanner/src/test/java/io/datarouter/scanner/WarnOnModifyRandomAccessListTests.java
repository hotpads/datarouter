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
package io.datarouter.scanner;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WarnOnModifyRandomAccessListTests{

	@Test
	public void testLogs(){
		List<Integer> backingList = new ArrayList<>(List.of(0, 1));
		List<Integer> warnList = new WarnOnModifyRandomAccessList<>(backingList);
		Assert.assertEquals(0, warnList.get(0));
		warnList.add(2);// logs
		Assert.assertEquals(backingList, List.of(0, 1, 2));
		warnList.add(0, 3);// logs
		Assert.assertEquals(backingList, List.of(3, 0, 1, 2));
		warnList.remove(2);// logs
		Assert.assertEquals(backingList, List.of(3, 0, 2));
	}

}
