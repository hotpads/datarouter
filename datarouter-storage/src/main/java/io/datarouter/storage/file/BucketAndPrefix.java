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

import io.datarouter.storage.util.Subpath;

/**
 * Bucket name and Object key prefix are frequenly passed around together.
 * This wraps them into a more type safe object.
 */
public record BucketAndPrefix(
		String bucket,
		String prefix){

	public BucketAndPrefix(
			String bucket,
			Subpath prefix){
		this(bucket, prefix.toString());
	}

	@Override
	public String toString(){
		return bucket + "/" + prefix;
	}

	public BucketAndKey toBucketAndKey(PathbeanKey pathbeanKey){
		return new BucketAndKey(
				bucket,
				prefix + pathbeanKey.getPathAndFile());
	}

}
