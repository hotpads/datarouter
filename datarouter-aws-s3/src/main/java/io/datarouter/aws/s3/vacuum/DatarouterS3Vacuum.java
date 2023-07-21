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
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.time.ZoneIds;

public class DatarouterS3Vacuum{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterS3Vacuum.class);

	private final DatarouterS3Client s3Client;
	private final BucketAndPrefix bucketAndPrefix;
	private final Instant cutOffTime;
	private final boolean logEachObject;

	public DatarouterS3Vacuum(
			DatarouterS3Client s3Client,
			BucketAndPrefix bucketAndPrefix,
			Instant cutOffTime,
			boolean logEachObject){
		this.s3Client = s3Client;
		this.bucketAndPrefix = bucketAndPrefix;
		this.cutOffTime = cutOffTime;
		this.logEachObject = logEachObject;
	}

	public DatarouterS3VacuumResult vacuum(){
		var numObjectsConsidered = new AtomicLong();
		var numObjectsDeleted = new AtomicLong();
		var numBytesConsidered = new AtomicLong();
		var numBytesDeleted = new AtomicLong();
		s3Client.scan(bucketAndPrefix)
				.each(s3Object -> {
					numObjectsConsidered.incrementAndGet();
					numBytesConsidered.addAndGet(s3Object.size());
				})
				.include(s3Object -> s3Object.lastModified().isBefore(cutOffTime))
				.each(s3Object -> {
					if(logEachObject){
						logger.warn(
								"vacuuming bucket={}, key={}, lastModified={}, which is before the cutOff={}",
								bucketAndPrefix.bucket(),
								s3Object.key(),
								s3Object.lastModified().atZone(ZoneIds.UTC),
								cutOffTime.atZone(ZoneIds.UTC));
					}
				})
				.each(s3Object -> {
					numObjectsDeleted.incrementAndGet();
					numBytesDeleted.addAndGet(s3Object.size());
				})
				.map(s3Object -> new BucketAndKey(bucketAndPrefix.bucket(), s3Object.key()))
				.batch(S3Limits.MAX_DELETE_MULTI_KEYS)
				.map(BucketAndKeys::fromIndividualKeys)
				.forEach(s3Client::deleteMulti);
		return new DatarouterS3VacuumResult(
				bucketAndPrefix,
				cutOffTime,
				numObjectsConsidered.get(),
				numObjectsDeleted.get(),
				ByteLength.ofBytes(numBytesConsidered.get()),
				ByteLength.ofBytes(numBytesDeleted.get()));
	}

}
