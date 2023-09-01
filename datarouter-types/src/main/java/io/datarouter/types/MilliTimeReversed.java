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
package io.datarouter.types;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * MilliTimeReversed is a time in milliseconds since the epoch, but the value is Long.MAX_VALUE - epochMillis.
 *
 * This allows for efficient sorting in reverse chronological order. MilliTimeReversed is immutable. It is also
 * comparable to other MilliTimeReverseds, so it can be used in sorted collections.
 */
public final class MilliTimeReversed
implements BaseMilliTime<MilliTimeReversed>, Comparable<MilliTimeReversed>{

	public static final MilliTimeReversed MIN = new MilliTimeReversed(Long.MAX_VALUE);
	public static final MilliTimeReversed MAX = new MilliTimeReversed(0L);

	private final long reversedEpochMilli;

	private MilliTimeReversed(long reversedEpochMilli){
		this.reversedEpochMilli = reversedEpochMilli;
	}

	public static MilliTimeReversed ofEpochMilli(long epochMilli){
		return new MilliTimeReversed(Long.MAX_VALUE - epochMilli);
	}

	public static MilliTimeReversed ofReversedEpochMilli(long reversedEpochMilli){
		return new MilliTimeReversed(reversedEpochMilli);
	}

	public static MilliTimeReversed of(Instant instant){
		return ofEpochMilli(instant.toEpochMilli());
	}

	public static MilliTimeReversed of(Date date){
		return ofEpochMilli(date.getTime());
	}

	public static MilliTimeReversed of(ZonedDateTime date){
		return of(date.toInstant());
	}

	/*-----------------------------------------------------------------------*/

	@Override
	public int compareTo(MilliTimeReversed other){
		return Long.compare(reversedEpochMilli, other.reversedEpochMilli);
	}

	@Override
	public String toString(){
		return Long.toString(reversedEpochMilli);
	}

	@Override
	public int hashCode(){
		return Long.hashCode(reversedEpochMilli);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		MilliTimeReversed other = (MilliTimeReversed)obj;
		return Objects.equals(reversedEpochMilli, other.reversedEpochMilli);
	}

	@Override
	public long toEpochMilli(){
		return Long.MAX_VALUE - reversedEpochMilli;
	}

	@Override
	public long toReversedEpochMilli(){
		return reversedEpochMilli;
	}

	/*-----------------------------------------------------------------------*/

	public static MilliTimeReversed now(){
		return ofEpochMilli(System.currentTimeMillis());
	}

}
