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
package io.datarouter.types;

import de.huxhorn.sulky.ulid.ULID;

public record UlidReversed(String reverseValue) implements Comparable<UlidReversed>{

	public UlidReversed{
		ULID.parseULID(reverseValue);
	}

	public UlidReversed(){
		this(createReverseStringWithReverseTimestamp(new ULID().nextULID()));
	}

	@Override
	public String toString(){
		return reverseValue;
	}

	@Override
	public int compareTo(UlidReversed other){
		return reverseValue.compareTo(other.reverseValue);
	}

	public static UlidReversed toUlidReversed(Ulid ulid){
		return new UlidReversed(createReverseStringWithReverseTimestamp(ulid.value()));
	}

	public static Ulid toUlid(UlidReversed ulidReversed){
		String reversedValue = ulidReversed.reverseValue();
		return new Ulid(createReverseStringWithReverseTimestamp(reversedValue));
	}

	private static String createReverseStringWithReverseTimestamp(String value){
		long reversedTimestamp = Long.MAX_VALUE - ULID.parseULID(value).timestamp();
		long timestamp = reversedTimestamp & UlidTool.TIMESTAMP_MASK;
		char[] buffer = new char[26];
		UlidTool.internalWriteCrockford(buffer, timestamp, 10, 0);
		return reverseRandomPart(value.substring(10).toCharArray(), buffer);
	}

	public static String reverseRandomPart(char[] randomPart, char[] buffer){
		int charIndex = 10;
		for(char ch : randomPart){
			char reverseChar = UlidTool.CHAR_TO_REVERSE_CHAR.get(ch);
			buffer[charIndex] = reverseChar;
			charIndex += 1;
		}
		return new String(buffer);
	}

}
