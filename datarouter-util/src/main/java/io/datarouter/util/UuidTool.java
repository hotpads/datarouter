/**
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
package io.datarouter.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public class UuidTool{

	private static final long GREGORIAN_CALENDAR_BEGINNING = ZonedDateTime.of(1582, 10, 15, 0, 0, 0, 0, ZoneOffset.UTC)
			.toInstant()
			.toEpochMilli();

	public static String generateV1Uuid(){
		EthernetAddress addr = EthernetAddress.fromInterface();
		TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
		UUID uuid = uuidGenerator.generate();
		return uuid.toString();
	}

	public static Optional<Long> getTimestamp(String v1Uuid){
		long timestamp;
		try{
			UUID uuid = UUID.fromString(v1Uuid);
			timestamp = uuid.timestamp();
		}catch(Exception e){
			return Optional.empty();
		}
		long ms = timestamp / 10_000;
		return Optional.of(ms + GREGORIAN_CALENDAR_BEGINNING);
	}

}
