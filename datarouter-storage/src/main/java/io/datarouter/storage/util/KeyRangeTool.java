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
package io.datarouter.storage.util;

import io.datarouter.model.field.compare.FieldSetRangeFilter;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.tuple.Range;

public class KeyRangeTool{

	public static <PK extends PrimaryKey<PK>> Range<PK> forPrefix(PK prefix){
		return new Range<>(prefix, true, prefix, true);
	}

	public static <PK extends PrimaryKey<PK>> Range<PK> forPrefixWithWildcard(
			String prefixString,
			KeyWithStringFieldSuffixProvider<PK> keyWithStringFieldSuffixProvider){
		if(prefixString == null){
			return forPrefix(keyWithStringFieldSuffixProvider.createWithSuffixStringField(null));
		}
		String endString = incrementLastChar(prefixString);
		PK startKey = keyWithStringFieldSuffixProvider.createWithSuffixStringField(prefixString);
		PK endKey = keyWithStringFieldSuffixProvider.createWithSuffixStringField(endString);
		return new Range<>(startKey,endKey);
	}

	public static String incrementLastChar(String string){
		if(string == null){
			return null;
		}else if(string.isEmpty()){
			return String.valueOf(Character.MIN_VALUE);
		}
		int lastCharPos = string.length() - 1;
		char lastChar = string.charAt(lastCharPos);
		if(lastChar == Character.MAX_VALUE){
			// edge case where the last character can't be incremented
			return string + Character.MIN_VALUE;
		}
		return string.substring(0, lastCharPos) + (char) (lastChar + 1);
	}

	public interface KeyWithStringFieldSuffixProvider<PK extends PrimaryKey<PK>>{
		PK createWithSuffixStringField(String fieldValue);
	}

	public static <PK extends PrimaryKey<PK>> boolean contains(Range<PK> range, PK pk){
		return !isBeforeStartOfRange(range, pk) && !isAfterEndOfRange(range, pk);
	}

	public static <PK extends PrimaryKey<PK>> boolean isBeforeStartOfRange(Range<PK> range, PK pk){
		return range != null && !range.matchesStart(pk);
	}

	public static <PK extends PrimaryKey<PK>> boolean isAfterEndOfRange(Range<PK> range, PK pk){
		if(range == null || !range.hasEnd()){
			return false;
		}
		return !FieldSetRangeFilter.isCandidateBeforeEndOfRange(
				pk.getFields(),
				range.getEnd().getFields(),
				range.getEndInclusive());
	}

}
