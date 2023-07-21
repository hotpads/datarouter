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
package io.datarouter.bytes.blockfile.dto;

import java.util.Collection;
import java.util.Comparator;

import io.datarouter.bytes.ByteLength;

public record BlockfileNameAndSize(
		String name,
		long size){

	public static final Comparator<BlockfileNameAndSize> COMPARE_SIZE_AND_NAME = Comparator
			.comparing(BlockfileNameAndSize::size)
			.thenComparing(BlockfileNameAndSize::name);

	public static ByteLength totalSize(Collection<BlockfileNameAndSize> files){
		long numBytes = files.stream()
				.mapToLong(BlockfileNameAndSize::size)
				.sum();
		return ByteLength.ofBytes(numBytes);
	}

}