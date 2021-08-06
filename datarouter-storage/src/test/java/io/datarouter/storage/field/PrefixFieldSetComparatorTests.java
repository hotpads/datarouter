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
package io.datarouter.storage.field;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.field.compare.FieldSetRangeFilter;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.util.tuple.Range;

public class PrefixFieldSetComparatorTests{

	private static final String NODE_NAME = "SortedNodeTestRouter.SortedBean";

	private SortedBeanKey endOfRange1 = new SortedBeanKey("emu", null, null, null);
	private Range<SortedBeanKey> rangeEndInclusive = new Range<>(null, true, endOfRange1, true);

	@Test
	public void testStart(){
		SortedBeanKey startOfRangeKey = new SortedBeanKey("a", "c", 2, null);
		SortedBeanKey candidateKey = new SortedBeanKey("a", "c", 2, "d");
		Assert.assertTrue(FieldSetRangeFilter.isCandidateAfterStartOfRange(candidateKey.getFields(), startOfRangeKey
				.getFields(), true, NODE_NAME));
		Assert.assertFalse(FieldSetRangeFilter.isCandidateAfterStartOfRange(candidateKey.getFields(), startOfRangeKey
				.getFields(), false, NODE_NAME));
	}

	@Test
	public void testEnd(){
		SortedBeanKey endOfRangeKey = new SortedBeanKey("a", "c", 2, null);
		SortedBeanKey candidateKey = new SortedBeanKey("a", "c", 2, "d");
		Assert.assertTrue(FieldSetRangeFilter.isCandidateBeforeEndOfRange(candidateKey.getFields(), endOfRangeKey
				.getFields(), true));
		Assert.assertFalse(FieldSetRangeFilter.isCandidateBeforeEndOfRange(candidateKey.getFields(), endOfRangeKey
				.getFields(), false));
	}

	@Test
	public void testObviousFailure(){
		SortedBeanKey candidate1 = new SortedBeanKey("zzz", "zzz", 55, "zzz");
		Assert.assertTrue(candidate1.compareTo(endOfRange1) > 0);// sanity check
		Assert.assertFalse(FieldSetRangeFilter.include(candidate1, rangeEndInclusive, NODE_NAME));
	}

	@Test
	public void testInclusiveExclusive(){
		SortedBeanKey endOfRange2 = new SortedBeanKey("emu", "d", 5, "g");
		Range<SortedBeanKey> rangeEnd2Inclusive = new Range<>(null, true, endOfRange2, true);
		Range<SortedBeanKey> rangeEnd2Exclusive = new Range<>(null, true, endOfRange2, false);
		// the candidate would normally compare after the endOfRange, but should be included here
		SortedBeanKey candidate3 = new SortedBeanKey("emu", "d", 5, "g");
		Assert.assertTrue(candidate3.compareTo(endOfRange2) == 0);
		// but in the prefix range
		Assert.assertTrue(FieldSetRangeFilter.include(candidate3, rangeEnd2Inclusive, NODE_NAME));
		// even with inclusive=false
		Assert.assertFalse(FieldSetRangeFilter.include(candidate3, rangeEnd2Exclusive, NODE_NAME));
	}

}
