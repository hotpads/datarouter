package com.hotpads.util.core.map;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.StringTool;

public class DilatedBitTool28{
	
	//28 is 2 * 14, and 2^14 => 16k, so our table is 16k entries
	public static final int MAX_BITS = 14;
	public static final int MAX_DILATED_BITS = 2 * MAX_BITS;
	
	//build the lookup tables for dilation and contraction.  table will only be 16k values so will have to be used twice for each lookup
	//should match the table on page 446 of http://books.google.com/books?id=fvA7zLEFWZgC&pg=RA9-PA443&lpg=RA9-PA443&dq=quad+code+interleave+bits&source=web&ots=7L5Hw9kEVl&sig=BdOl_wxF8eGaY0gkNjs-i-lvSoA&hl=en#PRA9-PA446,M1
	//verbose for debugging
	public static final short HALF_GRID_BITS = MAX_DILATED_BITS >>> 1;  //levelBits halved.  28 >>> 1 is 14
	public static final short TABLE_SLOTS = 1 << HALF_GRID_BITS;
	public static final int TABLE_MASK = TABLE_SLOTS - 1;
	public static final int[] DILATED_TABLE = new int[TABLE_SLOTS];
	public static final int[] LEFT_SHIFTED_DILATED_TABLE = new int[TABLE_SLOTS];
	
	static{
		for(int i=0; i < TABLE_SLOTS; ++i){
			int dilated = 0;
			for(short shift=0; shift < HALF_GRID_BITS; ++shift){
				int mask = 1 << shift;
				int workingBit = i & mask;
				int dilatedWorkingBit = workingBit << shift;
				dilated += dilatedWorkingBit;
				DILATED_TABLE[i] = dilated;
				int dilatedShiftedLeft = dilated << 1;
				LEFT_SHIFTED_DILATED_TABLE[i] = dilatedShiftedLeft;
			}
		}
	}
	
	public static class DilatedBitToolTests {
		@Test public void testRightXBits(){
			long x = 13;
			String xBinaryString = "1101";
			Assert.assertEquals(xBinaryString, Long.toBinaryString(x));
			
			long rightPartOfX = x & DilatedBitTool28.TABLE_MASK;  //x and'd with 14 1's
			Assert.assertEquals(13, rightPartOfX);
			long dilatedRightPartOfX = DilatedBitTool28.DILATED_TABLE[(int)rightPartOfX];
			String dilatedRightPartOfXBinaryString = "1010001";  //omit leading 0
			Assert.assertEquals(dilatedRightPartOfXBinaryString, Long.toBinaryString(dilatedRightPartOfX));
			
			long leftPartOfX = x >>> 14;
			Assert.assertEquals(0, leftPartOfX);
			long dilatedLeftPartOfX = DilatedBitTool28.DILATED_TABLE[(int)leftPartOfX];
			Assert.assertEquals(0, dilatedLeftPartOfX);
		}
		@Test public void testLeftXBits(){
			long x = 1048583L;
			String xBinaryString = "1"+StringTool.repeat('0', 17)+"111";
			Assert.assertEquals(xBinaryString, Long.toBinaryString(x));
			
			long rightPartOfX = x & DilatedBitTool28.TABLE_MASK;  //x and'd with 14 1's
			Assert.assertEquals(7, rightPartOfX);
			long dilatedRightPartOfX = DilatedBitTool28.DILATED_TABLE[(int)rightPartOfX];
			String dilatedRightPartOfXBinaryString = "10101";  //omit leading 0
			Assert.assertEquals(dilatedRightPartOfXBinaryString, Long.toBinaryString(dilatedRightPartOfX));
			
			long leftPartOfX = x >>> 14;
			Assert.assertEquals(64, leftPartOfX);
			long dilatedLeftPartOfX = DilatedBitTool28.DILATED_TABLE[(int)leftPartOfX];
			Assert.assertEquals(4096, dilatedLeftPartOfX);
		}
		
	}
	
	public static void main (String[] args) {
		
		System.out.println("M_D_B: " + DilatedBitTool28.MAX_DILATED_BITS + " H_G_B: " + DilatedBitTool28.HALF_GRID_BITS);
		System.out.println("Table_slots: " + DilatedBitTool28.TABLE_SLOTS);
	}
}
