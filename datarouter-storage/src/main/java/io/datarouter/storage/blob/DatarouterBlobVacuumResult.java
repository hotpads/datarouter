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
package io.datarouter.storage.blob;

import java.time.Instant;
import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.number.NumberFormatter;

public record DatarouterBlobVacuumResult(
		BucketAndPrefix bucketAndPrefix,
		Instant cutOffTime,
		long objectsConsidered,
		long objectsDeleted,
		ByteLength bytesConsidered,
		ByteLength bytesDeleted){

	public static long totalObjectsConsidered(List<DatarouterBlobVacuumResult> results){
		return results.stream()
				.mapToLong(DatarouterBlobVacuumResult::objectsConsidered)
				.sum();
	}

	public static long totalObjectsDeleted(List<DatarouterBlobVacuumResult> results){
		return results.stream()
				.mapToLong(DatarouterBlobVacuumResult::objectsDeleted)
				.sum();
	}

	public static ByteLength totalBytesConsidered(List<DatarouterBlobVacuumResult> results){
		return Scanner.of(results)
				.map(DatarouterBlobVacuumResult::bytesConsidered)
				.listTo(ByteLength::sum);
	}

	public static ByteLength totalBytesDeleted(List<DatarouterBlobVacuumResult> results){
		return Scanner.of(results)
				.map(DatarouterBlobVacuumResult::bytesDeleted)
				.listTo(ByteLength::sum);
	}

	public KvString toKvString(){
		return toKvStrings(List.of(this));
	}

	public static KvString toKvStrings(List<DatarouterBlobVacuumResult> results){
		return new KvString()
				.add(
						"objectsConsidered",
						DatarouterBlobVacuumResult.totalObjectsConsidered(results),
						NumberFormatter::addCommas)
				.add(
						"objectsDeleted",
						DatarouterBlobVacuumResult.totalObjectsDeleted(results),
						NumberFormatter::addCommas)
				.add(
						"bytesConsidered",
						DatarouterBlobVacuumResult.totalBytesConsidered(results),
						ByteLength::toDisplay)
				.add(
						"bytesDeleted",
						DatarouterBlobVacuumResult.totalBytesDeleted(results),
						ByteLength::toDisplay);
	}

}