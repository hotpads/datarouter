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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.types.Quad.DilatedBits;

public class QuadTests{

	@Test
	public void testConstants(){
		String tableMaskBitString = Integer.toBinaryString(DilatedBits.TABLE_MASK);
		for(int i = 0; i < DilatedBits.HALF_GRID_BITS; ++i){
			Assert.assertEquals(tableMaskBitString.charAt(i), '1');
		}
	}

	@Test
	public void testLevelBits(){
		long level = 4;
		String bits = prependZeros(Long.toBinaryString(level), 64);
		Assert.assertEquals(bits.indexOf("1"), 61);
		Assert.assertEquals(bits.lastIndexOf("1"), 61);
	}

	@Test
	public void testYBits(){
		long yval = 15, xval = 13, level = 4;
		String yBinaryString = "1111";
		Assert.assertEquals(Long.toBinaryString(yval), yBinaryString);

		long rightPartOfY = yval & DilatedBits.TABLE_MASK; // x and'd with 16 1's
		Assert.assertEquals(rightPartOfY, 15);
		long dilatedRightPartOfY = DilatedBits.LEFT_SHIFTED_DILATED_TABLE[(int)rightPartOfY];
		String dilatedLeftShiftedRightPartOfYBinaryString = "10101010";
		Assert.assertEquals(Long.toBinaryString(dilatedRightPartOfY), dilatedLeftShiftedRightPartOfYBinaryString);

		long leftPartOfY = yval >>> 16;
		Assert.assertEquals(0, leftPartOfY);
		long dilatedLeftPartOfY = DilatedBits.LEFT_SHIFTED_DILATED_TABLE[(int)leftPartOfY];
		Assert.assertEquals(dilatedLeftPartOfY, 0);

		// now put the same values in a quad and see if it behaves
		Quad quad0 = new Quad(level, xval, yval);
		Assert.assertEquals(quad0.getLevel(), 4);
		Assert.assertEquals(quad0.getX(), 13);
		Assert.assertEquals(quad0.getY(), 15);

		String aboveQuadAsMicrosoftString = "3323";
		Quad quad1 = new Quad(aboveQuadAsMicrosoftString);
		Assert.assertEquals(quad1.getLevel(), 4);
		Assert.assertEquals(quad1.getX(), 13);
		Assert.assertEquals(quad1.getY(), 15);
	}

	@Test
	public void testLongZero(){
		String str = Quad.SIXTEEN_ZERO_STRING + "000000";
		Quad sqaud = new Quad(str);
		Assert.assertEquals(str.length(), sqaud.getLevel());
		Assert.assertEquals(sqaud.getX(), 0);
		Assert.assertEquals(sqaud.getY(), 0);
	}

	@Test
	public void testLongOnes(){
		String str = "1111111111111111111";
		Quad squad = new Quad(str);
		Assert.assertEquals(str.length(), squad.getLevel());
		Assert.assertEquals(squad.getX(), (1L << str.length()) - 1);
		Assert.assertEquals(squad.getY(), 0);
	}

	@Test
	public void testFullTwos(){
		String str = "22222222222222222222222222222";
		Quad quad = new Quad(str);
		Assert.assertEquals(str.length(), quad.getLevel());
		Assert.assertEquals(0, quad.getX());
		Assert.assertEquals((1L << str.length()) - 1, quad.getY());
	}

	@Test
	public void testFullThrees(){
		String str = "33333333333333333333333333333";
		Quad quad = new Quad(str);
		Assert.assertEquals(str.length(), quad.getLevel());
		Assert.assertEquals(quad.getX(), (1L << str.length()) - 1);
		Assert.assertEquals(quad.getY(), (1L << str.length()) - 1);
	}

	@Test
	public void testComplexQuad(){
		String str = "0320100322312132010";
		int level = str.length();
		Assert.assertEquals(level, 19);
		Quad quad = new Quad(str);
		Assert.assertEquals(quad.getX(), 149938);
		Assert.assertEquals(quad.getY(), 200536);
		Assert.assertEquals(quad.getLevel(), level);
	}

	@Test
	public void testComplexQuad2(){
		String str = "032021232302132110303333201";
		int level = str.length();
		Quad quad = new Quad(str);
		Assert.assertEquals(quad.getX(), 36334969);
		Assert.assertEquals(quad.getY(), 56537468);
		Assert.assertEquals(quad.getLevel(), level);
		Assert.assertEquals(quad.getLevel(), str.length());
		Assert.assertEquals(quad.toMicrosoftStyleString(), str);
	}

