package com.hotpads.datarouter.storage.field.enums;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class TestDataRouterEnumTool{
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
		 Assert.assertTrue( expected.length == actual.size() );
		 for (int index = 0; index < expected.length; index++ ) {
			 Assert.assertTrue( expected[index] == actual.get(index));
		 }
	}

	@Test
	public void testSomeTypeCsvNames2() {
		 SomeType[] expected = { SomeType.RENTAL, SomeType.SALE, SomeType.SUBLET };
		 List<SomeType> actual = DataRouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "rental, funky, condo, sale, sublet", false );
		 Assert.assertTrue( expected.length == actual.size() );
		 for (int index = 0; index < expected.length; index++ ) {
			 Assert.assertTrue( expected[index] == actual.get(index));
		 }
		 actual = DataRouterEnumTool.uniqueListFromCsvNames( SomeType.values(), "ballons", true );
		 expected = SomeType.values();
		 Assert.assertTrue( expected.length == actual.size() );
		 for (int index = 0; index < expected.length; index++ ) {
			 Assert.assertTrue( expected[index] == actual.get(index));
		 }
	}
}
