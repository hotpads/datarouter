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
package io.datarouter.aws.s3;

import io.datarouter.bytes.ByteLength;

public class S3Limits{

	public static final int MIN_BUCKET_NAME_LENGTH = 3;
	public static final int MAX_BUCKET_NAME_LENGTH = 63;
	public static final int MAX_KEY_LENGTH = 1_024;
	public static final int MAX_RESULTS_PER_LIST_OBJECTS_PAGE = 1_000;
	public static final long MAX_S3_FILE_SIZE_BYTES = ByteLength.ofTiB(5).toBytes();
	public static final int MAX_MULTIPART_UPLOAD_PARTS = 10_000;
	public static final int MAX_DELETE_MULTI_KEYS = 1_000;

}