	@Test
	public void testComplexQuad3(){
		String s0 = "03201002330";
		String s1 = "03201002331";
		String sp = "0320100233";
		Quad q0 = new Quad(s0);
		Quad q1 = new Quad(s1);
		Quad qp = new Quad(sp);
		Assert.assertTrue(q0.bits() != q1.bits());
		Assert.assertTrue(qp.bits() != q1.bits());
		Assert.assertTrue(qp.bits() != q0.bits());
		Assert.assertNotEquals(q1, q0);
		Assert.assertNotEquals(q0, qp);
		Assert.assertNotEquals(q1, qp);
	}

	@Test
	public void testComplexQuad4(){
		String s0 = "03200300000000000000000";
		Quad q0 = new Quad(s0);
		Quad qc = q0.getLastPossibleSubQuad();
		Assert.assertEquals(qc.toString(), "03200300000000000000000333333");
	}

	@Test
	public void testComplexQuad5(){
		String s0 = "03201003220303101300";
		Quad quad = new Quad(s0);
		Quad roundTripped = new Quad(quad.getLevel(), quad.getX(), quad.getY());
		Assert.assertEquals(roundTripped.getLevel(), quad.getLevel());
		Assert.assertEquals(roundTripped.getX(), quad.getX());
		Assert.assertEquals(roundTripped.getY(), quad.getY());
		Assert.assertEquals(roundTripped, quad);
	}

	@Test
	public void testMultipleComplexQuads(){
		List<String> quadStrings = List.of(
				"032023112222022003200100200",
				"032023112222022113120000333",
				"032023112222022133123310132",
				"032023112222030002032330312");
		List<Quad> quads = Quad.fromMicrosoftStyleStrings(quadStrings);
		for(Quad quad : quads){
			Quad newQuad = new Quad(quad.getLevel(), quad.getX(), quad.getY());
			Assert.assertEquals(newQuad.getLevel(), quad.getLevel());
			Assert.assertEquals(newQuad.getX(), quad.getX());
			Assert.assertEquals(newQuad.getY(), quad.getY());
			Assert.assertEquals(newQuad.toMicrosoftStyleString(), quad.toMicrosoftStyleString());
		}
	}

	@Test
	public void testMicrosoftStyleString(){
		String str = "0320100322312132010";// --0,032000000000330332220210113,218188,032002333132010030030121100 -
											// 03200300000000000000000000
		Quad squad = new Quad(str);
		Assert.assertEquals(squad.toMicrosoftStyleString(), str);
	}

	@Test
	public void testGetGridBits(){
		String s0 = "3323";
		String s1 = "032000";
		Quad q0 = new Quad(s0);
		Quad q1 = new Quad(4L, 13, 15);
		Quad q2 = new Quad(s1);
		Assert.assertEquals(Long.toBinaryString(q0.getGridBits()), "11111011");
		Assert.assertEquals(Long.toBinaryString(q1.getGridBits()), "11111011");
		Assert.assertEquals(Long.toBinaryString(q2.getGridBits()), "1110000000");
	}

	@Test
	public void testNestingChild(){
		Quad parent = new Quad("032021230201121202");
		Quad notAChild = new Quad("032023112222213202320112132");
		Quad child = new Quad("03202123020112120229");
		Assert.assertTrue(child.isChildOfOrEqualTo(parent));
		Assert.assertFalse(parent.isChildOfOrEqualTo(notAChild));
		Assert.assertFalse(parent.isChildOfOrEqualTo(child));
		Assert.assertTrue(parent.isChildOfOrEqualTo(parent));
	}

	@Test
	public void testNestingParent(){
		Quad parent = new Quad("032021230201121202");
		Quad notAChild = new Quad("032023112222213202320112132");
		Quad child = new Quad("03202123020112120229");
		Assert.assertFalse(parent.isParentOfOrEqualTo(notAChild));
		Assert.assertTrue(parent.isParentOfOrEqualTo(child));
		Assert.assertTrue(parent.isParentOfOrEqualTo(parent));
	}

	@Test
	public void testRange(){
		Quad q0 = new Quad("031");
		Quad q1 = new Quad("0312");
		String twentyFiveThrees = "3333333333333333333333333";
		String twentySixThrees = "33333333333333333333333333";
		Assert.assertEquals(q0.getLastPossibleSubQuad().toMicrosoftStyleString(), "031" + twentySixThrees);
		Assert.assertEquals(q1.getLastPossibleSubQuad().toMicrosoftStyleString(), "0312" + twentyFiveThrees);
	}

	@Test
	public void testGetSiblings(){
		Quad q1 = new Quad("031");
		Quad[] siblings = q1.get3Siblings().toArray(new Quad[]{});
		Assert.assertEquals(siblings, new Quad[]{new Quad("030"), new Quad("032"), new Quad("033")});
	}

