package com.hotpads.util.core.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Base64RangeGenerator{
	private static final String[] CASE_SENSITIVE_ORDER = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g",
			"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1",
			"2", "3", "4", "5", "6", "7", "8", "9", "-", "_"};

	private static final String[] CASE_INSENSITIVE_ORDER = {"A", "a", "B", "b", "C", "c", "D", "d", "E", "e", "F", "f",
			"G", "g", "H", "h", "I", "i", "J", "j", "K", "k", "L", "l", "M", "m", "N", "n", "O", "o", "P", "p", "Q",
			"q", "R", "r", "S", "s", "T", "t", "U", "u", "V", "v", "W", "w", "X", "x", "Y", "y", "Z", "z", "0", "1",
			"2", "3", "4", "5", "6", "7", "8", "9", "-", "_"};

	public static Iterable<Range<String>> generateRangesOfDepth(int depth){
		return generateRangesOfDepth(depth, true);
	}

	public static Iterable<Range<String>> generateRangesOfDepth(int depth, boolean caseSensitive){
		String[] starts = generateRangeStarts(depth, caseSensitive ? CASE_SENSITIVE_ORDER : CASE_INSENSITIVE_ORDER);
		return rangify(starts);
	}

	private static String[] generateRangeStarts(int depth, String[] order){
		if(depth < 2){
			return order;
		}

		String[] previousLevel = generateRangeStarts(depth - 1, order);
		String[] thisLevel = new String[previousLevel.length * order.length];
		int index = 0;
		for(int i = 0; i < order.length; i++){
			for(int j = 0; j < previousLevel.length; j++){
				thisLevel[index++] = order[i] + previousLevel[j];
			}
		}

		return thisLevel;
	}

	private static Iterable<Range<String>> rangify(String[] starts){
		List<Range<String>> ranges = new ArrayList<>();
		for(int i = 0; i < starts.length - 1; i++){
			ranges.add(new Range<String>(starts[i], starts[i + 1]));
		}
		ranges.add(new Range<String>(starts[starts.length - 1]));//from last start to null
		return ranges;
	}

	public class Tests{
		@Test
		public void testBaseCase(){
			Assert.assertEquals(generateRangeStarts(0, CASE_SENSITIVE_ORDER), CASE_SENSITIVE_ORDER);
			Assert.assertEquals(generateRangeStarts(1, CASE_SENSITIVE_ORDER), CASE_SENSITIVE_ORDER);
			Assert.assertNotEquals(generateRangeStarts(2, CASE_SENSITIVE_ORDER), CASE_SENSITIVE_ORDER);

			Assert.assertEquals(generateRangeStarts(0, CASE_INSENSITIVE_ORDER), CASE_INSENSITIVE_ORDER);
			Assert.assertEquals(generateRangeStarts(1, CASE_INSENSITIVE_ORDER), CASE_INSENSITIVE_ORDER);
			Assert.assertNotEquals(generateRangeStarts(2, CASE_INSENSITIVE_ORDER), CASE_INSENSITIVE_ORDER);
		}

		@Test
		public void testDepth2(){
			String[] sensitive = generateRangeStarts(2, CASE_SENSITIVE_ORDER);
			String[] insensitive = generateRangeStarts(2, CASE_INSENSITIVE_ORDER);
			Assert.assertEquals(sensitive.length, insensitive.length);

			String firstExpected = "AA";
			String secondSensitiveExpected = "AB";
			String secondInsensitiveExpected = "Aa";
			String lastExpected = "__";

			Assert.assertEquals(sensitive[0], firstExpected);
			Assert.assertEquals(insensitive[0], firstExpected);

			Assert.assertEquals(sensitive[1], secondSensitiveExpected);
			Assert.assertEquals(insensitive[1], secondInsensitiveExpected);

			Assert.assertEquals(sensitive[sensitive.length - 1], lastExpected);
			Assert.assertEquals(insensitive[insensitive.length - 1], lastExpected);
		}

		@Test
		public void testOverloading(){
			Assert.assertEquals(generateRangesOfDepth(0), generateRangesOfDepth(0, true));
		}

		@Test
		public void testRangify(){
			String[] testIn = {"0", "1", "2", "3"};
			List<Range<String>> expected = Arrays.asList(new Range<String>("0", "1"), new Range<String>("1","2"),
					new Range<String>("2", "3"), new Range<String>("3", null));
			Assert.assertEquals(rangify(testIn).iterator(), expected.iterator());
		}
	}
}
