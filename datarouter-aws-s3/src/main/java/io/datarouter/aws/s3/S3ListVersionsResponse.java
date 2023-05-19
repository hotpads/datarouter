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

import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndKeyVersionResult;
import io.datarouter.storage.file.BucketObjectType;
import software.amazon.awssdk.services.s3.model.DeleteMarkerEntry;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

public record S3ListVersionsResponse(
		String bucket,
		List<ObjectVersion> versions,
		List<DeleteMarkerEntry> deleteMarkers){

	public List<BucketAndKeyVersionResult> list(){
		List<BucketAndKeyVersionResult> versionResults = Scanner.of(versions)
				.map(version -> new BucketAndKeyVersionResult(
						bucket,
						version.key(),
						version.lastModified(),
						version.versionId(),
						BucketObjectType.FILE,
						version.size()))
				.list();
		List<BucketAndKeyVersionResult> deleteMarkerResults = Scanner.of(deleteMarkers)
				.map(marker -> new BucketAndKeyVersionResult(
						bucket,
						marker.key(),
						marker.lastModified(),
						marker.versionId(),
						BucketObjectType.TOMBSTONE,
						0L))
				.list();
		// TODO replace sort with collate
		return Scanner.concat(versionResults, deleteMarkerResults)
				.sort(BucketAndKeyVersionResult.COMPARATOR)
				.list();
	}
}
