package com.hotpads.datarouter.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.bytes.StringByteTool;

public class PercentFieldCodec{

	//separates the fragments
	private static final char CHAR_INTERNAL_SEPARATOR = '/';
	private static final String INTERNAL_SEPARATOR = Character.toString(CHAR_INTERNAL_SEPARATOR);
	private static final String ENCODED_INTERNAL_SEPARATOR = "%2F";//our separator must be encoded

	private static final char
			CHAR_ENCODED_SPACE = '+', //special encoding
			CHAR_ENCODED_DASH = '-', //same
			CHAR_ENCODED_UNDERSCORE = '_', //same
			CHAR_ENCODED_DOT = '.', //same
			CHAR_ENCODED_STAR = '*'; //same

	private static final String
			ENCODED_SPACE = Character.toString(CHAR_ENCODED_SPACE),
			ENCODED_DASH = Character.toString(CHAR_ENCODED_DASH),
			ENCODED_UNDERSCORE = Character.toString(CHAR_ENCODED_UNDERSCORE),
			ENCODED_DOT = Character.toString(CHAR_ENCODED_DOT),
			ENCODED_STAR = Character.toString(CHAR_ENCODED_STAR);

	private static final Set<Character> ILLEGAL_EXTERNAL_SEPARATORS = new HashSet<>();
	static{
		for(char c = 'A'; c <= 'Z'; ++c){
			ILLEGAL_EXTERNAL_SEPARATORS.add(c);
		}
		for(char c = 'a'; c <= 'z'; ++c){
			ILLEGAL_EXTERNAL_SEPARATORS.add(c);
		}
		for(char c = '0'; c <= '9'; ++c){
			ILLEGAL_EXTERNAL_SEPARATORS.add(c);
		}
		ILLEGAL_EXTERNAL_SEPARATORS.add(CHAR_INTERNAL_SEPARATOR);
		ILLEGAL_EXTERNAL_SEPARATORS.add(CHAR_ENCODED_SPACE);
		ILLEGAL_EXTERNAL_SEPARATORS.add(CHAR_ENCODED_DASH);
		ILLEGAL_EXTERNAL_SEPARATORS.add(CHAR_ENCODED_UNDERSCORE);
		ILLEGAL_EXTERNAL_SEPARATORS.add(CHAR_ENCODED_DOT);
		ILLEGAL_EXTERNAL_SEPARATORS.add(CHAR_ENCODED_STAR);
	}

	public static boolean isValidExternalSeparator(char separator){
		return ! ILLEGAL_EXTERNAL_SEPARATORS.contains(separator);
	}


	public static String encodeFields(List<Field<?>> fields){
		Stream<String> fieldValues = fields.stream().map(Field::getValueString);
		return encode(fieldValues);
	}

	private static String encode(Stream<String> strings){
		return strings
				.map(PercentFieldCodec::encodeFragment)
				.collect(Collectors.joining(INTERNAL_SEPARATOR));
	}

	private static String encodeFragment(String value){
		try{
			return URLEncoder.encode(value, StringByteTool.CHARSET_UTF8.toString());
		}catch(UnsupportedEncodingException e){
			throw new IllegalArgumentException("exception encoding " + value, e);
		}
	}

	public static List<String> decode(String encoded){
		String[] eachEncoded = encoded.split(INTERNAL_SEPARATOR);
		return Arrays.stream(eachEncoded)
				.map(PercentFieldCodec::decodeFragment)
				.collect(Collectors.toList());

	}

	private static String decodeFragment(String encoded){
		try{
			return URLDecoder.decode(encoded, StringByteTool.CHARSET_UTF8.toString());
		}catch(UnsupportedEncodingException e){
			throw new IllegalArgumentException("exception decoding " + encoded, e);
		}
	}


	/*---------------- tests -------------------*/

	public static class PercentFieldCodecTests{

		@Test
		public void testIndividualCharacters(){
			//slash must be encoded
			Assert.assertEquals(encodeFragment("/"), ENCODED_INTERNAL_SEPARATOR);
			//these 5 chars should not be encoded
			Assert.assertEquals(encodeFragment("-"), ENCODED_DASH);
			Assert.assertEquals(encodeFragment("_"), ENCODED_UNDERSCORE);
			Assert.assertEquals(encodeFragment("."), ENCODED_DOT);
			Assert.assertEquals(encodeFragment("*"), ENCODED_STAR);
			//nor should letters or digits
			Assert.assertEquals(encodeFragment("Aa0"), "Aa0");
			//forward slash must be encoded because it's our separator
		}
		@Test
		public void testComplexStrings(){
			String input = "/ A-b_0/.c*m";
			String expected = ENCODED_INTERNAL_SEPARATOR + ENCODED_SPACE + "A-b_0" + ENCODED_INTERNAL_SEPARATOR
					+ ".c*m";
			Assert.assertEquals(encodeFragment(input), expected);
		}
		@Test
		public void testMultipleStrings(){
			String[] inputs = new String[]{"*//a()&^$", "!~/.9"};
			String encoded = encode(Arrays.stream(inputs));
			List<String> decoded = decode(encoded);
			Assert.assertEquals(decoded, Arrays.asList(inputs));
		}
	}
}
