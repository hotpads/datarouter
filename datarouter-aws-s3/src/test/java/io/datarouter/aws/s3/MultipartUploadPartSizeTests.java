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

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteLength;
import io.datarouter.scanner.Scanner;

public class MultipartUploadPartSizeTests{
	private static final Logger logger = LoggerFactory.getLogger(MultipartUploadPartSizeTests.class);

	@Test
	public void testMiscPartSizes(){
		Assert.assertEquals(MultipartUploadPartSize.sizeForPart(9), ByteLength.ofMiB(5).toBytes());
		Assert.assertEquals(MultipartUploadPartSize.sizeForPart(250), ByteLength.ofMiB(20).toBytes());
		Assert.assertEquals(MultipartUploadPartSize.sizeForPart(9500), ByteLength.ofMiB(640).toBytes());
		Assert.assertThrows(IllegalArgumentException.class, () -> MultipartUploadPartSize.sizeForPart(10_001));
	}

	@Test
	public void testMaxFileSize(){
		long maxSize = MultipartUploadPartSize.totalSizeOfNParts(S3Limits.MAX_MULTIPART_UPLOAD_PARTS);
		Assert.assertTrue(maxSize >= S3Limits.MAX_FILE_SIZE_BYTES);
	}

	public static void main(String... args){
		logger.warn("thresholds:\n{}", makeThresholdTable());
	}

	private static String makeThresholdTable(){
		return Scanner.of(MultipartUploadPartSize.THRESHOLDS)
				.map(threshold -> String.format(
						"lastPartIndex=%s, partSizeMiB=%s, totalSize=%s",
						threshold.lastPartIndex(),
						threshold.partSizeBytes() / 1024 / 1024,
						totalSizeOfNPartsString(threshold.lastPartIndex())))
				.collect(Collectors.joining("\n"));
	}

	private static String totalSizeOfNPartsString(int numParts){
		long bytes = MultipartUploadPartSize.totalSizeOfNParts(numParts);
		long nMiB = bytes / 1024 / 1024;
		long nGiB = bytes / 1024 / 1024 / 1024;
		long nTiB = bytes / 1024 / 1024 / 1024 / 1024;
		return String.format("%s MiB, %s GiB, %s TiB", nMiB, nGiB, nTiB);
	}

}