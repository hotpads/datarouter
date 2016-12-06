package com.hotpads.util.core.bytes;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.bytes.ByteUnitType.ByteUnitSystem;

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

	/** tests *********************************************************************************************************/
	public static class ByteUnitToolTests{
		@Test
		public void testFileSizeUnit(){
			long step1024 = 1024;
			long binUnit = 1;

			for(ByteUnitType byteUnit: ByteUnitType.getAscValues(ByteUnitSystem.BINARY)){
				Assert.assertEquals(binUnit, byteUnit.getNumBytes());
				binUnit = step1024 * binUnit;
			}

			long step1000 = 1000;
			long decUnit = 1;
			for(ByteUnitType byteUnit: ByteUnitType.getAscValues(ByteUnitSystem.DECIMAL)){
				Assert.assertEquals(decUnit, byteUnit.getNumBytes());
				decUnit = step1000 * decUnit;
			}
		}

		@Test
		public void testByteCountToDisplaySize(){
			//binary system
			long numBytes = Long.MAX_VALUE;
			Assert.assertEquals("8,192.00 PiB",byteCountToDisplaySize(numBytes));
			numBytes = 1L << 50;
			Assert.assertEquals("1.00 PiB", byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 TiB", byteCountToDisplaySize(numBytes - 1));

			numBytes = 1L << 40;
			Assert.assertEquals("1.00 TiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 GiB",byteCountToDisplaySize(numBytes - 1));

			numBytes = 1L << 30;
			Assert.assertEquals("1.00 GiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 MiB",byteCountToDisplaySize(numBytes - 1));

			numBytes = 1L << 20;
			Assert.assertEquals("1.00 MiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 KiB",byteCountToDisplaySize(numBytes - 1));

			numBytes = 1L << 10;
			Assert.assertEquals("1.00 KiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.00 B",byteCountToDisplaySize(numBytes - 1));

			Assert.assertEquals("0.00 B",byteCountToDisplaySize(0L));

			//decimal system
			numBytes = (long) Math.pow(10, 15);
			Assert.assertEquals("9,223.37 PB",
					byteCountToDisplaySize(Long.MAX_VALUE, ByteUnitSystem.DECIMAL));
			Assert.assertEquals("1.00 PB", byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL));
			Assert.assertEquals("999.99 TB", byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL));

			numBytes = (long) Math.pow(10, 12);
			Assert.assertEquals("1.00 TB",byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL));
			Assert.assertEquals("999.99 GB",byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL));

			numBytes = (long) Math.pow(10, 9);
			Assert.assertEquals("1.00 GB",byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL));
			Assert.assertEquals("999.99 MB",byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL));

			numBytes = (long) Math.pow(10, 6);
			Assert.assertEquals("1.00 MB",byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL));
			Assert.assertEquals("999.99 KB",byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL));

			numBytes = (long) Math.pow(10, 3);
			Assert.assertEquals("1.00 KB",byteCountToDisplaySize(numBytes, ByteUnitSystem.DECIMAL));
			Assert.assertEquals("999.00 B",byteCountToDisplaySize(numBytes - 1, ByteUnitSystem.DECIMAL));

			Assert.assertEquals("0.00 B",byteCountToDisplaySize(0L, ByteUnitSystem.DECIMAL));

			Assert.assertEquals(48576, ByteUnitType.MiB.getNumBytes() - ByteUnitType.MB.getNumBytes());
			Assert.assertEquals(73741824, ByteUnitType.GiB.getNumBytes() - ByteUnitType.GB.getNumBytes());
			Assert.assertEquals(99511627776L, ByteUnitType.TiB.getNumBytes() - ByteUnitType.TB.getNumBytes());
		}
	}
}
