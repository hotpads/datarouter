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
package io.datarouter.aws.s3.vacuum;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.S3Limits;
import io.datarouter.bytes.ByteLength;
import io.datarouter.storage.file.BucketAndKeyVersion;
import io.datarouter.storage.file.BucketAndKeyVersions;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.time.ZoneIds;

public class DatarouterS3VersionVacuum{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterS3VersionVacuum.class);

	private final DatarouterS3Client s3Client;
	private final BucketAndPrefix bucketAndPrefix;
	private final Instant cutOffTime;
	private final boolean logEachVersion;

	public DatarouterS3VersionVacuum(
			DatarouterS3Client s3Client,
			BucketAndPrefix bucketAndPrefix,
			Instant cutOffTime,
			boolean logEachVersion){
		this.s3Client = s3Client;
		this.bucketAndPrefix = bucketAndPrefix;
		this.cutOffTime = cutOffTime;
		this.logEachVersion = logEachVersion;
	}

	public DatarouterS3VacuumResult vacuum(){
		var numObjectsConsidered = new AtomicLong();
		var numObjectsDeleted = new AtomicLong();
		var numBytesConsidered = new AtomicLong();
		var numBytesDeleted = new AtomicLong();
		s3Client.scanVersions(bucketAndPrefix)
				.each(version -> {
					numObjectsConsidered.incrementAndGet();
					numBytesConsidered.addAndGet(version.size());
				})
				.include(version -> version.timestamp().isBefore(cutOffTime))
				.each(version -> {
					if(logEachVersion){
						logger.warn(
								"vacuuming bucket={}, key={}, version={}, timestamp={}, which is before the cutOff={}",
								bucketAndPrefix.bucket(),
								version.key(),
								version.version(),
								version.timestamp().atZone(ZoneIds.UTC),
								cutOffTime.atZone(ZoneIds.UTC));
					}
				})
				.each(s3Object -> {
					numObjectsDeleted.incrementAndGet();
					numBytesDeleted.addAndGet(s3Object.size());
				})
				.map(version -> new BucketAndKeyVersion(bucketAndPrefix.bucket(), version.key(), version.version()))
				.batch(S3Limits.MAX_DELETE_MULTI_KEYS)
				.map(BucketAndKeyVersions::fromIndividualKeyVersions)
				.forEach(s3Client::deleteVersions);
		return new DatarouterS3VacuumResult(
				bucketAndPrefix,
				cutOffTime,
				numObjectsConsidered.get(),
				numObjectsDeleted.get(),
				ByteLength.ofBytes(numBytesConsidered.get()),
				ByteLength.ofBytes(numBytesDeleted.get()));
	}

}
