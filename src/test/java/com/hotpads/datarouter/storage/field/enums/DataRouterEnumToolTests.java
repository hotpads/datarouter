package com.hotpads.datarouter.storage.field.enums;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.CollectionTool;

public class DataRouterEnumToolTests{
	private enum SomeType implements StringEnum<SomeType> {
		LARGE, CONDO, RENTAL, SALE, SUBLET;
		public String getPersistentString(){
			return name();
		}
		public SomeType fromPersistentString(String s){
			for (SomeType st : SomeType.values()) {
				if (st.getPersistentString().equalsIgnoreCase(s)) {
					return st;
				}
			}
			return null;
		}
	}
	
	@Test
	public void testSomeTypeCsvNames1() {
		 SomeType[] expected = { SomeType.LARGE, SomeType.CONDO };
		 List<SomeType> actual = DataRouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "large, funky, condo, dunno", false );
		 Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));
	}

	@Test
	public void testSomeTypeCsvNames2() {
		 SomeType[] expected = { SomeType.RENTAL, SomeType.SALE, SomeType.SUBLET };
		 List<SomeType> actual = DataRouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "rental, funky, condoo, sale, sublet", false );
		 Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));
		
		 actual = DataRouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "ballons", true );
		 Assert.assertTrue(CollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(SomeType.values()), actual));
	}
}
