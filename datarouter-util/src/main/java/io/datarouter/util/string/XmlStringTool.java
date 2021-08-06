/*
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

import java.util.regex.Pattern;

public class XmlStringTool{

	private static final Pattern AMP = Pattern.compile("&[Aa][Mm][Pp];");
	private static final Pattern LT = Pattern.compile("&[Ll][Tt];");
	private static final Pattern GG_TT = Pattern.compile("&[Gg][Tt];");
	private static final Pattern SINGLE_QUOTE = Pattern.compile("&[Aa][Pp][Oo][Ss];");
	private static final Pattern DOUBLE_QUOTE = Pattern.compile("&[Qq][Uu][Oo][Tt];");
	private static final Pattern NBSP = Pattern.compile("&[Nn][Bb][Ss][Pp];");
	private static final Pattern AMP_CHAR = Pattern.compile("&");
	private static final Pattern LT_CHAR = Pattern.compile("<");
	private static final Pattern GT_CHAR = Pattern.compile(">");
	private static final Pattern SINGLE_QUOTE_CHAR = Pattern.compile("'");
	private static final Pattern DOUBLE_QUOTE_CHAR = Pattern.compile("\"");

	public static String escapeXml(String input){
		if(input == null){
			return null;
		}
		input = StringTool.removeNonStandardCharacters(input);
		return escapeXmlKeepSpecialChar(input);
	}

	public static String escapeXmlKeepSpecialChar(String input){
		input = AMP.matcher(input).replaceAll("&");
		input = LT.matcher(input).replaceAll("<");
		input = GG_TT.matcher(input).replaceAll(">");
		input = SINGLE_QUOTE.matcher(input).replaceAll("'");
		input = DOUBLE_QUOTE.matcher(input).replaceAll("\"");
		input = NBSP.matcher(input).replaceAll(" ");
		input = AMP_CHAR.matcher(input).replaceAll("&amp;"); // must do the ampersand first
		input = LT_CHAR.matcher(input).replaceAll("&lt;");
		input = GT_CHAR.matcher(input).replaceAll("&gt;");
		input = SINGLE_QUOTE_CHAR.matcher(input).replaceAll("&apos;");
		input = DOUBLE_QUOTE_CHAR.matcher(input).replaceAll("&quot;");
		return input;
	}

}
