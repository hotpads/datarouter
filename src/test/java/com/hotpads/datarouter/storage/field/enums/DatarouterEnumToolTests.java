package com.hotpads.datarouter.storage.field.enums;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import com.hotpads.datarouter.util.core.DrCollectionTool;

public class DatarouterEnumToolTests{
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
		 List<SomeType> actual = DatarouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "large, funky, condo, dunno", false );
		 Assert.assertTrue(DrCollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));
	}

	@Test
	public void testSomeTypeCsvNames2() {
		 SomeType[] expected = { SomeType.RENTAL, SomeType.SALE, SomeType.SUBLET };
		 List<SomeType> actual = DatarouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "rental, funky, condoo, sale, sublet", false );
		 Assert.assertTrue(DrCollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(expected), actual));
		
		 actual = DatarouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "ballons", true );
		 Assert.assertTrue(DrCollectionTool.equalsAllElementsInIteratorOrder(Arrays.asList(SomeType.values()), actual));
	}
}
