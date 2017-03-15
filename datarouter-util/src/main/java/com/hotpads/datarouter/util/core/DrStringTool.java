package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

public class DrStringTool{

	public static boolean notEmpty(String input){
		return input != null && input.length() > 0;
	}

	public static boolean isEmpty(String input){
		return input == null || input.length() <= 0;
	}

	public static boolean isNull(String input){
		return input == null || "null".equalsIgnoreCase(input);
	}

	public static boolean isNullOrEmpty(String input){
		return input == null || input.length() <= 0 || "null".equalsIgnoreCase(input);
	}

	private static boolean isEmptyOrWhitespace(String input){
		if(input == null){
			return true;
		}
		return isEmpty(input.trim());
	}

	public static boolean isNullOrEmptyOrWhitespace(String input){
		if(input == null){
			return true;
		}
		return isNullOrEmpty(input.trim());
	}

	public static String nullIfEmpty(String input){
		if(isEmptyOrWhitespace(input)){
			return null;
		}
		return input;
	}

	public static int length(String input){
		if(isEmpty(input)){
			return 0;
		}
		return input.length();
	}

	public static String toLowerCase(String input){
		if(input == null){
			return "";
		}
		return input.toLowerCase();
	}

	public static boolean equalsCaseInsensitive(String left, String right){
		if(DrObjectTool.bothNull(left, right)){
			return true;
		}
		if(DrObjectTool.isOneNullButNotTheOther(left, right)){
			return false;
		}
		return left.toLowerCase().equals(right.toLowerCase());
	}

	public static boolean equalsCaseInsensitiveButNotCaseSensitive(String left, String right){
		return !Objects.equals(left, right) && equalsCaseInsensitive(left, right);
	}

