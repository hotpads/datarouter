package com.hotpads.util.core.bytes;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.bytes.ByteUnitType.ByteUnitSystem;

public class ByteUnitTool {
	public static final long KB = ByteUnitType.KB.getNumBytes();//kilo
	public static final long MB = ByteUnitType.MB.getNumBytes();//mega
	public static final long GB = ByteUnitType.GB.getNumBytes();//giga
	public static final long TB = ByteUnitType.TB.getNumBytes();//tera
	public static final long PB = ByteUnitType.PB.getNumBytes();//peta
	
	public static final long KiB = ByteUnitType.KiB.getNumBytes();//kibi
	public static final long MiB = ByteUnitType.MiB.getNumBytes();//mebi
	public static final long GiB = ByteUnitType.GiB.getNumBytes();//gibi
	public static final long TiB = ByteUnitType.TiB.getNumBytes();//tebi
	public static final long PiB = ByteUnitType.PiB.getNumBytes();//pebi
	
	public static String byteCountToDisplaySize(long sizeInBytes) {
		return byteCountToDisplaySize(sizeInBytes, ByteUnitSystem.BINARY);
	}
	
	public static String byteCountToDisplaySize(long sizeInBytes, ByteUnitSystem byteUnitSystem) {
		if (sizeInBytes < 0) {
			return null;
		}
		if(byteUnitSystem == null){
			return ByteUnitType.BYTE.getNumBytesDisplay(sizeInBytes);
		}
		
		Long step = byteUnitSystem.getStep();
		for (ByteUnitType unit : ByteUnitType.getAscValues(byteUnitSystem)) {
			if (step.compareTo(Math.abs(sizeInBytes / unit.getNumBytes())) <= 0) {
				continue;
			}
			return unit.getNumBytesDisplay(sizeInBytes);
		}
		
		if (ByteUnitSystem.BINARY == byteUnitSystem) {
			return ByteUnitType.PiB.getNumBytesDisplay(sizeInBytes);
		} else {
			return ByteUnitType.PB.getNumBytesDisplay(sizeInBytes);
		}
	}
	
	public static void main(String[] args) {
		long advertisedHddSize = 80 * ByteUnitType.GB.getNumBytes();
		System.out.println("ADVERTISED HDD SIZE: "
				+ byteCountToDisplaySize(advertisedHddSize, ByteUnitSystem.DECIMAL));
		System.out.println("ACTUAL HDD SIZE: "
				+ byteCountToDisplaySize(advertisedHddSize, ByteUnitSystem.BINARY));
		System.out.println();
		advertisedHddSize = 1000 * ByteUnitType.GB.getNumBytes();
		System.out.println("ADVERTISED HDD SIZE: "
				+ byteCountToDisplaySize(advertisedHddSize, ByteUnitSystem.DECIMAL));
		System.out.println("ACTUAL HDD SIZE: "
				+ byteCountToDisplaySize(advertisedHddSize, ByteUnitSystem.BINARY));
	}
	
	/** tests *********************************************************************************************************/
	public static class ByteUnitToolTests{
		@Test public void testFileSizeUnit(){
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
		
		@Test public void testByteCountToDisplaySize(){
			//binary system
			long numBytes = Long.MAX_VALUE;
			Assert.assertEquals("8,192.00 PiB",byteCountToDisplaySize(numBytes));
			numBytes = 1l << 50;
			Assert.assertEquals("1.00 PiB", byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 TiB", byteCountToDisplaySize(numBytes - 1));
			
			numBytes = 1l << 40;
			Assert.assertEquals("1.00 TiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 GiB",byteCountToDisplaySize(numBytes - 1));
			
			numBytes = 1l << 30;
			Assert.assertEquals("1.00 GiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 MiB",byteCountToDisplaySize(numBytes - 1));
			
			numBytes = 1l << 20;
			Assert.assertEquals("1.00 MiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.99 KiB",byteCountToDisplaySize(numBytes - 1));
			
			numBytes = 1l << 10;
			Assert.assertEquals("1.00 KiB",byteCountToDisplaySize(numBytes));
			Assert.assertEquals("1,023.00 B",byteCountToDisplaySize(numBytes - 1));

			Assert.assertEquals("0.00 B",byteCountToDisplaySize(0l));
			
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

			Assert.assertEquals("0.00 B",byteCountToDisplaySize(0l, ByteUnitSystem.DECIMAL));
			
			Assert.assertEquals(48576, ByteUnitType.MiB.getNumBytes() - ByteUnitType.MB.getNumBytes());
			Assert.assertEquals(73741824, ByteUnitType.GiB.getNumBytes() - ByteUnitType.GB.getNumBytes());
			Assert.assertEquals(99511627776l, ByteUnitType.TiB.getNumBytes() - ByteUnitType.TB.getNumBytes());
		}
	}
}
