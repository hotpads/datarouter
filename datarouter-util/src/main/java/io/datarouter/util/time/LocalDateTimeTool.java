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
package io.datarouter.util.time;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class LocalDateTimeTool{

	public static LocalDateTime nowMicros(){
		return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
	}

	public static LocalDateTime nowUtcMicros(){
		return LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MICROS);
	}

	public static Date getDate(LocalDateTime localDateTime){
		if(localDateTime == LocalDateTime.MIN){
			return new Date(0);
		}
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDateTime convertToLocalDateTime(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

}
