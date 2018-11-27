/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.lang.ObjectTool;

public class StringTool{

	public static boolean notEmpty(String input){
		return input != null && input.length() > 0;
	}

	public static boolean isEmpty(String input){
		return input == null || input.length() <= 0;
	}

	public static boolean anyEmpty(String... inputs){
		for(String input : inputs){
			if(StringTool.isEmpty(input)){
				return true;
			}
		}
		return false;
	}

	public static boolean notNullNorEmpty(String input){
		return !isNullOrEmpty(input);
	}

	public static boolean isNullOrEmpty(String input){
		return input == null || input.length() <= 0 || "null".equalsIgnoreCase(input);
	}

	public static boolean notEmptyNorWhitespace(String input){
		return !isEmptyOrWhitespace(input);
	}

	public static boolean isEmptyOrWhitespace(String input){
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


	public static boolean notNullNorEmptyNorWhitespace(String input){
		return !isNullOrEmptyOrWhitespace(input);
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
		if(ObjectTool.bothNull(left, right)){
			return true;
		}
		if(ObjectTool.isOneNullButNotTheOther(left, right)){
			return false;
		}
		return left.toLowerCase().equals(right.toLowerCase());
	}

	public static boolean equalsCaseInsensitiveButNotCaseSensitive(String left, String right){
		return !Objects.equals(left, right) && equalsCaseInsensitive(left, right);
	}

	public static boolean containsCaseInsensitive(String baseString, String subString){
		if(ObjectTool.bothNull(baseString, subString)){
			return true;
		}
		if(ObjectTool.isOneNullButNotTheOther(baseString, subString)){
			return false;
		}
		return baseString.toLowerCase().contains(subString.toLowerCase());
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
		if(CollectionTool.isEmpty(objects)){
			return null;
		}
		if(objects.size() == 1){
			return CollectionTool.getFirst(objects).toString();
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

	public static String trimToSize(String str, int size){
		if(length(str) <= size){
			return str;
		}
		return str.substring(0, size);
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

	public static String replaceCharsAbove126(final String input, char replacement){
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

	public static String replaceCharactersInRange(final String input, int bottom, int top, char replacement){
		String range = RegexTool.makeCharacterClassFromRange(bottom, top, true);
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

	public static int[] getIndicesOfStringSurroundedWith(String fromString, String left, String right){
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

	public static String changeFirstCharacterCase(String input, boolean capitalize){
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

	public static String getStringAfterLastOccurrence(String searchFor, String inString){
		if(inString != null){
			int index = inString.lastIndexOf(searchFor);
			if(index >= 0 && inString.length() > searchFor.length()){
				return inString.substring(index + searchFor.length());
			}
			return "";
		}
		return null;
	}

	public static String getStringBeforeFirstOccurrence(char ch, String sourceString){
		if(sourceString == null){
			return null;
		}
		int firstOccurence = sourceString.indexOf(ch);
		if(firstOccurence == -1){
			return sourceString;
		}
		return sourceString.substring(0, firstOccurence);
	}

	public static String getStringBeforeLastOccurrence(char ch, String sourceString){
		return getStringBeforeLastOccurrence(Character.toString(ch), sourceString);
	}

	public static String getStringBeforeLastOccurrence(String searchFor, String inString){
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

	public static String getSimpleClassName(String className){
		int lastIndexOfDot = className.lastIndexOf('.');
		return lastIndexOfDot == -1 ? className : className.substring(lastIndexOfDot + 1);
	}

	public static String ensureEndsWithSlash(String string){
		return string.endsWith("/") ? string : string + '/';
	}

	public static String escapeString(String string){
		if(string == null){
			return "null";
		}
		String stringValue = string;
		//replace \ with \\
		stringValue = RegexTool.BACKSLASH_PATTERN.matcher(stringValue)
						.replaceAll(Matcher.quoteReplacement("\\\\"));
		//replace ' with \'
		stringValue = RegexTool.APOSTROPHE_PATTERN.matcher(stringValue)
						.replaceAll(Matcher.quoteReplacement("\\'"));
		return "'" + stringValue + "'";
	}

	/*------------------------- tests ---------------------------------------*/

	public static class Tests{

		@Test
		public void testGetStringSurroundedWith(){
			Assert.assertEquals(getStringSurroundedWith("|wee||", "|", "|"), "wee");
			Assert.assertEquals(getStringSurroundedWith("][x][", "[", "]"), "x");
			Assert.assertEquals(getStringSurroundedWith("<span>a name</span>", "<span>", "</span>"), "a name");
			Assert.assertEquals(getStringSurroundedWith("<span>a name</span>", "elephant", "</span>"), "");

			Assert.assertEquals(getStringSurroundedWith("[wrong][right][x]", "[", "][x]"), "right");
			Assert.assertEquals(getStringSurroundedWith("|a|b|", "|", "|"), "a");
			Assert.assertEquals(getStringSurroundedWith("|a|b||", "|", "||"), "b");
		}

		@Test
		public void testPad(){
			Assert.assertEquals(pad("asdf", ' ', 8), "    asdf");
			Assert.assertEquals(padEnd("fdsa", '_', 7), "fdsa___");
			Assert.assertEquals(repeat('f', 10), "ffffffffff");
		}

		@Test
		public void testReplaceCharactersInRange(){
			Assert.assertEquals(replaceCharactersInRange("01banana 2banana 3banana 4 56", '1', '4', '0'),
					"00banana 0banana 0banana 0 56");
		}

		@Test
		public void testSplitOnCharNoRegexWithEmptyStrings(){
			String input = "//";
			List<String> expected = Arrays.asList("", "", "");
			List<String> decoded = splitOnCharNoRegex(input, '/');
			Assert.assertEquals(decoded, expected);
		}

		@Test
		public void testSplitOnCharNoRegex(){
			Assert.assertEquals(splitOnCharNoRegex("", '/'), Arrays.asList(""));
			Assert.assertEquals(splitOnCharNoRegex(null, '/'), Collections.emptyList());
			Assert.assertEquals(splitOnCharNoRegex("/", '/'), Arrays.asList("", ""));
			Assert.assertEquals(splitOnCharNoRegex("  /", '/'), Arrays.asList("  ", ""));
			Assert.assertEquals(splitOnCharNoRegex("abc.def.g", '.'), Arrays.asList("abc", "def", "g"));
			Assert.assertEquals(splitOnCharNoRegex("..def.g.", '.'), Arrays.asList("", "", "def", "g", ""));
		}

		@Test
		public void testCaseInsensitive(){
			String aa = "dawgy";
			String bb = "dawGy";
			String cc = "dawGy";
			Assert.assertTrue(equalsCaseInsensitive(aa, bb));
			Assert.assertTrue(!Objects.equals(aa, bb));
			Assert.assertTrue(equalsCaseInsensitiveButNotCaseSensitive(aa, bb));
			Assert.assertTrue(!equalsCaseInsensitiveButNotCaseSensitive(bb, cc));
		}

		@Test
		public void testContainsCaseInsensitive(){
			String baseS = "HelloHowDYhi";
			String ss1 = "howdy";
			String ss2 = "howDy";
			String ss3 = "HowDy";
			String ss4 = "Hola";
			Assert.assertTrue(containsCaseInsensitive(baseS, ss1));
			Assert.assertTrue(containsCaseInsensitive(baseS, ss2));
			Assert.assertTrue(containsCaseInsensitive(baseS, ss3));
			Assert.assertFalse(containsCaseInsensitive(baseS, ss4));
		}

		@Test
		public void testEnforceNumeric(){
			Assert.assertEquals(enforceNumeric("-8.473.93"), "-8473.93");
			Assert.assertEquals(enforceNumeric("8.473.93"), "8473.93");
			Assert.assertEquals(enforceNumeric("8473.93"), "8473.93");
			Assert.assertEquals(enforceNumeric("5"), "5");
			Assert.assertEquals(enforceNumeric("ff5ff"), "5");
			Assert.assertEquals(enforceNumeric("ff5%"), "5");
			Assert.assertEquals(enforceNumeric("5%"), "5");
			Assert.assertEquals(enforceNumeric("%5"), "5");
			Assert.assertEquals(enforceNumeric("5."), "5");
			Assert.assertEquals(enforceNumeric("5.0.0."), "50.0");
			Assert.assertEquals(enforceNumeric("."), "");
			Assert.assertEquals(enforceNumeric("ABC400,000DEF"), "400000");
		}

		@Test
		public void testGetStringBeforeFirstOccurrence(){
			Assert.assertEquals(getStringBeforeFirstOccurrence('.', "v1.2"), "v1");
			Assert.assertEquals(getStringBeforeFirstOccurrence('.', "v1"), "v1");
			Assert.assertEquals(getStringBeforeFirstOccurrence('.', ""), "");
		}

		@Test
		public void testGetStringAfterLastOccurrence(){
			Assert.assertEquals(getStringAfterLastOccurrence('/', "abcdefxyz"), "");
			Assert.assertEquals(getStringAfterLastOccurrence('/', "abc/def/xyz"), "xyz");
			Assert.assertEquals(getStringAfterLastOccurrence("/d", "abc/def/xyz"), "ef/xyz");
			Assert.assertEquals(getStringAfterLastOccurrence("/z", "abc/def/xyz"), "");
		}

		@Test
		public void testGetStringBeforeLastOccurrence(){
			Assert.assertEquals(getStringBeforeLastOccurrence('.', "abc/def.xyz.xml"), "abc/def.xyz");
			Assert.assertEquals(getStringBeforeLastOccurrence("/d", "abc/def/xyz"), "abc");
			Assert.assertEquals(getStringBeforeLastOccurrence("", null), null);
			Assert.assertEquals(getStringBeforeLastOccurrence(".", "no_dot"), "");
		}

		@Test
		public void testEnforceAlphabetic(){
			Assert.assertEquals(enforceAlphabetic("abc123"), "abc");
			Assert.assertEquals(enforceAlphabetic("1abc123,"), "abc");
		}

		@Test
		public void testReplaceStart(){
			Assert.assertEquals(replaceStart("something", "something", "something"), "something");
			Assert.assertEquals(replaceStart("something", "some", "no"), "nothing");
			Assert.assertEquals(replaceStart("something", "12", "yikes"), "something");
			Assert.assertEquals(replaceStart("something", "thing", "yikes"), "something");
		}

		@Test
		public void testNumbers(){
			Assert.assertTrue(containsNumbers("a1dkfjaldk"));
			Assert.assertFalse(containsOnlyNumbers("a1dlkafj"));
			Assert.assertTrue(containsOnlyNumbers("01234567890123412341352109472813740198715"));
		}

		@Test
		public void testGetSimpleClassName(){
			Assert.assertEquals(getSimpleClassName("bar.Foo"), "Foo");
			Assert.assertEquals(getSimpleClassName("Foo"), "Foo");
		}

		@Test
		public void testEscapeString(){
			String string = "bill's";
			Assert.assertEquals(escapeString(string), "'bill\\'s'");
			string = "Renter\\\\\\'s Assurance Program";
			Assert.assertEquals(escapeString(string), "'Renter\\\\\\\\\\\\\\'s Assurance Program'");
			string = "no apostrophes";
			Assert.assertEquals(escapeString(string), "'no apostrophes'");
		}
	}
}