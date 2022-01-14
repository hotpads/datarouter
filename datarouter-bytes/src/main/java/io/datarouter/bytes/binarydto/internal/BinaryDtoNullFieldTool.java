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
package io.datarouter.bytes.binarydto.internal;

import io.datarouter.bytes.Java9;

public class BinaryDtoNullFieldTool{

	// Define null behavior.  Nulls will sort before non-nulls for unsigned byte comparisons.
	public static final int NULL_INDICATOR_LENGTH = 1;
	public static final byte NULL_INDICATOR_TRUE = 0;//all zero bits
	public static final byte[] NULL_INDICATOR_TRUE_ARRAY = new byte[]{NULL_INDICATOR_TRUE};
	public static final byte NULL_INDICATOR_FALSE = 1;//rightmost one bit
	public static final byte[] NULL_INDICATOR_FALSE_ARRAY = new byte[]{NULL_INDICATOR_FALSE};
	static{
		if(Java9.compareUnsigned(NULL_INDICATOR_TRUE_ARRAY, NULL_INDICATOR_FALSE_ARRAY) >= 0){
			throw new IllegalArgumentException("Nulls should sort before non-nulls.");
		}
	}

	public static boolean decodeNullIndicator(byte value){
		if(value == NULL_INDICATOR_FALSE){
			return false;
		}else if(value == NULL_INDICATOR_TRUE){
			return true;
		}else{
			throw new IllegalArgumentException("unknown nullIndicator=" + value);
		}
	}

}
