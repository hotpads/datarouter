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
package io.datarouter.util.number;

import io.datarouter.util.string.StringTool;

public class LongTool{

	private static final int MAX_POSITIVE_LONG_NUM_DIGITS = Long.toString(Long.MAX_VALUE).length();

	public static String paddedEpocMilli(long epochMilli){
		return StringTool.pad(Long.toString(epochMilli), '0', MAX_POSITIVE_LONG_NUM_DIGITS);
	}

	public static Long fromPaddedEpocMilli(String paddedEpocMilli){
		return Long.parseLong(paddedEpocMilli);
	}

}
