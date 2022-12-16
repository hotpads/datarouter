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

import java.util.HashMap;
import java.util.Map;

public class UlidTool{

	public static final int LENGTH = 26;

	public static final char[] ENCODING_CHARS = {
			'0','1','2','3','4','5','6','7','8','9',
			'A','B','C','D','E','F','G','H','J','K',
			'M','N','P','Q','R','S','T','V','W','X',
			'Y','Z',
	};

	public static final int MASK = 0x1F;
	public static final int MASK_BITS = 5;
	public static final long TIMESTAMP_MASK = 0x0000_FFFF_FFFF_FFFFL;
	public static final Map<Character,Character> CHAR_TO_REVERSE_CHAR = createCharToReverseCharMap();

	private static Map<Character,Character> createCharToReverseCharMap(){
		Map<Character,Character> charToReverse = new HashMap<>();
		for(int i = 0; i < ENCODING_CHARS.length; i++){
			charToReverse.put(ENCODING_CHARS[i], ENCODING_CHARS[ENCODING_CHARS.length - 1 - i]);
		}
		return charToReverse;
	}

	public static void internalWriteCrockford(char[] buffer, long value, int count, int offset){
		for(int i = 0; i < count; i++){
			int index = (int)((value >>> ((count - i - 1) * MASK_BITS)) & MASK);
			buffer[offset + i] = ENCODING_CHARS[index];
		}

	}
}
