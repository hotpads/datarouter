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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;

public abstract class BaseSortedNodeWriterIntegrationTests extends BaseSortedNodeReaderIntegrationTests{

	protected final SortedMapStorage<SortedBeanKey,SortedBean> node;

	public BaseSortedNodeWriterIntegrationTests(
			SortedMapStorage<SortedBeanKey,SortedBean> node){
		super(node);
		this.node = node;
	}

	protected void resetTable(){
		node.deleteAll();
		Assert.assertEquals(count(), 0);
		node.putMulti(SortedBeans.generatedSortedBeans());
		Assert.assertEquals(count(), SortedBeans.TOTAL_RECORDS);
	}

	protected void postTestTests(){
		testSortedDelete();
		testBlankDatabeanPut();
	}

	private void testSortedDelete(){
		resetTable();
		int remainingElements = SortedBeans.TOTAL_RECORDS;

		//delete
		Assert.assertEquals(count(), remainingElements);
		var key = new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 0,
				SortedBeans.STRINGS.last());
		node.delete(key);
		--remainingElements;
		Assert.assertEquals(count(), remainingElements);

		//deleteMulti
		Assert.assertEquals(count(), remainingElements);
		List<SortedBeanKey> keys = Arrays.asList(
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 1,
						SortedBeans.STRINGS.last()),
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 2,
						SortedBeans.STRINGS.last()),
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 3,
						SortedBeans.STRINGS.last()));
		node.deleteMulti(keys);
		remainingElements -= 3;
		Assert.assertEquals(count(), remainingElements);

		//deleteWithPrefix
		Assert.assertEquals(count(), remainingElements);
		var prefix = new SortedBeanKey(SortedBeans.S_aardvark, null, null, null);
		node.deleteWithPrefix(prefix);
		remainingElements -= SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(count(), remainingElements);
	}

	private void testBlankDatabeanPut(){
		var blankDatabean = new SortedBean("a", "b", 1, "d1", null, null, null, null);
		var nonBlankDatabean = new SortedBean("a", "b", 1, "d2", "non blank", null, null, null);
		node.putMulti(Arrays.asList(nonBlankDatabean, blankDatabean));
		SortedBean blankDatabeanFromDb = readerNode.get(blankDatabean.getKey());
		Assert.assertNotNull(blankDatabeanFromDb);
		new SortedBeanFielder().getNonKeyFields(blankDatabeanFromDb).stream()
				.map(Field::getValue)
				.forEach(Assert::assertNull);
		Scanner.of(blankDatabean, nonBlankDatabean)
				.map(Databean::getKey)
				.flush(keys -> node.deleteMulti(keys));
		Assert.assertFalse(readerNode.exists(blankDatabean.getKey()));
	}

}
