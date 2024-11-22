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
package io.datarouter.bytes.blockfile.io.storage;

import java.util.Arrays;

/**
 * Holds bytes for some arbitrary position in the file.
 * You can extract the specific bytes you want, getting an exception if they were not fully contained in the segment.
 */
public record BlockfileSegment(
		String name,
		long from,
		byte[] bytes){

	public long to(){
		return from + bytes.length;
	}

	public boolean contains(BlockfileLocation location){
		return from <= location.from() && to() >= location.to();
	}

	public byte[] extractBytes(BlockfileLocation location){
		if(!contains(location)){
			String message = String.format(
					"segment (from=%s, to=%s) does not contain location (from=%s, to=%s)",
					from, to(), location.from(), location.to());
			throw new IllegalArgumentException(message);
		}
		long start = location.from() - from;
		long end = start + location.length();
		return Arrays.copyOfRange(bytes, Math.toIntExact(start), Math.toIntExact(end));
	}

}