	@Test
	public void testGetChildren(){
		Quad q0 = new Quad("");
		Quad q1 = new Quad("031");
		Quad[] children = q0.getChildren().toArray(new Quad[]{});
		Quad[] children1 = q1.getChildren().toArray(new Quad[]{});
		Assert.assertEquals(children, new Quad[]{new Quad("0"), new Quad("1"), new Quad("2"), new Quad("3")});
		Assert.assertEquals(children1, new Quad[]{new Quad("0310"), new Quad("0311"), new Quad("0312"),
				new Quad("0313")});
	}

	@Test
	public void testGetNumQuadsAcross(){
		Assert.assertEquals(new Quad("031").getNumQuadsAcross(), 8);
		Assert.assertEquals(new Quad("0").getNumQuadsAcross(), 2);
		Assert.assertEquals(new Quad("q").getNumQuadsAcross(), 1);
		Assert.assertEquals(new Quad("00223133211200332121").getNumQuadsAcross(), 1024 * 1024);
	}

	@Test
	public void testShiftLeft(){
		Assert.assertEquals(new Quad("001").shiftLeft(), new Quad("000"));
		Assert.assertEquals(new Quad("030").shiftLeft(), new Quad("021"));
		Assert.assertEquals(new Quad("300").shiftLeft(), new Quad("211"));
		Assert.assertEquals(new Quad("1022").shiftLeft(), new Quad("0133"));
	}

	@Test
	public void testShiftRight(){
		Assert.assertEquals(new Quad("000").shiftRight(), new Quad("001"));
		Assert.assertEquals(new Quad("021").shiftRight(), new Quad("030"));
		Assert.assertEquals(new Quad("211").shiftRight(), new Quad("300"));
		Assert.assertEquals(new Quad("0133").shiftRight(), new Quad("1022"));
	}

	@Test
	public void testShiftUp(){
		Assert.assertEquals(new Quad("002").shiftUp(), new Quad("000"));
		Assert.assertEquals(new Quad("021").shiftUp(), new Quad("003"));
		Assert.assertEquals(new Quad("300").shiftUp(), new Quad("122"));
		Assert.assertEquals(new Quad("2111").shiftUp(), new Quad("0333"));
	}

	@Test
	public void testShiftDown(){
		Assert.assertEquals(new Quad("000").shiftDown(), new Quad("002"));
		Assert.assertEquals(new Quad("003").shiftDown(), new Quad("021"));
		Assert.assertEquals(new Quad("122").shiftDown(), new Quad("300"));
		Assert.assertEquals(new Quad("0333").shiftDown(), new Quad("2111"));
	}

	@Test
	public void testGetAtLevel(){
		String s1 = "2123";
		Quad squad = new Quad(s1);

		Assert.assertEquals(squad.getAtLevel(2), new Quad("21"));
		Assert.assertEquals(squad.getAtLevel(5), new Quad("21230"));
		Assert.assertEquals(squad.getAtLevel(29), new Quad("21230000000000000000000000000"));

		Quad q1 = new Quad("03133333");
		Assert.assertEquals(q1.getLevel(), 8);
		Quad q2 = q1.getAtLevel(1);
		Quad q3 = q1.getAtLevel(3);
		Quad q12 = q1.getAtLevel(12);
		Assert.assertEquals(q2.toMicrosoftStyleString(), "0");
		Assert.assertEquals(q3.toMicrosoftStyleString(), "031");
		Assert.assertEquals(q12.toMicrosoftStyleString(), "031333330000");
	}

	@Test
	public void testGetParent(){
		String s1 = "0123";
		String s2 = "0123123";
		Quad squad0 = new Quad(s1);
		Quad squad1 = new Quad(s2);

		Assert.assertEquals(squad0.getParent().toMicrosoftStyleString(), "012");
		Assert.assertEquals(squad1.getParent().toMicrosoftStyleString(), "012312");

		Assert.assertEquals(squad0.getParent(), new Quad("012"));
		Assert.assertEquals(squad1.getParent(), new Quad("012312"));
	}

	@Test
	public void testIsXyEven(){
		Quad q1 = new Quad("000");
		Quad q2 = new Quad("003");
		Quad q3 = new Quad("122");
		Quad q4 = new Quad("0331");
		Assert.assertTrue(q1.isXEven());
		Assert.assertFalse(q2.isXEven());
		Assert.assertTrue(q3.isXEven());
		Assert.assertFalse(q4.isXEven());

		Assert.assertTrue(q1.isYEven());
		Assert.assertFalse(q2.isYEven());
		Assert.assertFalse(q3.isYEven());
		Assert.assertTrue(q4.isYEven());
	}

