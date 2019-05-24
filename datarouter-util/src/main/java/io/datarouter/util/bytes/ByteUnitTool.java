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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.bytes.ByteUnitType.ByteUnitSystem;

public class ByteUnitTool{

	public static final long KiB = ByteUnitType.KiB.getNumBytes();//kibi
	public static final long MiB = ByteUnitType.MiB.getNumBytes();//mebi

	public static String byteCountToDisplaySize(long sizeInBytes){
		return byteCountToDisplaySize(sizeInBytes, ByteUnitSystem.BINARY);
	}

	private static String byteCountToDisplaySize(long sizeInBytes, ByteUnitSystem byteUnitSystem){
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

	public static class ByteUnitToolTests{

		@Test
		public void testFileSizeUnit(){
			long step1024 = 1024;
			long binUnit = 1;

			for(ByteUnitType byteUnit: ByteUnitType.getAscValues(ByteUnitSystem.BINARY)){
				Assert.assertEquals(byteUnit.getNumBytes(), binUnit);
				binUnit = step1024 * binUnit;
			}

			long step1000 = 1000;
			long decUnit = 1;
			for(ByteUnitType byteUnit: ByteUnitType.getAscValues(ByteUnitSystem.DECIMAL)){
				Assert.assertEquals(byteUnit.getNumBytes(), decUnit);
				decUnit = step1000 * decUnit;
			}
		}

		@Test
		public void testByteCountToDisplaySize(){
			//binary system
			long numBytes = Long.MAX_VALUE;
			Assert.assertEquals(byteCountToDisplaySize(numBytes), "8,192.00 PiB");
			numBytes = 1L << 50;
			Assert.assertEquals(byteCountToDisplaySize(numBytes), "1.00 PiB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1), "1,023.99 TiB");

			numBytes = 1L << 40;
			Assert.assertEquals(byteCountToDisplaySize(numBytes), "1.00 TiB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1), "1,023.99 GiB");

			numBytes = 1L << 30;
			Assert.assertEquals(byteCountToDisplaySize(numBytes), "1.00 GiB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1), "1,023.99 MiB");

			numBytes = 1L << 20;
			Assert.assertEquals(byteCountToDisplaySize(numBytes), "1.00 MiB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1), "1,023.99 KiB");

			numBytes = 1L << 10;
			Assert.assertEquals(byteCountToDisplaySize(numBytes), "1.00 KiB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1), "1,023.00 B");

			Assert.assertEquals(byteCountToDisplaySize(0L), "0.00 B");

			//decimal system
			numBytes = (long) Math.pow(10, 15);
			Assert.assertEquals(byteCountToDisplaySize(Long.MAX_VALUE, ByteUnitSystem.DECIMAL), "9,223.37 PB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1.00 PB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "999.99 TB");

			numBytes = (long) Math.pow(10, 12);
			Assert.assertEquals(byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1.00 TB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "999.99 GB");

			numBytes = (long) Math.pow(10, 9);
			Assert.assertEquals(byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1.00 GB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "999.99 MB");

			numBytes = (long) Math.pow(10, 6);
			Assert.assertEquals(byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1.00 MB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "999.99 KB");

			numBytes = (long) Math.pow(10, 3);
			Assert.assertEquals(byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1.00 KB");
			Assert.assertEquals(byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "999.00 B");

			Assert.assertEquals(byteCountToDisplaySize(0L, ByteUnitSystem.DECIMAL), "0.00 B");

			Assert.assertEquals(ByteUnitType.MiB.getNumBytes() - ByteUnitType.MB.getNumBytes(), 48576);
			Assert.assertEquals(ByteUnitType.GiB.getNumBytes() - ByteUnitType.GB.getNumBytes(), 73741824);
			Assert.assertEquals(ByteUnitType.TiB.getNumBytes() - ByteUnitType.TB.getNumBytes(), 99511627776L);
		}
	}

}
