package com.hotpads.datarouter.client.imp.jdbc.ddl.test;

import java.util.List;

import org.apache.commons.lang.WordUtils;

public class TestCodeGenerator{

	protected String className;
	protected String packageName;


	public TestCodeGenerator(String className){
		this.className = className;
	}

	public String generateClassCode(List<String> objectNamesToTest, String suffix){
		String code = "import org.junit.Test; \n" + "public class " + className + "Test {\n" + generateTestMethods(
				objectNamesToTest, suffix) + "\n}";
		return code;
	}

	private String generateTestMethods(List<String> objectNamesToTest, String suffix){
		String testmethodsCode = "";
		for(String s : objectNamesToTest){
			testmethodsCode += "\n\t@Test\n\t" + "public void test" + WordUtils.capitalize(s.toLowerCase()) + suffix
					+ "(){\n\n\t" + "}\n\t";
		}
		return testmethodsCode;
	}

}