	@Test
	public void testCompareTo(){
		Quad q0 = new Quad("0320");
		Quad q1 = new Quad("032");
		Assert.assertTrue(q1.compareTo(q0) < 0);

		Quad q2 = new Quad("33100");
		Quad q3 = new Quad("3310000");
		Assert.assertTrue(q2.compareTo(q3) < 0);
	}

	@Test
	public void testReplaceQWithEmptyStringQuad(){
		Quad squad = new Quad("q");
		Assert.assertEquals(squad.toMicrosoftStyleString(), "");
	}

	@Test
	public void testGetLastPossibleSubQuadFromStringQuad(){
		String s1 = "11323";
		Assert.assertEquals(Quad.getLastPossibleSubQuadFromString(s1), "11323333333333333333333333333");
	}

	@Test
	public void testGetDescendantAtLevel(){
		Quad squad = new Quad("13102");
		Assert.assertEquals(squad.getDescendantAtLevel(7).size(), 16);
	}

	@Test
	public void testGetLastPossibleSubQuadAtLevel(){
		Quad quad = new Quad("012");
		Assert.assertEquals(quad.getAtLevel(27), new Quad("012000000000000000000000000"));
		Assert.assertEquals(quad.getLastPossibleSubQuadAtLevel(25), new Quad("0123333333333333333333333"));
		Assert.assertEquals(quad.getLastPossibleSubQuadAtLevel(26), new Quad("01233333333333333333333333"));
		Assert.assertEquals(quad.getLastPossibleSubQuadAtLevel(27), new Quad("012333333333333333333333333"));
		Assert.assertEquals(quad.getLastPossibleSubQuadAtLevel(28), new Quad("0123333333333333333333333333"));
		Assert.assertEquals(quad.getLastPossibleSubQuadAtLevel(29), new Quad("01233333333333333333333333333"));
		Assert.assertEquals(quad.getLastPossibleSubQuadAtLevel(23), new Quad("01233333333333333333333"));
	}

	private static String prependZeros(String input, int paddedLength){
		int paddingLength = paddedLength - input.length();
		var sb = new StringBuilder();
		for(int i = 0; i < paddingLength; ++i){
			sb.append('0');
		}
		sb.append(input);
		return sb.toString();
	}

	/*------------ DilatedBits ------------*/

	@Test
	public void test(){
		int lsdtnum = 10, dtnum = 5;
		Assert.assertEquals(DilatedBits.LEFT_SHIFTED_DILATED_TABLE[3], lsdtnum);
		Assert.assertEquals(DilatedBits.DILATED_TABLE[3], dtnum);
	}

	@Test
	public void test2(){
		int lsdtnum = 136, dtnum = 68;
		Assert.assertEquals(DilatedBits.LEFT_SHIFTED_DILATED_TABLE[10], lsdtnum);
		Assert.assertEquals(DilatedBits.DILATED_TABLE[10], dtnum);
	}

	@Test
	public void testRightXBits(){
		long number = 13;
		String numberBinaryString = "1101";
		Assert.assertEquals(Long.toBinaryString(number), numberBinaryString);

		long rightPartOfX = number & DilatedBits.TABLE_MASK; // x and'd with 14 1's
		Assert.assertEquals(rightPartOfX, 13);
		long dilatedRightPartOfX = DilatedBits.DILATED_TABLE[(int)rightPartOfX];
		String dilatedRightPartOfXBinaryString = "1010001"; // omit leading 0
		Assert.assertEquals(Long.toBinaryString(dilatedRightPartOfX), dilatedRightPartOfXBinaryString);

		long leftPartOfX = number >>> 14;
		Assert.assertEquals(leftPartOfX, 0);
		long dilatedLeftPartOfX = DilatedBits.DILATED_TABLE[(int)leftPartOfX];
		Assert.assertEquals(dilatedLeftPartOfX, 0);
	}

	@Test
	public void testLeftXBits(){
		long number = 1048583L;
		String numberBinaryString = "1" + prependZeros("", 17) + "111";
		Assert.assertEquals(Long.toBinaryString(number), numberBinaryString);

		long rightPartOfX = number & DilatedBits.TABLE_MASK; // x and'd with 14 1's
		Assert.assertEquals(rightPartOfX, 7);
		long dilatedRightPartOfX = DilatedBits.DILATED_TABLE[(int)rightPartOfX];
		String dilatedRightPartOfXBinaryString = "10101"; // omit leading 0
		Assert.assertEquals(Long.toBinaryString(dilatedRightPartOfX), dilatedRightPartOfXBinaryString);

		long leftPartOfX = number >>> 14;
		Assert.assertEquals(leftPartOfX, 64);
		long dilatedLeftPartOfX = DilatedBits.DILATED_TABLE[(int)leftPartOfX];
		Assert.assertEquals(dilatedLeftPartOfX, 4096);
	}

}
