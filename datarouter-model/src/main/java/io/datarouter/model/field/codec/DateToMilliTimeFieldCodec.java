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
package io.datarouter.model.field.codec;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.Codec.NullPassthroughCodec;
import io.datarouter.types.MilliTime;

/**
 * @deprecated  convert Databean fields to MilliTime and use MilliTimeIdentityFieldCodec
 */
@Deprecated
public class DateToMilliTimeFieldCodec
extends FieldCodec<Date,MilliTime>{

	public DateToMilliTimeFieldCodec(){
		super(TypeToken.get(Date.class),
				NullPassthroughCodec.of(MilliTime::of, MilliTime::toDate),
				Date::compareTo,
				Date.from(Instant.now()),
				null);
	}

	@Override
	public Optional<String> findAuxiliaryHumanReadableString(
			Date date,
			DateTimeFormatter dateTimeFormatter,
			ZoneId zoneId){
		var zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), zoneId);
		return Optional.of(dateTimeFormatter.format(zonedDateTime));
	}

}
