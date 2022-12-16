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
package io.datarouter.bytes;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteLength.ByteUnitSystem;
import io.datarouter.bytes.ByteLength.Unit;

public class ByteLengthTestTool{

	@Test
	public void testToBytes(){
		Assert.assertEquals(ByteLength.ofKiB(3).toBytes(), 3 * 1024);
		Assert.assertEquals(ByteLength.ofMiB(3).toBytes(), 3 * 1024 * 1024);
	}

	@Test
	public void testFileSizeUnit(){
		long step1024 = 1024;
		long binUnit = 1;

		for(Unit byteUnit : ByteLength.getAscValues(ByteUnitSystem.BINARY)){
			Assert.assertEquals(byteUnit.unitValue, binUnit);
			binUnit = step1024 * binUnit;
		}

		long step1000 = 1000;
		long decUnit = 1;
		for(Unit byteUnit : ByteLength.getAscValues(ByteUnitSystem.DECIMAL)){
			Assert.assertEquals(byteUnit.unitValue, decUnit);
			decUnit = step1000 * decUnit;
		}
	}

	@Test
	public void testByteCountToDisplaySize(){
		long numBytes = Long.MAX_VALUE;
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(), "8190 PiB");
		numBytes = 1L << 50;
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(), "1 PiB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(), "1020 TiB");

		numBytes = 1L << 40;
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(), "1 TiB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(), "1020 GiB");

		numBytes = 1L << 30;
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(), "1 GiB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(), "1020 MiB");

		numBytes = 1L << 20;
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(), "1 MiB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(), "1020 KiB");

		numBytes = 1L << 10;
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(), "1 KiB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(), "1020 B");

		Assert.assertEquals(ByteLength.ofBytes(0L).toDisplay(), "0 B");

		numBytes = (long)Math.pow(10, 15);
		Assert.assertEquals(ByteLength.ofBytes(Long.MAX_VALUE).toDisplay(ByteUnitSystem.DECIMAL), "9220 PB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(ByteUnitSystem.DECIMAL), "1 PB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(ByteUnitSystem.DECIMAL), "1000 TB");

		numBytes = (long)Math.pow(10, 12);
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(ByteUnitSystem.DECIMAL), "1 TB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(ByteUnitSystem.DECIMAL), "1000 GB");

		numBytes = (long)Math.pow(10, 9);
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(ByteUnitSystem.DECIMAL), "1 GB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(ByteUnitSystem.DECIMAL), "1000 MB");

		numBytes = (long)Math.pow(10, 6);
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(ByteUnitSystem.DECIMAL), "1 MB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(ByteUnitSystem.DECIMAL), "1000 KB");

		numBytes = (long)Math.pow(10, 3);
		Assert.assertEquals(ByteLength.ofBytes(numBytes).toDisplay(ByteUnitSystem.DECIMAL), "1 KB");
		Assert.assertEquals(ByteLength.ofBytes(numBytes - 1).toDisplay(ByteUnitSystem.DECIMAL), "999 B");

		Assert.assertEquals(ByteLength.ofBytes(0L).toDisplay(), "0 B");

		Assert.assertEquals(ByteLength.ofMiB(1).toBytes() - ByteLength.ofMB(1).toBytes(), 48576);
		Assert.assertEquals(ByteLength.ofGiB(1).toBytes() - ByteLength.ofGB(1).toBytes(), 73741824);
		Assert.assertEquals(ByteLength.ofTiB(1).toBytes() - ByteLength.ofTB(1).toBytes(), 99511627776L);
	}

}
