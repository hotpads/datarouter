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
package io.datarouter.nodewatch.storage.tablesample;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

// Test that a PK with an array field uses deep equals and hashCode rather than the default identityHashCode
public class TableSampleKeyTests{

	private final String clientName = "myClient";
	private final String nodeName = "myNode";
	private final String subEntityPrefix = "mySubEntityPrefix";
	private final byte[] rowKeyBytes1 = new byte[]{1, 2, 3, 4};
	private TableSampleKey pk1 = new TableSampleKey(clientName, nodeName, subEntityPrefix, rowKeyBytes1);
	//use a separate array even though the value is the same in order to get a different identityHashCode
	private final byte[] rowKeyBytes2 = Arrays.copyOf(rowKeyBytes1, rowKeyBytes1.length);
	private TableSampleKey pk2 = new TableSampleKey(clientName, nodeName, subEntityPrefix, rowKeyBytes2);

	@Test
	public void testEquals(){
		Assert.assertEquals(pk1, pk2);
	}

	@Test
	public void testHashCode(){
		Assert.assertEquals(pk1.hashCode(), pk2.hashCode());
	}

}
