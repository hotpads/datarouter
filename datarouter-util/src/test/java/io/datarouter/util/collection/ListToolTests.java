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
package io.datarouter.util.collection;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ListToolTests{

	@Test
	public void copyOfRange(){
		List<Integer> resultA = ListTool.createLinkedList(1, 2, 3, 4, 5);
		List<Integer> resultB = ListTool.copyOfRange(resultA, 1, 3);
		Assert.assertEquals(resultB.toArray(), new Integer[]{2, 3});
		List<Integer> resultC = ListTool.copyOfRange(resultA, 4, 27);
		Assert.assertEquals(resultC.toArray(), new Integer[]{5});

		List<Integer> one = ListTool.createLinkedList(1);
		Assert.assertEquals(ListTool.copyOfRange(one, 0, 0).size(), 0);
		Assert.assertEquals(ListTool.copyOfRange(one, 0, -1).size(), 0);
		Assert.assertEquals(ListTool.copyOfRange(one, 0, 1).size(), 1);
		Assert.assertEquals(ListTool.copyOfRange(one, 0, 2).size(), 1);
		Assert.assertEquals(ListTool.copyOfRange(one, -1, 2).size(), 0);
	}

	@Test
	public void testGetFirstNElements(){
		List<Integer> list1To15 = ListTool.createArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
		List<Integer> list1To10 = ListTool.getFirstNElements(list1To15, 10);
		List<Integer> list1To5 = ListTool.getFirstNElements(list1To10, 5);
		List<Integer> list1To5TestLimit200 = ListTool.getFirstNElements(list1To5, 200);
		List<Integer> list1To5TestLimit0 = ListTool.getFirstNElements(list1To5TestLimit200, 0);
		List<Integer> list1To5TestLimitNeg1 = ListTool.getFirstNElements(list1To5TestLimit200, -1);

		Assert.assertEquals(list1To10.size(), 10);
		Assert.assertEquals(list1To10.toArray(), new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		Assert.assertEquals(list1To5.size(), 5);
		Assert.assertEquals(list1To5.toArray(), new Integer[]{1, 2, 3, 4, 5});
		Assert.assertEquals(list1To5.size(), list1To5TestLimit200.size());
		Assert.assertEquals(list1To5.toArray(), list1To5TestLimit200.toArray());
		Assert.assertEquals(list1To5TestLimit0.size(), 0);
		Assert.assertEquals(list1To5TestLimitNeg1.size(), 0);
	}

}
