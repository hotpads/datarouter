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
package io.datarouter.web.html.form;

import java.util.Optional;

import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;

public class HtmlFormValidator{

	public static Optional<String> notBlank(String input){
		if(input.isBlank()){
			return Optional.of("Non-blank value required");
		}
		return Optional.empty();
	}

	public static Optional<String> positiveInteger(String input){
		if(hasNonDigits(input)){
			return Optional.of("Digits only");
		}
		return canParseInteger(input)
				? Optional.empty()
				: Optional.of("Max value is " + Integer.MAX_VALUE);
	}

	public static Optional<String> positiveLong(String input){
		if(hasNonDigits(input)){
			return Optional.of("Digits only");
		}
		return canParseLong(input)
				? Optional.empty()
				: Optional.of("Max value is " + Long.MAX_VALUE);
	}

	public static Optional<String> maxLength(String input, int maxLength){
		if(input.length() > maxLength){
			String message = String.format(
					"Length %s is above max %s",
					NumberFormatter.addCommas(input.length()),
					NumberFormatter.addCommas(maxLength));
			return Optional.of(message);
		}
		return Optional.empty();
	}

	private static boolean canParseInteger(String input){
		try{
			Integer.valueOf(input);
			return true;
		}catch(RuntimeException e){
			return false;
		}
	}

	private static boolean canParseLong(String input){
		try{
			Long.valueOf(input);
			return true;
		}catch(RuntimeException e){
			return false;
		}
	}

	private static boolean hasNonDigits(String input){
		return !StringTool.scanCharacters(input)
				.allMatch(Character::isDigit);
	}

}
