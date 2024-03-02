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
package io.datarouter.bytes.blockfile.index;

import io.datarouter.bytes.blockfile.row.BlockfileRowVersion;

public record BlockfileRowRange(
		BlockfileRowVersion first,
		BlockfileRowVersion last){

	public int sumOfLengths(){
		return first().length() + last.length();
	}

	public boolean containsKey(byte[] key){
		return first.compareToKey(key) <= 0
				&& last.compareToKey(key) >= 0;
	}

	public int compareToKey(byte[] key){
		if(last.compareToKey(key) < 0){// This range is fully before the key
			return -1;
		}else if(first.compareToKey(key) > 0){// This range is fully after the key
			return 1;
		}else{
			return 0;
		}
	}
}
