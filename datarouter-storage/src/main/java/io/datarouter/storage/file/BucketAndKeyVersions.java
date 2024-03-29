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

public record BucketAndKeyVersions(
		String bucket,
		List<KeyVersion> keyVersions){

	public static BucketAndKeyVersions fromIndividualKeyVersions(List<BucketAndKeyVersion> inputs){
		Require.notEmpty(inputs);
		boolean multipleBuckets = Scanner.of(inputs)
				.map(BucketAndKeyVersion::bucket)
				.deduplicateConsecutive()
				.skip(1)
				.hasAny();
		Require.isFalse(multipleBuckets, "All objects must be in the same bucket");
		return Scanner.of(inputs)
				.map(input -> new KeyVersion(input.key(), input.version()))
				.listTo(keyVersions -> new BucketAndKeyVersions(inputs.getFirst().bucket(), keyVersions));
	}

	public List<BucketAndKeyVersion> toIndividualKeyVersions(){
		return Scanner.of(keyVersions)
				.map(keyVersion -> new BucketAndKeyVersion(bucket, keyVersion.key(), keyVersion.version()))
				.list();
	}

	public record KeyVersion(
			String key,
			String version){
	}

}
