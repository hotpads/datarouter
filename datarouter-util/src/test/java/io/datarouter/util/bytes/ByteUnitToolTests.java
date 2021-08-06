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
package io.datarouter.util.bytes;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.bytes.ByteUnitType.ByteUnitSystem;

public class ByteUnitToolTests{

	@Test
	public void testFileSizeUnit(){
		long step1024 = 1024;
		long binUnit = 1;

		for(ByteUnitType byteUnit : ByteUnitType.getAscValues(ByteUnitSystem.BINARY)){
			Assert.assertEquals(byteUnit.getNumBytes(), binUnit);
			binUnit = step1024 * binUnit;
		}

		long step1000 = 1000;
		long decUnit = 1;
		for(ByteUnitType byteUnit : ByteUnitType.getAscValues(ByteUnitSystem.DECIMAL)){
			Assert.assertEquals(byteUnit.getNumBytes(), decUnit);
			decUnit = step1000 * decUnit;
		}
	}

	@Test
	public void testByteCountToDisplaySize(){
		//binary system
		long numBytes = Long.MAX_VALUE;
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes), "8190 PiB");
		numBytes = 1L << 50;
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes), "1 PiB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1), "1020 TiB");

		numBytes = 1L << 40;
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes), "1 TiB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1), "1020 GiB");

		numBytes = 1L << 30;
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes), "1 GiB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1), "1020 MiB");

		numBytes = 1L << 20;
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes), "1 MiB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1), "1020 KiB");

		numBytes = 1L << 10;
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes), "1 KiB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1), "1020 B");

		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(0L), "0 B");

		//decimal system
		numBytes = (long) Math.pow(10, 15);
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(Long.MAX_VALUE, ByteUnitSystem.DECIMAL), "9220 PB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1 PB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "1000 TB");

		numBytes = (long) Math.pow(10, 12);
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1 TB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "1000 GB");

		numBytes = (long) Math.pow(10, 9);
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1 GB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "1000 MB");

		numBytes = (long) Math.pow(10, 6);
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1 MB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "1000 KB");

		numBytes = (long) Math.pow(10, 3);
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL), "1 KB");
		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL), "999 B");

		Assert.assertEquals(ByteUnitTool.byteCountToDisplaySize(0L, ByteUnitSystem.DECIMAL), "0 B");

		Assert.assertEquals(ByteUnitType.MiB.getNumBytes() - ByteUnitType.MB.getNumBytes(), 48576);
		Assert.assertEquals(ByteUnitType.GiB.getNumBytes() - ByteUnitType.GB.getNumBytes(), 73741824);
		Assert.assertEquals(ByteUnitType.TiB.getNumBytes() - ByteUnitType.TB.getNumBytes(), 99511627776L);
	}

	@Test
	public void testToBytes(){
		Assert.assertEquals(ByteUnitType.KiB.toBytes(3), 3 * 1024);
	}

}
