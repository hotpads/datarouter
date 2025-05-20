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
package io.datarouter.gcp.gcs.util;

import java.util.List;
import java.util.stream.IntStream;

import io.datarouter.scanner.Scanner;

public record GcsMultipartUploadPartSize(
		int partSizeBytes,
		int lastPartIndex){

	public static final int INITIAL_BUFFER_SIZE_BYTES = GcsLimits.MIN_PART_SIZE.toBytesInt();

	// reflects the fact that ByteArrayOutputStream only doubles in size
	private static int bufferSize = INITIAL_BUFFER_SIZE_BYTES;
	public static final List<GcsMultipartUploadPartSize> THRESHOLDS = List.of(
			new GcsMultipartUploadPartSize(bufferSize, 100),
			new GcsMultipartUploadPartSize(bufferSize *= 2, 200),
			new GcsMultipartUploadPartSize(bufferSize *= 2, 400),
			new GcsMultipartUploadPartSize(bufferSize *= 2, 800),
			new GcsMultipartUploadPartSize(bufferSize *= 2, 1_200),
			new GcsMultipartUploadPartSize(bufferSize *= 2, 1_600),
			new GcsMultipartUploadPartSize(bufferSize *= 2, 2_300),
			new GcsMultipartUploadPartSize(bufferSize *= 2, GcsLimits.MAX_MULTIPART_UPLOAD_PARTS));

	public static int sizeForPart(int partId){
		return Scanner.of(THRESHOLDS)
				.include(threshold -> partId <= threshold.lastPartIndex)
				.findFirst()
				.map(GcsMultipartUploadPartSize::partSizeBytes)
				.orElseThrow(() -> new IllegalArgumentException("illegal partId=" + partId
						+ ", max=" + GcsLimits.MAX_MULTIPART_UPLOAD_PARTS));
	}

	public static long totalSizeOfNParts(int numParts){
		return IntStream.rangeClosed(1, numParts)
				.mapToLong(GcsMultipartUploadPartSize::sizeForPart)
				.sum();
	}

}
