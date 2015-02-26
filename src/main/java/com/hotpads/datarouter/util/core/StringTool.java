package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class StringTool{


	public static boolean notEmpty(String input){
		return input != null && input.length() > 0;
	}

	public static boolean isEmpty(String input){
		return input == null || input.length() <= 0;
	}
	
	public static boolean isNull(String s){
		return s == null || s.equalsIgnoreCase("null");
	}

	public static boolean isNullOrEmpty(String s){
		return s == null || s.length() <= 0 || s.equalsIgnoreCase("null");
	}

	public static boolean isEmptyOrWhitespace(String s){
		if(s == null) return true;
		return isEmpty(s.trim());
	}

	public static boolean isNullOrEmptyOrWhitespace(String s){
		if(s == null) return true;
		return isNullOrEmpty(s.trim());
	}

	public static int length(String input){
		if(isEmpty(input)){ return 0; }
		return input.length();
	}
	
	public static String toLowerCase(String input){
		if(input==null) return "";
		return input.toLowerCase();
	}

	public static boolean equalsCaseInsensitive(String a, String b){
		if(ObjectTool.bothNull(a, b)){ return true; }
		if(ObjectTool.isOneNullButNotTheOther(a, b)){ return false; }
		return a.toLowerCase().equals(b.toLowerCase());
	}

	public static boolean equalsCaseInsensitiveButNotCaseSensitive(String a, String b){
		return !ObjectTool.nullSafeEquals(a, b) && equalsCaseInsensitive(a, b);
	}

	public static boolean containsCharactersBesidesWhitespace(String in){
		for(int i = 0; i < in.length(); ++i){
			if(in.charAt(i) != ' ' && in.charAt(i) != '\n' && in.charAt(i) != '\t'){ return true; }
		}
		return false;
	}

	public static ArrayList<String> splitOnCharNoRegex(String input, char c){
		if(isEmpty(input)){ return ListTool.createArrayList(input); }
		List<Integer> indexesOfChar = ListTool.createArrayList();
		for(int i = 0; i < input.length(); ++i){
			if(input.charAt(i) == c){
				indexesOfChar.add(i);
			}
		}
		if(indexesOfChar.size() == 0){ return ListTool.createArrayList(input); }
		indexesOfChar.add(input.length());
		ArrayList<String> result = ListTool.createArrayList();
		for(int i = 0; i < indexesOfChar.size(); ++i){
			int startIndex;
			if(i == 0){
				startIndex = 0;
			}else{
				startIndex = 1 + indexesOfChar.get(i - 1);
			}
			int endIndex = indexesOfChar.get(i);
			if(endIndex > startIndex){
				result.add(input.substring(startIndex, endIndex));
			}
		}
		return result;
	}

	public static boolean exceedsLength(String text, Integer allowedLength){
		return allowedLength != null && text != null && text.length() > allowedLength;
	}

	public static String nullSafe(String input){
		if(input == null){
			input = "";
		}
		return input;
	}

	public static String nullSafeTrim(String input){
		if(input == null){
			input = "";
		}
		return input.trim();
	}

	public static String repeat(char chr, int number){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < number; ++i){
			sb.append(chr);
		}
		return sb.toString();
	}

	public static String repeat(String input, int numTimes){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < numTimes; ++i){
			sb.append(input);
		}
		return sb.toString();
	}
	
	public static String pad(String pInput, char padding, int length){
		String input = nullSafe(pInput);
		if(input.length() > length){ 
			throw new IllegalArgumentException("input \"" + input + "\" longer than maxLength=" + length); }
		int charsToGo = length - input.length();
		return repeat(padding, charsToGo) + input;
	}

	public static String padEnd(String pInput, char padding, int length){
		String input = nullSafe(pInput);
		if(input.length() > length){ throw new IllegalArgumentException("input longer than maxLength"); }
		int charsToGo = length - input.length();
		return input + repeat(padding, charsToGo);
	}

	public static String concatenate(Collection<? extends Object> objects, String delimiter){
		if(CollectionTool.isEmpty(objects)){ return null; }
		if(objects.size() == 1){ return CollectionTool.getFirst(objects).toString(); }
		StringBuilder sb = new StringBuilder();
		boolean didFirst = false;
		for(Object o : objects){
			if(didFirst){
				sb.append(delimiter);
			}
			sb.append(o.toString());
			didFirst = true;
		}
		return sb.toString();
	}

	public static String retainDigits(String input){
		if(input == null){ return null; }
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); ++i){
			if(Character.isDigit(input.charAt(i))){
				sb.append(input.charAt(i));
			}
		}
		return sb.toString();
	}

	public static String retainLetters(String input){
		if(input == null){ return null; }
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); ++i){
			if(Character.isLetter(input.charAt(i))){
				sb.append(input.charAt(i));
			}
		}
		return sb.toString();
	}

	public static String replaceCharsAbove126(final String input, char replacement){
		if(input == null){ return null; }

		String result = input;

		for(int i = 0; i < result.length(); ++i){ // careful about strings being immutable
			if(((int)result.charAt(i)) > 126){
				result = result.replace(result.charAt(i), replacement);
			}
		}

		return result;
	}

	public static String replaceCharactersInRange(final String input, int bottom, int top, char replacement){
		String range = RegexTool.makeCharacterClassFromRange(bottom, top, true);
		return input.replaceAll(range, "" + replacement);
	}

	public static boolean containsCharactersOutsideRange(final String input, int bottom, int top){
		char[] chars = input.toCharArray();
		for(int i = 0; i < chars.length; i++){
			if(((int)chars[i]) > top || ((int)chars[i]) < bottom){ return true; }
		}
		return false;
	}

	/**
	 * Get the string bounded by the first occurrence of right and the nearest occurrence to right of left.
	 * getStringSurroundedWithAndRest("|a|b||","|","|") = a getStringSurroundedWithAndRest("|a|b||","|,"||") = b
	 * 
	 * @param fromString
	 * @param left
	 * @param right
	 * @return
	 */
	public static String getStringSurroundedWith(String fromString, String left, String right){
		int[] startEnd = getIndicesOfStringSurroundedWith(fromString, left, right);
		if(startEnd == null || startEnd.length != 2){ return ""; }
		return fromString.substring(startEnd[0], startEnd[1]);
	}

	private static int[] getIndicesOfStringSurroundedWith(String fromString, String left, String right){
		int textStart = fromString.indexOf(left);
		if(textStart < 0) return null;
		textStart = textStart + left.length();
		int textEnd = fromString.indexOf(right, textStart);
		if(textEnd <= textStart) return null;
		int lastTextStart = fromString.substring(0, textEnd).lastIndexOf(left);
		if(lastTextStart > 0){
			lastTextStart = lastTextStart + left.length();
			if(lastTextStart < textEnd){
				textStart = lastTextStart;
			}
		}
		return new int[]{textStart, textEnd};
	}

	public static String enforceNumeric(String number){
		number = number.replaceAll("[^\\d\\.\\-]", "");
		if(!number.matches("\\-?\\d*\\.?\\d+")){
			while(number.endsWith("."))
				number = number.substring(0, number.length() - 1);
			int secondDot = 0;
			Pattern dot = Pattern.compile("\\.");
			while((secondDot = number.indexOf('.', number.indexOf('.') + 1)) > 0){
				number = dot.matcher(number.substring(0, secondDot)).replaceAll("") + number.substring(secondDot);
			}
		}
		return number;
	}
	
	public static String enforceAlphabetic(String characters){
		return characters.replaceAll("[^a-z]", "");
	}

	public static String removeNonStandardCharacters(String input){
		if(input == null){ return null; }
		input = replaceCharsAbove126(input, ' ');
		input = replaceCharactersInRange(input, 0, 8, ' ');
		input = replaceCharactersInRange(input, 11, 13, '\n');
		input = replaceCharactersInRange(input, 14, 31, ' ');
		return input;
	}

	public static String capitalizeFirstLetter(String input){
		return changeFirstCharacterCase(input, true);
	}

	public static String lowercaseFirstCharacter(String input){
		return changeFirstCharacterCase(input, false);
	}

	private static String changeFirstCharacterCase(String input, boolean capitalize){
		if(isEmpty(input)) return input;
		String firstLetter = input.substring(0, 1);
		if(capitalize)
			firstLetter = firstLetter.toUpperCase();
		else
			firstLetter = firstLetter.toLowerCase();
		return firstLetter + input.substring(1);
	}

	public static boolean containsOnlyNumbers(String data){
		return !StringTool.isEmpty(data) && !StringTool.containsCharactersOutsideRange(data, 48, 57);
	}

	public static String getStringAfterLastOccurrence(char c, String sourceString){
		return getStringAfterLastOccurrence(Character.toString(c), sourceString);
	}

	public static String getStringAfterLastOccurrence(String s, String string){
		if(string != null){
			int i = string.lastIndexOf(s);
			if(i >= 0 && string.length() > s.length()){
				return string.substring(i + s.length());
			}
			return "";
		}
		return null;
	}

	public static String getStringBeforeLastOccurrence(char c, String sourceString){
		return getStringBeforeLastOccurrence(Character.toString(c), sourceString);
	}
	
	public static String getStringBeforeLastOccurrence(String s, String string) {
		if (string == null) {
			return null;
		}
		int i = string.lastIndexOf(s);
		if (i < 0) {
			return "";
		}
		return string.substring(0, i);
	}


	/** TESTS *****************************************************************/
	
	public static class Tests{

		@Test
		public void testGetStringSurroundedWith(){
			Assert.assertEquals("wee", getStringSurroundedWith("|wee||", "|", "|"));
			Assert.assertEquals("x", getStringSurroundedWith("][x][", "[", "]"));
			Assert.assertEquals("a name", getStringSurroundedWith("<span>a name</span>", "<span>", "</span>"));
			Assert.assertEquals("", getStringSurroundedWith("<span>a name</span>", "elephant", "</span>"));

			Assert.assertEquals("right", getStringSurroundedWith("[wrong][right][x]", "[", "][x]"));
			Assert.assertEquals("a", getStringSurroundedWith("|a|b|", "|", "|"));
			Assert.assertEquals("b", getStringSurroundedWith("|a|b||", "|", "||"));
		}

		@Test
		public void testPad(){
			Assert.assertEquals("    asdf", pad("asdf", ' ', 8));
			Assert.assertEquals("fdsa___", padEnd("fdsa", '_', 7));
			Assert.assertEquals("ffffffffff", repeat('f', 10));
		}

		@Test
		public void testReplaceCharactersInRange(){
			Assert.assertEquals("00banana 0banana 0banana 0 56", replaceCharactersInRange(
					"01banana 2banana 3banana 4 56", '1', '4', '0'));
		}

		@Test
		public void testSplitOnCharNoRegex(){
			String input = "abc.def.g";
			Object[] output = splitOnCharNoRegex(input, '.').toArray();
			Assert.assertArrayEquals(new String[]{"abc", "def", "g"}, output);

			String input2 = "..def.g.";
			Object[] output2 = splitOnCharNoRegex(input2, '.').toArray();
			Assert.assertArrayEquals(new String[]{"def", "g"}, output2);
		}

		@Test
		public void testCaseInsensitive(){
			String a = "dawgy";
			String b = "dawGy";
			String c = "dawGy";
			Assert.assertTrue(equalsCaseInsensitive(a, b));
			Assert.assertTrue(!ObjectTool.nullSafeEquals(a, b));
			Assert.assertTrue(equalsCaseInsensitiveButNotCaseSensitive(a, b));
			Assert.assertTrue(!equalsCaseInsensitiveButNotCaseSensitive(b, c));
		}

		@Test
		public void testNumbers(){
			Assert.assertFalse(containsOnlyNumbers("a1dlkafj"));
			Assert.assertTrue(containsOnlyNumbers("01234567890123412341352109472813740198715"));
		}

		@Test
		public void testEnforceNumeric() throws Exception{
			Assert.assertEquals("-8473.93", enforceNumeric("-8.473.93"));
			Assert.assertEquals("8473.93", enforceNumeric("8.473.93"));
			Assert.assertEquals("8473.93", enforceNumeric("8473.93"));
			Assert.assertEquals("5", enforceNumeric("5"));
			Assert.assertEquals("5", enforceNumeric("ff5ff"));
			Assert.assertEquals("5", enforceNumeric("ff5%"));
			Assert.assertEquals("5", enforceNumeric("5%"));
			Assert.assertEquals("5", enforceNumeric("%5"));
			Assert.assertEquals("5", enforceNumeric("5."));
			Assert.assertEquals("50.0", enforceNumeric("5.0.0."));
			Assert.assertEquals("", enforceNumeric("."));
			Assert.assertEquals("400000", enforceNumeric("ABC400,000DEF"));
		}
		
		@Test public void testGetStringAfterLastOccurrence(){
			Assert.assertEquals("xyz",getStringAfterLastOccurrence('/',"abc/def/xyz"));
			Assert.assertEquals("ef/xyz",getStringAfterLastOccurrence("/d","abc/def/xyz"));
			Assert.assertEquals("",getStringAfterLastOccurrence("/z","abc/def/xyz"));
		}
		
		@Test public void testGetStringBeforeLastOccurrence(){
			Assert.assertEquals("abc/def.xyz",getStringBeforeLastOccurrence('.',"abc/def.xyz.xml"));
			Assert.assertEquals("abc",getStringBeforeLastOccurrence("/d","abc/def/xyz"));
			Assert.assertEquals(null,getStringBeforeLastOccurrence("",null));
			Assert.assertEquals("",getStringBeforeLastOccurrence(".","no_dot"));
		}
		
		@Test public void testEnforceAlphabetic(){
			Assert.assertEquals("abc", enforceAlphabetic("abc123"));
			Assert.assertEquals("abc", enforceAlphabetic("1abc123,"));
		}
	}

}