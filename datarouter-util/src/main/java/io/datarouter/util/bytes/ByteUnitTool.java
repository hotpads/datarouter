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
package io.datarouter.util.bytes;

import io.datarouter.util.bytes.ByteUnitType.ByteUnitSystem;

public class ByteUnitTool{

	public static final long KiB = ByteUnitType.KiB.getNumBytes();//kibi
	public static final long MiB = ByteUnitType.MiB.getNumBytes();//mebi

	public static String byteCountToDisplaySize(long sizeInBytes){
		return byteCountToDisplaySize(sizeInBytes, ByteUnitSystem.BINARY);
	}

	public static String byteCountToDisplaySize(long sizeInBytes, ByteUnitSystem byteUnitSystem){
		if(sizeInBytes < 0){
			return null;
		}
		if(byteUnitSystem == null){
			return ByteUnitType.BYTE.getNumBytesDisplay(sizeInBytes);
		}

		Long step = byteUnitSystem.getStep();
		for(ByteUnitType unit : ByteUnitType.getAscValues(byteUnitSystem)){
			if(step.compareTo(Math.abs(sizeInBytes / unit.getNumBytes())) <= 0){
				continue;
			}
			return unit.getNumBytesDisplay(sizeInBytes);
		}

		if(ByteUnitSystem.BINARY == byteUnitSystem){
			return ByteUnitType.PiB.getNumBytesDisplay(sizeInBytes);
		}
		return ByteUnitType.PB.getNumBytesDisplay(sizeInBytes);
	}

	/*------------------------- tests ---------------------------------------*/

}
