package com.hotpads.datarouter.util.core;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;


public class RegexTool {
	
	public static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\\\");
	public static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'");
	
	
	public static String makeCharacterClassFromRange(int bottom, int top, boolean brackets){
		String characterClass ="";
		for(int c=bottom;c<=top;c++){
			characterClass+=("\\u"+StringTool.pad(Integer.toHexString(c),'0',4));
		}
		return (brackets?"[":"")+characterClass+(brackets?"]":"");
	}

	
	
	public static class Tests {
		@Test public void testMakeCharacterClassFromRange() throws Exception{
			Assert.assertEquals("[]", makeCharacterClassFromRange(1,0,true));
			Assert.assertEquals("[\\u0000\\u0001\\u0002]", 
					makeCharacterClassFromRange(0,2,true));
			Assert.assertEquals("0a,  . smells 4",
					"01a,2  .3 smells 4".replaceAll(
							makeCharacterClassFromRange(49,51,true),""));
		}
	}
}
