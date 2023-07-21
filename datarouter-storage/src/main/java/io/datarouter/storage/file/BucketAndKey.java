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

import java.util.Objects;

/**
 * Bucket name and Object key are frequenly passed around together.
 * This wraps them into a more type safe object.
 */
public class BucketAndKey{

	private static final boolean VALIDATE_BY_DEFAULT = true;

	private final String bucket;
	private final String key;

	private BucketAndKey(String bucket, String key, boolean validate){
		if(validate){
			validateBlobStorageCompatibility(key);
		}
		this.bucket = bucket;
		this.key = key;
	}

	public BucketAndKey(String bucket, String key){
		this(bucket, key, VALIDATE_BY_DEFAULT);
	}

	public static BucketAndKey withoutBlobStorageCompatibilityValidation(String bucket, String key){
		return new BucketAndKey(bucket, key, false);
	}

	public static String validateBlobStorageCompatibility(String key){
		PathbeanKey.of(key);
		return key;
	}

	public String bucket(){
		return bucket;
	}

	public String key(){
		return key;
	}

	/*------- Object --------*/

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		BucketAndKey other = (BucketAndKey)obj;
		return Objects.equals(this.bucket, other.bucket) && Objects.equals(this.key, other.key);
	}

	@Override
	public int hashCode(){
		return Objects.hash(bucket, key);
	}

	@Override
	public String toString(){
		return bucket + "/" + key;
	}

}
