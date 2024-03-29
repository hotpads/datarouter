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
package io.datarouter.storage.file;

import java.time.Instant;
import java.util.Comparator;

public record BucketAndKeyVersionResult(
		String bucket,
		String key,
		Instant timestamp,
		String version,
		BucketObjectType type,
		long size){

	public static final Comparator<BucketAndKeyVersionResult> COMPARATOR
			= Comparator.comparing(BucketAndKeyVersionResult::bucket)
					.thenComparing(BucketAndKeyVersionResult::key)
					// putting timestamp before version for now because i don't know if versions are sorted
					.thenComparing(BucketAndKeyVersionResult::timestamp)
					.thenComparing(BucketAndKeyVersionResult::version);

	@Override
	public String toString(){
		return String.join("/", bucket, key, timestamp.toString(), version);
	}

	public boolean isFile(){
		return type == BucketObjectType.FILE;
	}

	public boolean isTombstone(){
		return type == BucketObjectType.TOMBSTONE;
	}

}