	public static boolean containsCharactersBesidesWhitespace(String in){
		for(int i = 0; i < in.length(); ++i){
			if(in.charAt(i) != ' ' && in.charAt(i) != '\n' && in.charAt(i) != '\t'){
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> splitOnCharNoRegex(String input, char separator){
		return splitOnCharNoRegex(input, separator, true);
	}

	public static ArrayList<String> splitOnCharNoRegex(String input, char separator, boolean keepEmptySegments){
		ArrayList<String> results = new ArrayList<>();
		if(input == null){
			return results;
		}
		int leftIndex = 0;
		for(int rightIndex = 0; rightIndex <= input.length(); ++rightIndex){
			if(rightIndex == input.length() || separator == input.charAt(rightIndex)){
				String segment = input.substring(leftIndex, rightIndex);
				if(!segment.isEmpty() || keepEmptySegments){
					results.add(segment);
				}
				leftIndex = rightIndex + 1;// move to start of next token
			}
		}
		return results;
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

	public static String pad(String paramInput, char padding, int length){
		String input = nullSafe(paramInput);
		if(input.length() > length){
			throw new IllegalArgumentException("input \"" + input + "\" longer than maxLength=" + length);
		}
		int charsToGo = length - input.length();
		return repeat(padding, charsToGo) + input;
	}

	public static String padEnd(String paramInput, char padding, int length){
		String input = nullSafe(paramInput);
		if(input.length() > length){
			throw new IllegalArgumentException("input longer than maxLength");
		}
		int charsToGo = length - input.length();
		return input + repeat(padding, charsToGo);
	}

	public static String concatenate(Collection<? extends Object> objects, String delimiter){
		if(DrCollectionTool.isEmpty(objects)){
			return null;
		}
		if(objects.size() == 1){
			return DrCollectionTool.getFirst(objects).toString();
		}
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

	public static int countDigits(String input){
		if(input == null){
			return 0;
		}
		int numDigits = 0;
		for(int i = 0; i < input.length(); ++i){
			if(Character.isDigit(input.charAt(i))){
				++numDigits;
			}
		}
		return numDigits;
	}

	public static String retainDigits(String input){
		if(input == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); ++i){
			if(Character.isDigit(input.charAt(i))){
				sb.append(input.charAt(i));
			}
		}
		return sb.toString();
	}

	public static String retainLetters(String input){
		if(input == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); ++i){
			if(Character.isLetter(input.charAt(i))){
				sb.append(input.charAt(i));
			}
		}
		return sb.toString();
	}

	private static String replaceCharsAbove126(final String input, char replacement){
		if(input == null){
			return null;
		}

		String result = input;

		for(int i = 0; i < result.length(); ++i){ // careful about strings being immutable
			if(result.charAt(i) > 126){
				result = result.replace(result.charAt(i), replacement);
			}
		}

		return result;
	}

	private static String replaceCharactersInRange(final String input, int bottom, int top, char replacement){
		String range = DrRegexTool.makeCharacterClassFromRange(bottom, top, true);
		return input.replaceAll(range, "" + replacement);
	}

	public static String replaceStart(String original, String startsWith, String replacement){
		if(!original.startsWith(startsWith)){
			return original;
		}

		original = original.substring(startsWith.length());
		return replacement + original;
	}

	/**
	 * Get the string bounded by the first occurrence of right and the nearest occurrence to right of left.
	 * getStringSurroundedWithAndRest("|a|b||","|","|") = a getStringSurroundedWithAndRest("|a|b||","|,"||") = b
	 */
	public static String getStringSurroundedWith(String fromString, String left, String right){
		int[] startEnd = getIndicesOfStringSurroundedWith(fromString, left, right);
		if(startEnd == null || startEnd.length != 2){
			return "";
		}
		return fromString.substring(startEnd[0], startEnd[1]);
	}

	private static int[] getIndicesOfStringSurroundedWith(String fromString, String left, String right){
		int textStart = fromString.indexOf(left);
		if(textStart < 0){
			return null;
		}
		textStart = textStart + left.length();
		int textEnd = fromString.indexOf(right, textStart);
		if(textEnd <= textStart){
			return null;
		}
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
			while(number.endsWith(".")){
				number = number.substring(0, number.length() - 1);
			}
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
		if(input == null){
			return null;
		}
		input = replaceCharsAbove126(input, ' ');
		input = replaceCharactersInRange(input, 0, 8, ' ');
		input = replaceCharactersInRange(input, 11, 13, '\n');
		input = replaceCharactersInRange(input, 14, 31, ' ');
		return input;
	}

	public static String capitalizeFirstLetter(String input){
		return changeFirstCharacterCase(input, true);
	}

	private static String changeFirstCharacterCase(String input, boolean capitalize){
		if(isEmpty(input)){
			return input;
		}
		String firstLetter = input.substring(0, 1);
		if(capitalize){
			firstLetter = firstLetter.toUpperCase();
		}else{
			firstLetter = firstLetter.toLowerCase();
		}
		return firstLetter + input.substring(1);
	}

	public static String getStringAfterLastOccurrence(char ch, String sourceString){
		return getStringAfterLastOccurrence(Character.toString(ch), sourceString);
	}

	private static String getStringAfterLastOccurrence(String searchFor, String inString){
		if(inString != null){
			int index = inString.lastIndexOf(searchFor);
			if(index >= 0 && inString.length() > searchFor.length()){
				return inString.substring(index + searchFor.length());
			}
			return "";
		}
		return null;
	}

	public static String getStringBeforeLastOccurrence(char ch, String sourceString){
		return getStringBeforeLastOccurrence(Character.toString(ch), sourceString);
	}

	private static String getStringBeforeLastOccurrence(String searchFor, String inString){
		if(inString == null){
			return null;
		}
		int index = inString.lastIndexOf(searchFor);
		if(index < 0){
			return "";
		}
		return inString.substring(0, index);
	}

	public static boolean containsCharactersOutsideRange(final String input, int bottom, int top){
		char[] chars = input.toCharArray();
		for(char c : chars){
			if(c > top || c < bottom){
				return true;
			}
		}
		return false;
	}

	public static boolean containsOnlyNumbers(String data){
		return !isEmpty(data) && !containsCharactersOutsideRange(data, 48, 57);
	}

	public static boolean containsNumbers(String data){
		return !isEmpty(data) && containsCharactersInRange(data, 48, 57);
	}

	public static boolean containsCharactersInRange(final String input, int bottom, int top){
		for(int i = 0; i < input.length(); i++){
			if(input.charAt(i) <= top && input.charAt(i) >= bottom){
				return true;
			}
		}
		return false;
	}

	/** TESTS *****************************************************************/

	public static class Tests{

		@Test
		public void testGetStringSurroundedWith(){
			AssertJUnit.assertEquals("wee", getStringSurroundedWith("|wee||", "|", "|"));
			AssertJUnit.assertEquals("x", getStringSurroundedWith("][x][", "[", "]"));
			AssertJUnit.assertEquals("a name", getStringSurroundedWith("<span>a name</span>", "<span>", "</span>"));
			AssertJUnit.assertEquals("", getStringSurroundedWith("<span>a name</span>", "elephant", "</span>"));

			AssertJUnit.assertEquals("right", getStringSurroundedWith("[wrong][right][x]", "[", "][x]"));
			AssertJUnit.assertEquals("a", getStringSurroundedWith("|a|b|", "|", "|"));
			AssertJUnit.assertEquals("b", getStringSurroundedWith("|a|b||", "|", "||"));
		}

		@Test
		public void testPad(){
			AssertJUnit.assertEquals("    asdf", pad("asdf", ' ', 8));
			AssertJUnit.assertEquals("fdsa___", padEnd("fdsa", '_', 7));
			AssertJUnit.assertEquals("ffffffffff", repeat('f', 10));
		}

		@Test
		public void testReplaceCharactersInRange(){
			AssertJUnit.assertEquals("00banana 0banana 0banana 0 56", replaceCharactersInRange(
					"01banana 2banana 3banana 4 56", '1', '4', '0'));
		}

		@Test
		public void testSplitOnCharNoRegexWithEmptyStrings(){
			String input = "//";
			List<String> expected = Arrays.asList("", "", "");
			List<String> decoded = splitOnCharNoRegex(input, '/');
			AssertJUnit.assertEquals(expected, decoded);
		}

		@Test
		public void testSplitOnCharNoRegex(){
			ArrayAsserts.assertArrayEquals(new String[]{""}, splitOnCharNoRegex("", '/').toArray());
			ArrayAsserts.assertArrayEquals(new String[]{}, splitOnCharNoRegex(null, '/').toArray());
			ArrayAsserts.assertArrayEquals(new String[]{"", ""}, splitOnCharNoRegex("/", '/').toArray());
			ArrayAsserts.assertArrayEquals(new String[]{"  ", ""}, splitOnCharNoRegex("  /", '/').toArray());
			ArrayAsserts.assertArrayEquals(new String[]{"abc", "def", "g"}, splitOnCharNoRegex("abc.def.g", '.')
					.toArray());
			ArrayAsserts.assertArrayEquals(new String[]{"", "", "def", "g", ""}, splitOnCharNoRegex("..def.g.", '.')
					.toArray());
		}

		@Test
		public void testCaseInsensitive(){
			String aa = "dawgy";
			String bb = "dawGy";
			String cc = "dawGy";
			AssertJUnit.assertTrue(equalsCaseInsensitive(aa, bb));
			AssertJUnit.assertTrue(!Objects.equals(aa, bb));
			AssertJUnit.assertTrue(equalsCaseInsensitiveButNotCaseSensitive(aa, bb));
			AssertJUnit.assertTrue(!equalsCaseInsensitiveButNotCaseSensitive(bb, cc));
		}

		@Test
		public void testEnforceNumeric(){
			AssertJUnit.assertEquals("-8473.93", enforceNumeric("-8.473.93"));
			AssertJUnit.assertEquals("8473.93", enforceNumeric("8.473.93"));
			AssertJUnit.assertEquals("8473.93", enforceNumeric("8473.93"));
			AssertJUnit.assertEquals("5", enforceNumeric("5"));
			AssertJUnit.assertEquals("5", enforceNumeric("ff5ff"));
			AssertJUnit.assertEquals("5", enforceNumeric("ff5%"));
			AssertJUnit.assertEquals("5", enforceNumeric("5%"));
			AssertJUnit.assertEquals("5", enforceNumeric("%5"));
			AssertJUnit.assertEquals("5", enforceNumeric("5."));
			AssertJUnit.assertEquals("50.0", enforceNumeric("5.0.0."));
			AssertJUnit.assertEquals("", enforceNumeric("."));
			AssertJUnit.assertEquals("400000", enforceNumeric("ABC400,000DEF"));
		}

		@Test
		public void testGetStringAfterLastOccurrence(){
			AssertJUnit.assertEquals("xyz", getStringAfterLastOccurrence('/', "abc/def/xyz"));
			AssertJUnit.assertEquals("ef/xyz", getStringAfterLastOccurrence("/d", "abc/def/xyz"));
			AssertJUnit.assertEquals("", getStringAfterLastOccurrence("/z", "abc/def/xyz"));
		}

		@Test
		public void testGetStringBeforeLastOccurrence(){
			AssertJUnit.assertEquals("abc/def.xyz", getStringBeforeLastOccurrence('.', "abc/def.xyz.xml"));
			AssertJUnit.assertEquals("abc", getStringBeforeLastOccurrence("/d", "abc/def/xyz"));
			AssertJUnit.assertEquals(null, getStringBeforeLastOccurrence("", null));
			AssertJUnit.assertEquals("", getStringBeforeLastOccurrence(".", "no_dot"));
		}

		@Test
		public void testEnforceAlphabetic(){
			AssertJUnit.assertEquals("abc", enforceAlphabetic("abc123"));
			AssertJUnit.assertEquals("abc", enforceAlphabetic("1abc123,"));
		}

		@Test
		public void testReplaceStart(){
			AssertJUnit.assertEquals("something", replaceStart("something", "something", "something"));
			AssertJUnit.assertEquals("nothing", replaceStart("something", "some", "no"));
			AssertJUnit.assertEquals("something", replaceStart("something", "12", "yikes"));
			AssertJUnit.assertEquals("something", replaceStart("something", "thing", "yikes"));
		}

	}

}