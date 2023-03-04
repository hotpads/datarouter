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
package io.datarouter.bytes.kvfile;

import java.util.Collection;
import java.util.Comparator;

import io.datarouter.bytes.ByteLength;

public record KvFileNameAndSize(
		String name,
		long size){

	public static final Comparator<KvFileNameAndSize> COMPARE_SIZE_AND_NAME = Comparator
			.comparing(KvFileNameAndSize::size)
			.thenComparing(KvFileNameAndSize::name);

	public static ByteLength totalSize(Collection<KvFileNameAndSize> files){
		long numBytes = files.stream()
				.mapToLong(KvFileNameAndSize::size)
				.sum();
		return ByteLength.ofBytes(numBytes);
	}

}
