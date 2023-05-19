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

import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

/**
 * A single bucket with multiple keys.
 */
public record BucketAndKeys(
		String bucket,
		List<String> keys){

	public static BucketAndKeys fromIndividualKeys(List<BucketAndKey> inputs){
		boolean multipleBuckets = Scanner.of(inputs)
				.map(BucketAndKey::bucket)
				.deduplicateConsecutive()
				.skip(1)
				.hasAny();
		Require.isFalse(multipleBuckets, "All objects must be in the same bucket");
		return Scanner.of(inputs)
				.map(BucketAndKey::key)
				.listTo(keys -> new BucketAndKeys(inputs.get(0).bucket(), keys));
	}
}
