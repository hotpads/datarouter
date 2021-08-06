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
package io.datarouter.client.hbase.util;

import java.util.Comparator;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseResultComparator implements Comparator<Result>{

	private final int numPrefixBytesToIgnore;

	public HBaseResultComparator(int numPrefixBytes){
		this.numPrefixBytesToIgnore = numPrefixBytes;
	}

	@Override
	public int compare(Result left, Result right){
		int leftLength = left.getRow().length - numPrefixBytesToIgnore;
		int rightLength = right.getRow().length - numPrefixBytesToIgnore;
		return Bytes.compareTo(left.getRow(), numPrefixBytesToIgnore, leftLength, right.getRow(),
				numPrefixBytesToIgnore, rightLength);
	}

}
