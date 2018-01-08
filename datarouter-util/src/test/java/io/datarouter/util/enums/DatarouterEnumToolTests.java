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
package io.datarouter.util.enums;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collection.CollectionTool;

public class DatarouterEnumToolTests{
	private enum SomeType implements StringEnum<SomeType>{
		LARGE, CONDO, RENTAL, SALE, SUBLET;
		@Override
		public String getPersistentString(){
			return name();
		}
		@Override
		public SomeType fromPersistentString(String value){
			for(SomeType st : SomeType.values()){
				if(st.getPersistentString().equalsIgnoreCase(value)){
					return st;
				}
			}
			return null;
		}
	}

	@Test
	public void testSomeTypeCsvNames1(){
		SomeType[] expected = {SomeType.LARGE, SomeType.CONDO};
		List<SomeType> actual = DatarouterEnumTool.uniqueListFromCsvNames(SomeType.values(),
				"large, funky, condo, dunno", false).get();
		Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));
	}

	@Test
	public void testDuplicatesAreRemoved(){
		SomeType[] expected = {SomeType.LARGE, SomeType.CONDO};
		List<SomeType> actual = DatarouterEnumTool.uniqueListFromCsvNames(SomeType.values(),
				"large, funky, condo, large, condo, condo", false).get();
		Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));
	}


	@Test
	public void testSomeTypeCsvNames2(){
		SomeType[] expected = {SomeType.RENTAL, SomeType.SALE, SomeType.SUBLET};
		List<SomeType> actual = DatarouterEnumTool.uniqueListFromCsvNames(SomeType.values(),
				"rental, funky, condoo, sale, sublet", false).get();
		Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));

		actual = DatarouterEnumTool.uniqueListFromCsvNames(SomeType.values(), "ballons", true).get();
		Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(SomeType.values()), actual));
	}
}
