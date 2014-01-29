package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.junit.Test;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;

public class SchemaUpdateIntegrationTest{

	
	public static void main(String[] args){
		List<String> listOfColumnTypeNames = MySqlColumnType.getAllColumnTypeNames();
		TestCodeGenerator genereTest = new TestCodeGenerator("SchemaUpdateIntegration");
		System.out.println(genereTest.generateClassCode(listOfColumnTypeNames, "Modified"));
//		for(String type : listOfColumnTypeNames){
//			System.out.println("@Test\n" +
//					"public void test" +
//					WordUtils.capitalize(type.toLowerCase()) +
//					"Modified(){\n\n" +
//					"}");
//		}
	}
	
	
	/**
	 * TESTING THE MODIFICATION OF A COLUMN
	 */
	
	@Test
	public void testBitModified(){
			
	}
	@Test
	public void testTinyintModified(){

	}
	@Test
	public void testBoolModified(){

	}
	@Test
	public void testBooleanModified(){

	}
	@Test
	public void testSmallintModified(){

	}
	@Test
	public void testMediumintModified(){

	}
	@Test
	public void testIntModified(){

	}
	@Test
	public void testIntegerModified(){

	}
	@Test
	public void testBigintModified(){

	}
	@Test
	public void testDecimalModified(){

	}
	@Test
	public void testDecModified(){

	}
	@Test
	public void testFloatModified(){

	}
	@Test
	public void testDoubleModified(){

	}
	@Test
	public void testDouble_precisionModified(){

	}
	@Test
	public void testDateModified(){

	}
	@Test
	public void testDatetimeModified(){

	}
	@Test
	public void testTimestampModified(){

	}
	@Test
	public void testTimeModified(){

	}
	@Test
	public void testYearModified(){

	}
	@Test
	public void testCharModified(){

	}
	@Test
	public void testVarcharModified(){
		SqlTable current;
		SqlTable requested;
		SchemaUpdateOptions option;
		
		// DELETE THE CURRENT TABLE IF IT EXISTS
		// CREATE THE TABLE USING CURRENT
		// ASSERT THAT THE TABLE PARSES CORRECTLY AND MATCHES CURRENT
		// SET THE OPTIONS MANUALLY
		// SCHEMA UPDATE
		// PASS TO THE METHOD THAT TESTS THAT THE REQUESTED MATCHES EQUAL
		
	}
	@Test
	public void testBinaryModified(){

	}
	@Test
	public void testVarbinaryModified(){

	}
	@Test
	public void testTinyblobModified(){

	}
	@Test
	public void testTinytextModified(){

	}
	@Test
	public void testBlobModified(){

	}
	@Test
	public void testTextModified(){

	}
	@Test
	public void testMediumblobModified(){

	}
	@Test
	public void testMediumtextModified(){

	}
	@Test
	public void testLongblobModified(){

	}
	@Test
	public void testLongtextModified(){

	}
	@Test
	public void testEnumModified(){

	}
	@Test
	public void testSetModified(){

	}

	
	
	
	
}
