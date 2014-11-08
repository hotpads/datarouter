package com.hotpads.profile.callsite;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.util.core.lang.StackTraceElementTool;

public class LineOfCode{
	
	private static final int OFFSET_FROM_TOP_OF_STACK = 1;//top of stack is our constructor

	
	/**************** fields *********************************/
	
	private String packageName;
	private String className;
	private String methodName;
	private Integer lineNumber;
	
	
	/****************** construct ***************************/
	
	public LineOfCode(){
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		StackTraceElement callsite = stackTrace[OFFSET_FROM_TOP_OF_STACK];
		this.packageName = StackTraceElementTool.getPackageName(callsite);
		this.className = StackTraceElementTool.getSimpleClassName(callsite);
		this.methodName = callsite.getMethodName();
		this.lineNumber = callsite.getLineNumber();
	}

	
	/***************** get/set ****************************/
	
	public String getPackageName(){
		return packageName;
	}

	public String getClassName(){
		return className;
	}

	public String getMethodName(){
		return methodName;
	}

	public Integer getLineNumber(){
		return lineNumber;
	}

	
	/******************** tests ******************************/
	
	/*
	 * these can break easily if you modify this class.  just update the test
	 */
	public static class LineOfCodeTests{
		@Test
		public void testSimple(){
			LineOfCode lineOfCode = new LineOfCode();
			Assert.assertEquals("com.hotpads.profile.callsite", lineOfCode.getPackageName());
			Assert.assertEquals("LineOfCode$LineOfCodeTests", lineOfCode.getClassName());
			Assert.assertEquals("testSimple", lineOfCode.getMethodName());
			Assert.assertEquals(new Integer(61), lineOfCode.getLineNumber());
		}
	}
}
