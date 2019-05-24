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
package io.datarouter.util.iterable.scanner;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.scanner.collate.PriorityQueueCollator;
import io.datarouter.util.iterable.scanner.imp.ListBackedSortedScanner;

public class PriorityQueueCollatorTests{

	private ListBackedSortedScanner<Integer> s1, s2, s3, s4;

	@BeforeMethod
	public void setup(){
		s1 = new ListBackedSortedScanner<>(ListTool.createArrayList(1, 3, 8));
		s2 = new ListBackedSortedScanner<>(ListTool.createArrayList(2, 4, 9));
		s3 = new ListBackedSortedScanner<>(ListTool.createArrayList(5, 6, 7));
		s4 = new ListBackedSortedScanner<>(ListTool.createArrayList(64, 88, 100));
	}

	@Test
	private void test(){
		PriorityQueueCollator<Integer> collator = new PriorityQueueCollator<>(Arrays.asList(s1, s2, s3, s4));
		Assert.assertEquals(collator, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 64, 88, 100));
	}

	@Test
	private void testLimitedExcat(){
		PriorityQueueCollator<Integer> collator = new PriorityQueueCollator<>(Arrays.asList(s1, s2, s3, s4), 12L);
		Assert.assertEquals(collator, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 64, 88, 100));
	}

	@Test
	private void testLimitedRestrict(){
		PriorityQueueCollator<Integer> collator = new PriorityQueueCollator<>(Arrays.asList(s1, s2, s3, s4), 5L);
		Assert.assertEquals(collator, Arrays.asList(1, 2, 3, 4, 5));
	}

	@Test
	private void testLimitedToLarge(){
		PriorityQueueCollator<Integer> collator = new PriorityQueueCollator<>(Arrays.asList(s1, s2, s3, s4), 50L);
		Assert.assertEquals(collator, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 64, 88, 100));
	}

}
