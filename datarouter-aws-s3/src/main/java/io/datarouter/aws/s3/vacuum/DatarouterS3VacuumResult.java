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
import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.BucketAndPrefix;
import io.datarouter.util.number.NumberFormatter;

public record DatarouterS3VacuumResult(
		BucketAndPrefix bucketAndPrefix,
		Instant cutOffTime,
		long objectsConsidered,
		long objectsDeleted,
		ByteLength bytesConsidered,
		ByteLength bytesDeleted){

	public static long totalObjectsConsidered(List<DatarouterS3VacuumResult> results){
		return results.stream()
				.mapToLong(DatarouterS3VacuumResult::objectsConsidered)
				.sum();
	}

	public static long totalObjectsDeleted(List<DatarouterS3VacuumResult> results){
		return results.stream()
				.mapToLong(DatarouterS3VacuumResult::objectsDeleted)
				.sum();
	}

	public static ByteLength totalBytesConsidered(List<DatarouterS3VacuumResult> results){
		return Scanner.of(results)
				.map(DatarouterS3VacuumResult::bytesConsidered)
				.listTo(ByteLength::sum);
	}

	public static ByteLength totalBytesDeleted(List<DatarouterS3VacuumResult> results){
		return Scanner.of(results)
				.map(DatarouterS3VacuumResult::bytesDeleted)
				.listTo(ByteLength::sum);
	}

	public String toLogString(){
		return String.format(
				"finished objectsConsidered=%s, objectsDeleted=%s, bytesConsidered=%s, bytesDeleted=%s",
				NumberFormatter.addCommas(objectsConsidered),
				NumberFormatter.addCommas(objectsDeleted),
				bytesConsidered.toDisplay(),
				bytesDeleted.toDisplay());
	}

}