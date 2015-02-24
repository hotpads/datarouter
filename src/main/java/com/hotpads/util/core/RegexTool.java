package com.hotpads.util.core;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hotpads.util.core.collections.Pair;


public class RegexTool {
	
	public static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\\\");
	public static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'");
	public static final Pattern CRLF_PATTERN = Pattern.compile("[\r\n]+");
	
	public static final String LIST_DELIMITER_PATTERN_STRING = 
			",|[,\\s]+and\\s+|[,\\s]+or\\s+|&";
	
	public static String makeCharacterClassFromRanges(boolean brackets,Pair<Integer,Integer>... ranges){
		String characterClass = "";
		for(Pair<Integer, Integer> range:ranges){
			characterClass+=makeCharacterClassFromRange(range,false);
		}
		return (brackets?"[":"")+characterClass+(brackets?"]":"");
	}
	
	public static String makeCharacterClassFromRange(Pair<Integer,Integer> range, boolean brackets){
		return makeCharacterClassFromRange(range.getLeft(),range.getRight(),brackets);
	}
	
	public static String makeCharacterClassFromRange(int bottom, int top, boolean brackets){
		String characterClass ="";
		for(int c=bottom;c<=top;c++){
			characterClass+=("\\u"+StringTool.pad(Integer.toHexString(c),'0',4));
		}
		return (brackets?"[":"")+characterClass+(brackets?"]":"");
	}
	
	/**
	 * DOES NOT HANDLE escaping anything, just formats list as a regex
	 * @param s
	 * @return
	 */
	public static String makeRegexForExactStrings(String... ss){
		return makeRegexForExactStrings(Lists.newArrayList(ss));
	}
	public static String makeRegexForExactStrings(List<String> ss){
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		int c = 0;
		for(String s : ss){
			c++;
			sb.append(Pattern.quote(s));			
			if(c!=ss.size()){
				sb.append("|");
			}
		}
		sb.append(")");
		return sb.toString();
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
		@Test public void testMakeRegexForExactStrings(){
			String magnolia = "magnolia";
			Assert.assertEquals(magnolia,magnolia.replaceAll(makeRegexForExactStrings("no."),"x"));
			Assert.assertEquals(magnolia+"x",(magnolia+"no.").replaceAll(makeRegexForExactStrings("no."),"x"));
			Assert.assertEquals("x"+magnolia+"x",("no."+magnolia+"no.").replaceAll(makeRegexForExactStrings("no."),"x"));

			Assert.assertEquals("xyz","axbycz".replaceAll(makeRegexForExactStrings("a","b","c"),""));
		}
		@Test public void testCrLfPattern(){
			Assert.assertEquals("", CRLF_PATTERN.matcher("\r").replaceAll(""));
			Assert.assertEquals("", CRLF_PATTERN.matcher("\n").replaceAll(""));
			Assert.assertEquals("", CRLF_PATTERN.matcher("\n\r").replaceAll(""));
			Assert.assertEquals("", CRLF_PATTERN.matcher("\r\n").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\rdef").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\ndef").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\n\rdef").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\r\ndef").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\rd\r\ref").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\nd\ne\rf").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\n\rd\n\ref\n\r").replaceAll(""));
			Assert.assertEquals("abcdef", CRLF_PATTERN.matcher("abc\r\nd\r\nef\r\n").replaceAll(""));
		}
	}
}
