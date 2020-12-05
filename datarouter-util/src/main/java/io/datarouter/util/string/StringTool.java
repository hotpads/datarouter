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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.datarouter.util.lang.ObjectTool;

public class StringTool{

	private static final Collator COLLATOR = Collator.getInstance();
	private static final Pattern NOT_A_NUMBER_DOT_OR_MINUS_PATTERN = Pattern.compile("[^\\d\\.\\-]");
	private static final Pattern LOOKS_LIKE_A_NUMBER_PATTERN = Pattern.compile("\\-?\\d*\\.?\\d+");
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
	private static final Pattern MINUS_PATTERN = Pattern.compile("-");
	private static final Pattern NOT_LOWERCASE_ALPHA_PATTERN = Pattern.compile("[^a-z]");

	public static final Comparator<String> COLLATOR_COMPARATOR = (first, second) -> COLLATOR.compare(first, second);

	public static boolean notEmpty(String input){
		return input != null && input.length() > 0;
	}

	public static boolean isEmpty(String input){
		return input == null || input.isEmpty();
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
		return isEmpty(input) || input.isBlank();
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

	public static String concatenate(Collection<?> objects, String delimiter){
		if(objects == null || objects.isEmpty()){
			return null;
		}
		if(objects.size() == 1){
			return objects.iterator().next().toString();
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

	public static String replaceCharsAbove126(String input, char replacement){
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

	public static String replaceCharactersInRange(String input, int bottom, int top, char replacement){
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
		number = NOT_A_NUMBER_DOT_OR_MINUS_PATTERN.matcher(number).replaceAll("");
		if(!LOOKS_LIKE_A_NUMBER_PATTERN.matcher(number).matches()){
			while(number.endsWith(".")){
				number = number.substring(0, number.length() - 1);
			}
			int secondDot = 0;
			while((secondDot = number.indexOf('.', number.indexOf('.') + 1)) > 0){
				number = DOT_PATTERN.matcher(number.substring(0, secondDot)).replaceAll("")
						+ number.substring(secondDot);
			}
			if(number.length() > 1){
				number = number.substring(0, 1) + MINUS_PATTERN.matcher(number.substring(1)).replaceAll("");
			}
			if("-".equals(number)){
				return "";
			}
		}
		return number;
	}

	//FIXME: This should be renamed to enforceLowerCaseAlphabetic
	public static String enforceAlphabetic(String characters){
		return NOT_LOWERCASE_ALPHA_PATTERN.matcher(characters).replaceAll("");
	}

	public static String removeNonStandardCharacters(String input){
		if(input == null){
			return null;
		}
		char[] chrs = input.toCharArray();
		for(int i = 0; i < chrs.length; ++i){
			char chr = chrs[i];
			if(chr > 126){
				chrs[i] = ' ';
			}else if(0 <= chr && chr <= 8){
				chrs[i] = ' ';
			}else if(11 <= chr && chr <= 13){
				chrs[i] = '\n';
			}else if(14 <= chr && chr <= 31){
				chrs[i] = ' ';
			}
		}
		return new String(chrs);
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
		return getStringBeforeFirstOccurrence(Character.toString(ch), sourceString);
	}

	public static String getStringBeforeFirstOccurrence(String searchFor, String sourceString){
		if(sourceString == null){
			return null;
		}
		int firstOccurence = sourceString.indexOf(searchFor);
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

	public static boolean containsCharactersOutsideRange(String input, int bottom, int top){
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

	public static boolean containsCharactersInRange(String input, int bottom, int top){
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

}
