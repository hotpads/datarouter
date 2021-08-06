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
package io.datarouter.instrumentation.trace;

/*
 * Maximum number of flag mask types is 8 with each bit field controlling different types of samplings.
 */
public enum TraceContextFlagMask{
	DEFAULT((byte)0b00000000),
	TRACE((byte)0b00000001),
	LOG((byte)0b00000010);

	private final byte mask;

	TraceContextFlagMask(byte mask){
		this.mask = mask;
	}

	public static String enableTrace(String curTraceFlags){
		return isTraceEnabled(curTraceFlags) ? curTraceFlags : setFlag(curTraceFlags, TRACE);
	}

	public static String enableLog(String curTraceFlags){
		return isLogEnabled(curTraceFlags) ? curTraceFlags : setFlag(curTraceFlags, LOG);
	}

	public static boolean isTraceEnabled(String hex){
		return isFlagSet(hex, TRACE);
	}

	public static boolean isLogEnabled(String hex){
		return isFlagSet(hex, LOG);
	}

	public String toHexCode(){
		return String.format("%02x", mask);
	}

	public byte getByteMask(){
		return mask;
	}

	/*
	 * trace-flags is hex-encoded ranged from 00 to ff (all 8 flags/bits set)
	 */
	private static boolean isFlagSet(String hexflags, TraceContextFlagMask sample){
		if(hexflags == null || hexflags.length() < 2){
			return false;
		}
		byte flag = (byte)Integer.parseInt(hexflags, 16);
		return (flag & sample.mask) == sample.mask;
	}

	private static String setFlag(String hexflags, TraceContextFlagMask sample){
		if(hexflags == null || hexflags.length() < 2){
			return hexflags;
		}
		byte flag = (byte)Integer.parseInt(hexflags, 16);
		return String.format("%02x", flag | sample.mask);
	}

}
