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
package io.datarouter.bytes;

import java.io.InputStream;
import java.util.List;

/**
 * Some situations like S3 multipart upload require wrapping a known quantity of bytes into an InputStream.
 */
public record InputStreamAndLength(
		InputStream inputStream,
		long length){

	public InputStreamAndLength(List<byte[]> arrays){
		this(new MultiByteArrayInputStream(arrays), ByteTool.totalLength(arrays));
	}

}
