package com.hotpads.datarouter.client.imp.mysql.ddl.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.util.core.collections.Range;

public class Base64RangeGenerator{
	private static final String[] UTF8_BIN_AND_UTF8MB4_BIN_ORDER = {"-", "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "_", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
			"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

	private static final String[] UTF8MB4_UNICODE_CI_ORDER = {"_", "-", "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "A", "a", "B", "b", "C", "c", "D", "d", "E", "e", "F", "f", "G", "g", "H", "h", "I", "i", "J", "j",
			"K", "k", "L", "l", "M", "m", "N", "n", "O", "o", "P", "p", "Q", "q", "R", "r", "S", "s", "T", "t", "U",
			"u", "V", "v", "W", "w", "X", "x", "Y", "y", "Z", "z"};

	private static final String[] LATIN1_SWEDISH_CI_ORDER = {"-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a",
			"A", "B", "b", "c", "C", "d", "D", "e", "E", "f", "F", "g", "G", "H", "h", "i", "I", "J", "j", "k", "K",
			"L", "l", "m", "M", "n", "N", "o", "O", "P", "p", "Q", "q", "r", "R", "S", "s", "t", "T", "u", "U", "V",
			"v", "w", "W", "X", "x", "Y", "y", "z", "Z", "_"};

	/**
	 * Generates Ranges to equally partition all possible Base64 values
	 * Known charset + collation combinations are utf8 + utf8_bin, utf8mb4 + utf8mb4_bin, utf8mb4 + utf8mb4_unicode_ci,
	 * and latin1 + latin1_swedish_ci.
	 * @param depth length of prefixes to use in the generated keys
	 * @return the Iterable will contain 64^depth ranges, with the first starting at null, and the last ending at null
	 */
	public static Iterable<Range<String>> generateRangesOfDepth(int depth, MySqlCharacterSet charset,
			MySqlCollation collation){
		String[] starts;
		switch(charset.name() + collation.name()){
		case "utf8utf8_bin":
		case "utf8mb4utf8mb4_bin":
			starts = generateRangeStarts(depth, UTF8_BIN_AND_UTF8MB4_BIN_ORDER);
			break;
		case "utf8mb4utf8mb4_unicode_ci":
			starts = generateRangeStarts(depth, UTF8MB4_UNICODE_CI_ORDER);
			break;
		case "latin1latin1_swedish_ci":
			starts = generateRangeStarts(depth, LATIN1_SWEDISH_CI_ORDER);
			break;
		default:
			throw new RuntimeException("Unknown charset, collation combination: " + charset + ", " + collation);
		}
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
		ranges.add(new Range<String>(null, starts[1]));
		for(int i = 1; i < starts.length - 1; i++){
			ranges.add(new Range<String>(starts[i], starts[i + 1]));
		}
		ranges.add(new Range<String>(starts[starts.length - 1]));//from last start to null
		return ranges;
	}

	public class Tests{
		@Test
		public void testBaseCase(){
			Assert.assertEquals(generateRangeStarts(0, UTF8_BIN_AND_UTF8MB4_BIN_ORDER), UTF8_BIN_AND_UTF8MB4_BIN_ORDER);
			Assert.assertEquals(generateRangeStarts(1, UTF8_BIN_AND_UTF8MB4_BIN_ORDER), UTF8_BIN_AND_UTF8MB4_BIN_ORDER);
			Assert.assertNotEquals(generateRangeStarts(2, UTF8_BIN_AND_UTF8MB4_BIN_ORDER),
					UTF8_BIN_AND_UTF8MB4_BIN_ORDER);

			Assert.assertEquals(generateRangeStarts(0, UTF8MB4_UNICODE_CI_ORDER), UTF8MB4_UNICODE_CI_ORDER);
			Assert.assertEquals(generateRangeStarts(1, UTF8MB4_UNICODE_CI_ORDER), UTF8MB4_UNICODE_CI_ORDER);
			Assert.assertNotEquals(generateRangeStarts(2, UTF8MB4_UNICODE_CI_ORDER), UTF8MB4_UNICODE_CI_ORDER);

			Assert.assertEquals(generateRangeStarts(0, LATIN1_SWEDISH_CI_ORDER), LATIN1_SWEDISH_CI_ORDER);
			Assert.assertEquals(generateRangeStarts(1, LATIN1_SWEDISH_CI_ORDER), LATIN1_SWEDISH_CI_ORDER);
			Assert.assertNotEquals(generateRangeStarts(2, LATIN1_SWEDISH_CI_ORDER), LATIN1_SWEDISH_CI_ORDER);
		}

		@Test
		public void testUtf8BinAndUtf8mb4BinOrderDepth2(){
			String expectedFirst = "--";
			String expectedSecond = "-0";
			String expectedCaseOrderTestPosition11 = "-A";
			String expectedCaseOrderTestPosition12 = "-B";
			String expectedLast = "zz";

			String[] rangeStarts = generateRangeStarts(2, UTF8_BIN_AND_UTF8MB4_BIN_ORDER);
			Assert.assertEquals(rangeStarts[0], expectedFirst);
			Assert.assertEquals(rangeStarts[1], expectedSecond);
			Assert.assertEquals(rangeStarts[11], expectedCaseOrderTestPosition11);
			Assert.assertEquals(rangeStarts[12], expectedCaseOrderTestPosition12);
			Assert.assertEquals(rangeStarts[rangeStarts.length - 1], expectedLast);
		}

		@Test
		public void testUtf8mb4UnicodeCiOrderDepth2(){
			String expectedFirst = "__";
			String expectedSecond = "_-";
			String expectedThird = "_0";
			String expectedCaseOrderTestPosition12 = "_A";
			String expectedCaseOrderTestPosition13 = "_a";
			String expectedLast = "zz";

			String[] rangeStarts = generateRangeStarts(2, UTF8MB4_UNICODE_CI_ORDER);
			Assert.assertEquals(rangeStarts[0], expectedFirst);
			Assert.assertEquals(rangeStarts[1], expectedSecond);
			Assert.assertEquals(rangeStarts[2], expectedThird);
			Assert.assertEquals(rangeStarts[12], expectedCaseOrderTestPosition12);
			Assert.assertEquals(rangeStarts[13], expectedCaseOrderTestPosition13);
			Assert.assertEquals(rangeStarts[rangeStarts.length - 1], expectedLast);
		}

		@Test
		public void testLatin1SwedishCiOrderDepth2(){
			String expectedFirst = "--";
			String expectedSecond = "-0";
			String expectedCaseOrderTestPosition11 = "-a";
			String expectedCaseOrderTestPosition12 = "-A";
			String expectedCaseOrderTestPosition13 = "-B";
			String expectedCaseOrderTestPosition14 = "-b";
			String expectedSecondToLast = "_Z";
			String expectedLast = "__";

			String[] rangeStarts = generateRangeStarts(2, LATIN1_SWEDISH_CI_ORDER);
			Assert.assertEquals(rangeStarts[0], expectedFirst);
			Assert.assertEquals(rangeStarts[1], expectedSecond);
			Assert.assertEquals(rangeStarts[11], expectedCaseOrderTestPosition11);
			Assert.assertEquals(rangeStarts[12], expectedCaseOrderTestPosition12);
			Assert.assertEquals(rangeStarts[13], expectedCaseOrderTestPosition13);
			Assert.assertEquals(rangeStarts[14], expectedCaseOrderTestPosition14);
			Assert.assertEquals(rangeStarts[rangeStarts.length - 2], expectedSecondToLast);
			Assert.assertEquals(rangeStarts[rangeStarts.length - 1], expectedLast);
		}

		@Test
		public void testArgParsing(){
			Assert.assertEquals(generateRangesOfDepth(0, MySqlCharacterSet.utf8, MySqlCollation.utf8_bin),
					rangify(UTF8_BIN_AND_UTF8MB4_BIN_ORDER));
			Assert.assertEquals(generateRangesOfDepth(0, MySqlCharacterSet.utf8mb4, MySqlCollation.utf8mb4_bin),
					rangify(UTF8_BIN_AND_UTF8MB4_BIN_ORDER));
			Assert.assertEquals(generateRangesOfDepth(0, MySqlCharacterSet.utf8mb4, MySqlCollation.utf8mb4_unicode_ci),
					rangify(UTF8MB4_UNICODE_CI_ORDER));
			Assert.assertEquals(generateRangesOfDepth(0, MySqlCharacterSet.latin1, MySqlCollation.latin1_swedish_ci),
					rangify(LATIN1_SWEDISH_CI_ORDER));
			try{
				Assert.assertEquals(generateRangesOfDepth(0,MySqlCharacterSet.utf8,MySqlCollation.latin1_swedish_ci),
						rangify(UTF8_BIN_AND_UTF8MB4_BIN_ORDER));
				Assert.fail();
			}catch(RuntimeException e){
				Assert.assertEquals(e.getMessage(), "Unknown charset, collation combination: utf8, latin1_swedish_ci");
			}
		}

		@Test
		public void testRangify(){
			String[] testIn = {"0", "1", "2", "3"};
			List<Range<String>> expected = Arrays.asList(new Range<String>(null, "1"), new Range<String>("1","2"),
					new Range<String>("2", "3"), new Range<String>("3", null));
			Assert.assertEquals(rangify(testIn).iterator(), expected.iterator());
		}
	}
}
