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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTool{

	public static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\\\");
	public static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'");

	public static String makeCharacterClassFromRange(int bottom, int top, boolean brackets){
		String characterClass = "";
		for(int c = bottom; c <= top; c++){
			characterClass += "\\u" + StringTool.pad(Integer.toHexString(c), '0', 4);
		}
		return (brackets ? "[" : "") + characterClass + (brackets ? "]" : "");
	}

	public static String buildWithReplacements(Pattern pattern, String input, Function<Matcher,String> toReplacement){
		var sb = new StringBuilder();
		var matcher = pattern.matcher(input);
		while(matcher.find()){
			matcher.appendReplacement(sb, toReplacement.apply(matcher));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

}
