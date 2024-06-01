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
package io.datarouter.gcp.gcs.vacuum;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.gcp.gcs.DatarouterGcsClient;
import io.datarouter.gcp.gcs.GcsLimits;
import io.datarouter.storage.blob.DatarouterBlobVacuumResult;
import io.datarouter.storage.file.BucketAndKey;
import io.datarouter.storage.file.BucketAndKeys;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.time.ZoneIds;

public class DatarouterGcsVacuum{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterGcsVacuum.class);

	private final DatarouterGcsClient gcsClient;
	private final BucketAndPrefix bucketAndPrefix;
	private final Instant cutOffTime;
	private final boolean logEachObject;

	public DatarouterGcsVacuum(
			DatarouterGcsClient gcsClient,
			BucketAndPrefix bucketAndPrefix,
			Instant cutOffTime,
			boolean logEachObject){
		this.gcsClient = gcsClient;
		this.bucketAndPrefix = bucketAndPrefix;
		this.cutOffTime = cutOffTime;
		this.logEachObject = logEachObject;
	}

	public DatarouterBlobVacuumResult vacuum(){
		var numObjectsConsidered = new AtomicLong();
		var numObjectsDeleted = new AtomicLong();
		var numBytesConsidered = new AtomicLong();
		var numBytesDeleted = new AtomicLong();
		gcsClient.scanObjects(bucketAndPrefix)
				.each(gcsObject -> {
					numObjectsConsidered.incrementAndGet();
					numBytesConsidered.addAndGet(gcsObject.getSize());
				})
				.include(gcsObject -> gcsObject.getCreateTimeOffsetDateTime().toInstant().isBefore(cutOffTime))
				.each(gcsObject -> {
					if(logEachObject){
						logger.warn(
								"vacuuming bucket={}, key={}, lastModified={}, which is before the cutOff={}",
								bucketAndPrefix.bucket(),
								gcsObject.getName(),
								gcsObject.getCreateTimeOffsetDateTime().toInstant().atZone(ZoneIds.UTC),
								cutOffTime.atZone(ZoneIds.UTC));
					}
				})
				.each(gcsObject -> {
					numObjectsDeleted.incrementAndGet();
					numBytesDeleted.addAndGet(gcsObject.getSize());
				})
				.map(gcsObject -> BucketAndKey.withoutBlobStorageCompatibilityValidation(
						bucketAndPrefix.bucket(),
						gcsObject.getName()))
				.batch(GcsLimits.MAX_DELETE_MULTI_KEYS)
				.map(BucketAndKeys::fromIndividualKeys)
				.forEach(gcsClient::deleteObjects);
		return new DatarouterBlobVacuumResult(
				bucketAndPrefix,
				cutOffTime,
				numObjectsConsidered.get(),
				numObjectsDeleted.get(),
				ByteLength.ofBytes(numBytesConsidered.get()),
				ByteLength.ofBytes(numBytesDeleted.get()));
	}

}
