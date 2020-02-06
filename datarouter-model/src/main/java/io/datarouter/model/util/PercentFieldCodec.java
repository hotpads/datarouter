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
package io.datarouter.model.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.model.field.Field;
import io.datarouter.util.net.UrlTool;
import io.datarouter.util.string.StringTool;

public class PercentFieldCodec{

	//separates the fragments
	private static final char CHAR_INTERNAL_SEPARATOR = '/';
	public static final String INTERNAL_SEPARATOR = Character.toString(CHAR_INTERNAL_SEPARATOR);

	private static final char
			CHAR_ENCODED_SPACE = '+', //special encoding
			CHAR_ENCODED_DASH = '-', //same
			CHAR_ENCODED_UNDERSCORE = '_', //same
			CHAR_ENCODED_DOT = '.', //same
			CHAR_ENCODED_STAR = '*'; //same

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
		return !ILLEGAL_EXTERNAL_SEPARATORS.contains(separator);
	}


	public static String encodeFields(List<Field<?>> fields){
		Stream<String> fieldValues = fields.stream().map(Field::getStringEncodedValue);
		return encode(fieldValues);
	}

	public static String encode(Stream<String> strings){
		return strings
				.map(PercentFieldCodec::encodeFragment)
				.collect(Collectors.joining(INTERNAL_SEPARATOR));
	}

	private static String encodeFragment(String value){
		if(value == null){
			return "";//treat null as empty string since we must do that when decoding
		}
		try{
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		}catch(UnsupportedEncodingException e){
			throw new IllegalArgumentException("exception encoding " + value, e);
		}
	}

	public static List<String> decode(String encoded){
		List<String> eachEncoded = StringTool.splitOnCharNoRegex(encoded, CHAR_INTERNAL_SEPARATOR);
		return eachEncoded.stream()
				.map(UrlTool::decode)
				.collect(Collectors.toList());

	}

}
