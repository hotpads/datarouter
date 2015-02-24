package com.hotpads.util.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.collections.AppendList;
import com.hotpads.util.core.collections.Pair;

public class StringTool{

	private StringTool(){
	} // uninstantiatable util class

	public static boolean notEmpty(String input){
		return input != null && input.length() > 0;
	}

	public static String verifyNotEmpty(String input){
		if(isEmpty(input)){ throw new IllegalArgumentException("string was empty"); }
		return input;
	}

	public static boolean isEmpty(String input){
		return input == null || input.length() <= 0;
	}

	public static boolean allEmpty(String... inputs){
		for(String i : inputs){
			if(!isEmpty(i)) return false;
		}
		return true;
	}

	public static boolean anyEmpty(String... inputs) {
		for (String i : inputs) {
			if (isEmpty(i)) {
				return true;
			}
		}
		return false;
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

	public static int nullSafeLength(String input){
		if(isEmpty(input)) return 0;
		return length(input);
	}

	public static boolean contains(String parent, String substring){
		if(parent == null){ return false; }
		return parent.contains(substring);
	}

	public static String nullIfEmpty(String input){
		if(isEmptyOrWhitespace(input)) return null;
		return input;
	}

	public static String replaceNullWithEmptyString(String input){
		if(input == null){
			return "";
		}else{
			return input;
		}
	}
	
	public static String toLowerCase(String input){
		if(input==null) return "";
		return input.toLowerCase();
	}

	public static String insertSeparator(String intoString, String groupingRegex, String separator){
		Pattern p = Pattern.compile(groupingRegex);
		Matcher m = p.matcher(intoString);
		AppendList<String> groups = AppendList.create();
		while(m.find()){
			groups.add(m.group());
		}
		return groups.toString(separator);
	}

	public static String pluralize(String item, int quantity){
		return pluralize(item, quantity != 1);
	}

	public static String pluralize(String item, boolean plural){
		if(!plural) return item;

		if(item.endsWith("y") && !item.matches(".*[aeiou]y")){ return item.substring(0, item.length() - 1) + "ies"; }

		// funky special cases
		if(item.toLowerCase().endsWith("child")) 
			return "children";

		boolean needsE = false;
		if(item.endsWith("x")) // words ending with -x pluralize to -xes
			needsE = true;

		if(item.endsWith("o")){
			// words ending with -o are usually pluralized to -oes, except for some special cases (photos)
			if(item.toLowerCase().endsWith("photo") || item.toLowerCase().endsWith("zero") 
				|| item.toLowerCase().endsWith("pro") || item.toLowerCase().endsWith("condo"))
				needsE = false;
			else
				needsE = true;
		}

		return item + (needsE ? "e" : "") + "s";
	}

	public static String pluralize(String singular, String plural, Integer quantity){
		if(quantity != null && quantity == 1){ return singular; }
		return plural;
	}

	public static boolean equalsCaseInsensitive(String a, String b){
		if(ObjectTool.bothNull(a, b)){ return true; }
		if(ObjectTool.isOneNullButNotTheOther(a, b)){ return false; }
		return a.toLowerCase().equals(b.toLowerCase());
	}

	public static boolean equalsCaseInsensitiveButNotCaseSensitive(String a, String b){
		return !ObjectTool.nullSafeEquals(a, b) && equalsCaseInsensitive(a, b);
	}

	public static boolean containsAny(String parent, Collection<String> any){
		for(String child : CollectionTool.nullSafe(any)){
			if(parent.contains(child)){ return true; }
		}
		return false;
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
	
	public static List<String> removeEmpties(Collection<String> list) {
		LinkedList<String> result = ListTool.createLinkedList();
		for (String element : CollectionTool.nullSafe(list)) {
			if (notEmpty(element)) {
				result.add(element);
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
	
	public static String trimNotNull(String input) {
		if (input == null) {
			return input;
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

	public static String tab(int num){
		return repeat('\t', num);
	}

	public static String truncate(final String input, int length){
		if(input == null)
			return null;
		else if(input.length() <= length)
			return input;
		else
			return input.substring(0, length);
	}

	public static String truncate(String text, int lengthLimit, boolean elipsis){
		if(text == null) return null;
		if(text.length() < lengthLimit) return text;
		if(elipsis) lengthLimit -= 3;
		text = text.substring(0, lengthLimit);
		if(elipsis) return text + "...";
		return text;
	}

	public static String truncateMiddleElipsis(String text, int lengthLimit){
		if(text == null) return null;
		if(text.length() <= lengthLimit) return text;
		lengthLimit -= 3;
		int beginExtra = text.length() % 2 == 1 ? 0 : 1;
		return text.substring(0, lengthLimit / 2 + beginExtra) + "..."
				+ text.substring(text.length() - lengthLimit / 2, text.length());
	}

	public static String truncateAtChar(String subject, char truncateAt){
		if(!StringTool.isEmpty(subject)){
			int indexOfTruncateAt = subject.indexOf(truncateAt);
			if(indexOfTruncateAt > 0){ return subject.substring(0, indexOfTruncateAt); }
		}
		return subject;
	}

	public static String tail(String text, int numChars){
		if(text == null || text.length()<numChars){
			return text;
		}
		return text.substring(text.length()-numChars);
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

	public static String removeNonAlphanumericCharacters(String input){
		if(input == null){ return null; }
		return input.replaceAll("[\\W_]", "");
	}

	public static String replaceNonAlphanumericCharactersWithSpace(String input){
		if(input == null){ return null; }
		input = retainCharactersInRange(input, 32, 'z').getLeft();
		input = replaceCharactersInRange(input, 33, 47, ' ');
		input = replaceCharactersInRange(input, 58, 64, ' ');
		input = replaceCharactersInRange(input, 91, 96, ' ');
		return input;
	}

	public static String replaceNonAlphanumericCharactersAllowSpaceUnderscore(String input, String replace){
		if(input == null){ return null; }
		return input.replaceAll("[^a-zA-Z0-9 _]", replace);
	}

	public static String removeNonAlphanumericCharactersAllowSpace(String input){
		if(input == null){ return null; }
		return input.replaceAll("[^a-zA-Z0-9 ]", "");
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

	public static String retainAlphanumericsAndUnderscores(String input){
		return retainAlphanumericsAndEtc(input, '_');
	}

	public static String retainAlphanumericsAndEtc(String input, char... etc){
		if(input == null){ return null; }

		if(etc.length > 0) Arrays.sort(etc); // sort so we can use Arrays.binarysearch

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < input.length(); ++i){

			if(Character.isLetterOrDigit(input.charAt(i)) || Arrays.binarySearch(etc, input.charAt(i)) >= 0){
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

	public static String replaceCharsAbove126(final String input, String replacement){
		if(input == null){ return null; }

		StringBuilder output = new StringBuilder();

		for(int i = 0; i < input.length(); ++i){
			if(((int)input.charAt(i)) > 126){
				output.append(replacement);
			}else{
				output.append(input.charAt(i));
			}
		}

		return output.toString();
	}

	public static String replaceCharactersInRange(final String input, int bottom, int top, char replacement){
		String range = RegexTool.makeCharacterClassFromRange(bottom, top, true);
		return input.replaceAll(range, "" + replacement);
	}

	public static Pair<String,Integer> retainCharactersInRange(final String input, int bottom, int top){
		String result = "";
		int elim = 0;
		for(int i = 0; i < input.length(); i++){
			char ci = input.charAt(i);
			if(((int)ci) <= top && ((int)ci) >= bottom){
				result += ci;
			}else{
				elim++;
			}
		}
		return new Pair<String,Integer>(result, elim);
	}

	public static boolean containsCharactersInRange(final String input, int bottom, int top){
		for(int i = 0; i < input.length(); i++){
			if(((int)input.charAt(i)) <= top && ((int)input.charAt(i)) >= bottom){ return true; }
		}
		return false;
	}

	public static boolean containsCharactersOutsideRange(final String input, int bottom, int top){
		char[] chars = input.toCharArray();
		for(int i = 0; i < chars.length; i++){
			if(((int)chars[i]) > top || ((int)chars[i]) < bottom){ return true; }
		}
		return false;
	}

	public static int countInRange(String s, char rangeBottom, char rangeTop){
		int inRange = 0;
		if(s == null) return inRange;
		for(char c : s.toCharArray()){
			if(c >= rangeBottom && c <= rangeTop){
				inRange++;
			}
		}
		return inRange;
	}

	public static boolean containsAsciiWhitespace(String input){
		if(input.contains(" ")) return true;
		if(input.contains("\t")) return true;
		if(input.contains("\n")) return true;
		if(input.contains("\r")) return true;
		return false;
	}

	public static int numStartsWith(Collection<String> input, String prefix){
		int count = 0;
		for(String i : input){
			if(i.startsWith(prefix)){
				++count;
			}
		}
		return count;
	}

	public static int numCaps(String input){
		if(StringTool.isEmpty(input)) return 0;
		int caps = 0;
		for(char c : input.toCharArray()){
			if(c >= 'A' && c <= 'Z') caps++;
		}
		return caps;
	}

	public static String removeLineBreaks(String text){
		return text.replaceAll("[\\n\\r]", "");
	}

	public static String reverse(String forward){
		char[] reverse = new char[forward.length()];
		for(int i = 0; i < forward.length(); ++i){
			reverse[i] = forward.charAt(forward.length() - i - 1);
		}
		return new String(reverse);
	}

	/**
	 * Inserts a string into another string at a specified index. If the index specified is past the end of the string,
	 * inserts at the end of the string.
	 * 
	 * @param text
	 * @param index the index at which to insert the string
	 * @param toInsert
	 * @return
	 */
	public static String insertAt(String text, int index, String toInsert){
		String r = (index > text.length() ? text : text.substring(0, index));
		r += toInsert;
		r += (index > text.length() ? "" : text.substring(index, text.length()));
		return r;
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

	public static Pair<String,String> getStringSurroundedWithAndRest(String fromString, String left, String right){
		int[] startEnd = getIndicesOfStringSurroundedWith(fromString, left, right);
		if(startEnd == null || startEnd.length != 2){ return Pair.create(null, null); }
		return Pair.create(fromString.substring(startEnd[0], startEnd[1]), fromString.substring(startEnd[1]));
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

	public static String replaceOnce(String string, String replace, String replaceWith, boolean caseSensitive,
			boolean last){
		if(!caseSensitive){
			replace = replace.toLowerCase();
		}
		int index;
		String stringCopy = string;
		if(!caseSensitive){
			stringCopy = string.toLowerCase();
		}
		if(last){
			index = stringCopy.lastIndexOf(replace);
		}else{
			index = stringCopy.indexOf(replace);
		}
		if(index < 0){ return string; }
		return string.substring(0, index) + replaceWith + string.substring(index + replace.length());
	}

	public static String
			replaceLastOccurrence(String string, String replace, String replaceWith, boolean caseSensitive){
		return replaceOnce(string, replace, replaceWith, caseSensitive, true);
	}

	public static String replaceEnd(String original, String endsWith, String replacement){
		if(!original.endsWith(endsWith)){ return original; }

		original = original.substring(0, original.length() - endsWith.length());
		return original + replacement;
	}

	public static String replaceStart(String original, String startsWith, String replacement){
		if(!original.startsWith(startsWith)) return original;

		original = original.substring(startsWith.length());
		return replacement + original;
	}

	public static boolean isDigits(String something){
		return !containsCharactersOutsideRange(something, (int)'0', (int)'9');
	}

	public static boolean isInteger(String i){
		try{
			Integer.parseInt(i);
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public static boolean isLong(String l){
		try{
			Long.parseLong(l);
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public static boolean isDouble(String d){
		try{
			Double.parseDouble(d);
			return true;
		}catch(Exception e){
			return false;
		}
	}

	public static boolean isOperator(Character c){
		boolean toReturn;
		switch(c){
		case '+':
		case '-':
		case '*':
		case '/':
		case '^':
			toReturn = true;
			break;
		default:
			toReturn = false;
			break;
		}
		return toReturn;
	}

	public static boolean isSeparator(Character c){
		boolean toReturn;
		switch(c){
		case '(':
		case ')':
			toReturn = true;
			break;
		default:
			toReturn = false;
			break;
		}
		return toReturn;
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

	public static String enforcePositive(String number){
		return enforceNumeric(number).replaceAll("\\-", "");
	}

	public static String enforceInteger(String number){
		boolean neg = number.startsWith("-");
		return (neg ? "-" : "") + number.replaceAll("[^\\d]", "");
	}

	public static String enforceDigits(String number){
		return number.replaceAll("[^\\d]", "");
	}
	
	public static String enforceAlphabetic(String characters){
		return characters.replaceAll("[^a-z]", "");
	}

	public static String getTabs(int numTabsNeeded){
		StringBuilder tabs = new StringBuilder();
		for(int i = 0; i < numTabsNeeded; ++i){
			tabs.append("\t");
		}
		return tabs.toString();
	}

	public static String removeNonStandardCharacters(String input){
		if(input == null){ return null; }
		input = replaceCharsAbove126(input, ' ');
		input = replaceCharactersInRange(input, 0, 8, ' ');
		input = replaceCharactersInRange(input, 11, 13, '\n');
		input = replaceCharactersInRange(input, 14, 31, ' ');
		return input;
	}

	public static String getRandomString(int length, Random r){
		char[] cs = new char[length];
		int range = '~' - ' ';
		for(int i = 0; i < length; i++){
			int c = r.nextInt(range);
			cs[i] = (char)(c + ' ');
		}
		return new String(cs);
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

	public static boolean isNumeric(String data){
		return !StringTool.isEmpty(data) && !StringTool.containsCharactersOutsideRange(data, 45, 57)
				&& !data.contains("/");
	}

	public static boolean isCurrency(String data){
		return !StringTool.isEmpty(data) && data.startsWith("$") && StringTool.isNumeric(data.substring(1));
	}

	public static boolean containsNumbers(String data){
		return !StringTool.isEmpty(data) && StringTool.containsCharactersInRange(data, 48, 57);
	}

	public static boolean containsNoAlpha(String data){
		return StringTool.isEmpty(data) || !StringTool.containsCharactersInRange(data, 65, 90)
				&& !StringTool.containsCharactersInRange(data, 97, 122);
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
	
	public static String convertFinalStaticFieldName2normalFieldName(String finalStaticFieldName){
		String fieldName = "";
		if(finalStaticFieldName != "" && finalStaticFieldName.length() > 0){
			String temp = "";
			finalStaticFieldName = finalStaticFieldName.toLowerCase();

			while(finalStaticFieldName.contains("_")){
				temp = getStringAfterLastOccurrence("_", finalStaticFieldName);
				fieldName = toUpperCaseAtIndex(0, temp) + fieldName;
				finalStaticFieldName = finalStaticFieldName.substring(0, finalStaticFieldName.lastIndexOf("_"));
			}
		}
		return finalStaticFieldName + fieldName;

	}

	public static String getGetterNameFromFinalStaticFieldName(String finalStaticFieldName){
		return getGetterNameFromJavaFieldName(convertFinalStaticFieldName2normalFieldName(finalStaticFieldName));
	}

	public static String getGetterNameFromJavaFieldName(String javaFieldName){
		String getterName = "";
		if(javaFieldName != "" && javaFieldName.length() > 0){
			getterName = "get" + toUpperCaseAtIndex(0, javaFieldName);
		}
		return getterName;
	}

	public static String toUpperCaseAtIndex(int index, String s){
		return s.substring(0, index) + s.substring(index, index + 1).toUpperCase() + s.substring(index + 1);
	}
	
	public static String join(String[] stringArray, String delimiter) {
		if(stringArray == null) {
			return null;
		}
		if(delimiter == null) {
			delimiter = "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(String s : stringArray) {
			sb.append(sb.length() > 0 ? delimiter + s : s);
		}
		return sb.toString();
	}
	
	public static int lastIndexOfAny(int[] chars, String s, int fromIndex, int toIndex) {
		if (s == null || s.length() <= 0) {
			return -1;
		}
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			for (int ch : chars) {
				if (s.charAt(i) == ch) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static String[] asNullSafeArray(List<String> list) {
		if (CollectionTool.isEmpty(list))  return new String[0];
		return list.toArray(new String[list.size()]);
	}

	public static String getNonEmptyDefault( String candidate, String dft ) {
		if(StringTool.isEmpty(candidate)) return dft;
		return candidate;
	}



	/** TESTS *****************************************************************/
	public static class Tests{
		@Test
		public void testCountInRange(){
			Assert.assertEquals(0, countInRange("", (char)0, (char)255));
			Assert.assertEquals(0, countInRange("ÃƒÆ’Ã†â€™Ã", (char)0, (char)126));
			Assert.assertEquals(10, countInRange("aÃbƒcÆd’eÃf†gâh€i™jÃ", (char)0, (char)126));
		}
		@Test
		public void testToUppercaseAtIndex(){
			Assert.assertTrue( "Hello World".equals( toUpperCaseAtIndex(0, "hello World") ) );
			Assert.assertTrue( "Hello World".equals( toUpperCaseAtIndex(0, "Hello World") ) );
			Assert.assertTrue( "hello World".equals( toUpperCaseAtIndex(6, "hello world") ) );
		}
		
		@Test
		public void testCropStringMiddleElipsis(){
			Assert.assertEquals(20, truncateMiddleElipsis("0123456789012345678901234567890123456789", 20).length());
		}

		@Test
		public void testGetStringSurroundedWithAndRest(){
			Assert.assertEquals("wee", getStringSurroundedWithAndRest("|wee||", "|", "|").getLeft());
			Assert.assertEquals("||", getStringSurroundedWithAndRest("|wee||", "|", "|").getRight());

			Assert.assertEquals("slank", getStringSurroundedWithAndRest("\"slank,bank", "\"", ",").getLeft());
			Assert.assertEquals(",bank", getStringSurroundedWithAndRest("\"slank,bank", "\"", ",").getRight());

			Assert.assertEquals("peters", getStringSurroundedWithAndRest("davepetersdave", "dave", "dave").getLeft());
			Assert.assertEquals("dave", getStringSurroundedWithAndRest("davepetersdave", "dave", "dave").getRight());

			Assert.assertEquals(null, getStringSurroundedWithAndRest("davepetersdave", "snow", "dave").getRight());
		}

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
		public void testRemoveNonAlphanumericCharacters() throws Exception{
			Assert.assertEquals("4A", removeNonAlphanumericCharacters("4.A! "));
			Assert.assertEquals("A", removeNonAlphanumericCharacters("A"));
			Assert.assertEquals("267", removeNonAlphanumericCharacters("267"));
			Assert.assertEquals("267", removeNonAlphanumericCharacters("#267"));
			Assert.assertEquals("267", removeNonAlphanumericCharacters("#26 _7"));
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
		public void testRetain(){
			assertEquals("boo_gity", retainAlphanumericsAndEtc("boo_gity//", '_'));
			assertEquals("boogity", retainAlphanumericsAndEtc("boo_gity//"));
			assertEquals("asd34lf-fds_", retainAlphanumericsAndEtc("a#s&d,34...\\[lf-fds_", '-', '_'));
			assertEquals("asd34lffds", retainAlphanumericsAndEtc("a#s&d,34...\\[lf-fds_"));
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
		public void testPluralize(){
			Assert.assertEquals("Condos", pluralize("Condo", 3));
			
			Assert.assertEquals(" photos", pluralize(" photo", 0));
			Assert.assertEquals(" photo", pluralize(" photo", 1));
			Assert.assertEquals(" photos", pluralize(" photo", 2));
			Assert.assertEquals(" photos", pluralize(" photo", -1));

			Assert.assertEquals(" monkeys", pluralize(" monkey", 0));
			Assert.assertEquals(" monkey", pluralize(" monkey", 1));
			Assert.assertEquals(" monkeys", pluralize(" monkey", 2));
			Assert.assertEquals(" monkeys", pluralize(" monkey", -1));

			Assert.assertEquals(" toys", pluralize(" toy", 0));
			Assert.assertEquals(" toy", pluralize(" toy", 1));
			Assert.assertEquals(" toys", pluralize(" toy", 2));
			Assert.assertEquals(" toys", pluralize(" toy", -1));

			Assert.assertEquals(" tries", pluralize(" try", 0));
			Assert.assertEquals(" try", pluralize(" try", 1));
			Assert.assertEquals(" tries", pluralize(" try", 2));
			Assert.assertEquals(" tries", pluralize(" try", -1));

			Assert.assertEquals("duplex", pluralize("duplex", 1));
			Assert.assertEquals("duplexes", pluralize("duplex", 2));

			Assert.assertEquals("potato", pluralize("potato", 1));
			Assert.assertEquals("potatoes", pluralize("potato", 2));
		}

		@Test
		public void testNumbers(){
			Assert.assertTrue(containsNumbers("a1dkfjaldk"));
			Assert.assertFalse(containsOnlyNumbers("a1dlkafj"));
			Assert.assertTrue(containsOnlyNumbers("01234567890123412341352109472813740198715"));
		}

		@Test
		public void testEnforcePositive() throws Exception{
			Assert.assertEquals("8375.99", enforcePositive("8,375.99"));
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

		@Test
		public void testReplaceEnd() throws Exception{
			Assert.assertEquals("something", replaceEnd("something", "something", "something"));
			Assert.assertEquals("somethink", replaceEnd("something", "g", "k"));
			Assert.assertEquals("somethinking", replaceEnd("something", "g", "king"));
			Assert.assertEquals("somewhere", replaceEnd("something", "thing", "where"));
			Assert.assertEquals("something", replaceEnd("something", "12", "yikes"));
			Assert.assertEquals("something", replaceEnd("something", "some", "yikes"));
		}

		@Test
		public void testReplaceStart() throws Exception{
			Assert.assertEquals("something", replaceStart("something", "something", "something"));
			Assert.assertEquals("nothing", replaceStart("something", "some", "no"));
			Assert.assertEquals("something", replaceStart("something", "12", "yikes"));
			Assert.assertEquals("something", replaceStart("something", "thing", "yikes"));
		}

		@Test
		public void testNumCaps(){
			Assert.assertEquals(0, numCaps(null));
			Assert.assertEquals(0, numCaps(""));
			Assert.assertEquals(2, numCaps("A Word"));
			Assert.assertEquals(0, numCaps("abcdefghijklmnopqrstuvwxyz"));
			Assert.assertEquals(26, numCaps("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
			Assert.assertEquals(7, numCaps("1Br, 2Ba HOUSE"));
		}

		@Test
		public void testTruncate(){
			Assert.assertEquals(1, truncate("squid", 1).length());
			Assert.assertEquals(4, truncate("squid", 4).length());
			Assert.assertEquals(5, truncate("squid", 40).length());
			Assert.assertEquals(0, truncate("", 40).length());
		}

		@Test
		public void testRemoveLineBreaks(){
			Assert.assertEquals("", removeLineBreaks(""));
			Assert.assertEquals("", removeLineBreaks("\r"));
			Assert.assertEquals("", removeLineBreaks("\n"));
			Assert.assertEquals("", removeLineBreaks("\r\n"));
			Assert.assertEquals("Somethingwithwords", removeLineBreaks("Something\r\nwith\rwords"));
		}

		@Test
		public void testTab(){
			Assert.assertEquals("", tab(0));
			Assert.assertEquals("", repeat('\t', 0));
			Assert.assertEquals("\t", tab(1));
			Assert.assertEquals("\t", repeat('\t', 1));
			Assert.assertEquals("\t\t", tab(2));
			Assert.assertEquals("\t\t", repeat('\t', 2));
		}

		@Test
		public void testReplaceLastOccurrence(){
			Assert.assertEquals("hat", replaceLastOccurrence("hot", "o", "a", false));
			Assert.assertEquals("hat", replaceLastOccurrence("hot", "o", "a", true));
			Assert.assertEquals("hOt", replaceLastOccurrence("hOt", "o", "a", true));
			Assert.assertEquals("hat", replaceLastOccurrence("hOt", "o", "a", false));
			Assert.assertEquals("Hat", replaceLastOccurrence("HOt", "o", "a", false));
			Assert.assertEquals("ab", replaceLastOccurrence("aa", "a", "b", false));
			Assert.assertEquals("b", replaceLastOccurrence("a", "a", "b", false));
			Assert.assertEquals("?a=Ccc&c=ddd", replaceLastOccurrence("?a=bBb&c=ddd", "bbb", "Ccc", false));
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
		
		@Test public void testJoin() {
			Assert.assertNull(join(null, null));
			Assert.assertNull(join(null, "SOMETHING SOMETHING SOMETHING"));
			Assert.assertEquals("", join(new String[]{""}, "SOMETHING ELSE SOMETHING ELSE"));
			Assert.assertEquals("h", join(new String[]{"h"}, "OOGA BOOGA BOOGA"));
			Assert.assertEquals("hello", join(new String[]{"h","e","l","l","o"}, null));
			Assert.assertEquals("hello", join(new String[]{"h","e","l","l","o"}, ""));
			Assert.assertEquals("h|e|l|l|o", join(new String[]{"h","e","l","l","o"}, "|"));
		}
		
		@Test public void testEnforceAlphabetic(){
			Assert.assertEquals("abc", enforceAlphabetic("abc123"));
			Assert.assertEquals("abc", enforceAlphabetic("1abc123,"));
		}
		
		@Test public void testTail(){
			Assert.assertEquals("bc", tail("abc",2));
			Assert.assertEquals("a", tail("a",2));
			Assert.assertEquals("abc", tail("abc",3));
			Assert.assertEquals("c", tail("abc",1));
			Assert.assertEquals("", tail("abc",0));
			Assert.assertNull(tail(null, 0));
			Assert.assertNull(tail(null, 3));
		}
	}
	/** END TESTS, don't put code after this point ****************************/

}