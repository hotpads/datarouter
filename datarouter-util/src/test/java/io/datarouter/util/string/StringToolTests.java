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
package io.datarouter.util.string;

import java.util.List;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;

public class StringToolTests{

	@Test
	public void testGetStringSurroundedWith(){
		Assert.assertEquals(StringTool.getStringSurroundedWith("|wee||", "|", "|"), "wee");
		Assert.assertEquals(StringTool.getStringSurroundedWith("][x][", "[", "]"), "x");
		Assert.assertEquals(StringTool.getStringSurroundedWith("<span>a name</span>", "<span>", "</span>"), "a name");
		Assert.assertEquals(StringTool.getStringSurroundedWith("<span>a name</span>", "elephant", "</span>"), "");

		Assert.assertEquals(StringTool.getStringSurroundedWith("[wrong][right][x]", "[", "][x]"), "right");
		Assert.assertEquals(StringTool.getStringSurroundedWith("|a|b|", "|", "|"), "a");
		Assert.assertEquals(StringTool.getStringSurroundedWith("|a|b||", "|", "||"), "b");
	}

	@Test
	public void testPad(){
		Assert.assertEquals(StringTool.pad("asdf", ' ', 8), "    asdf");
		Assert.assertEquals(StringTool.padEnd("fdsa", '_', 7), "fdsa___");
		Assert.assertEquals(StringTool.repeat('f', 10), "ffffffffff");
	}

	@Test
	public void testReplaceCharactersInRange(){
		Assert.assertEquals(StringTool.replaceCharactersInRange("01banana 2banana 3banana 4 56", '1', '4', '0'),
				"00banana 0banana 0banana 0 56");
	}

	@Test
	public void testSplitOnCharNoRegexWithEmptyStrings(){
		String input = "//";
		List<String> expected = Java9.listOf("", "", "");
		List<String> decoded = StringTool.splitOnCharNoRegex(input, '/');
		Assert.assertEquals(decoded, expected);
	}

	@Test
	public void testSplitOnCharNoRegex(){
		Assert.assertEquals(StringTool.splitOnCharNoRegex("", '/'), Java9.listOf(""));
		Assert.assertEquals(StringTool.splitOnCharNoRegex(null, '/'), Java9.listOf());
		Assert.assertEquals(StringTool.splitOnCharNoRegex("/", '/'), Java9.listOf("", ""));
		Assert.assertEquals(StringTool.splitOnCharNoRegex("  /", '/'), Java9.listOf("  ", ""));
		Assert.assertEquals(StringTool.splitOnCharNoRegex("abc.def.g", '.'), Java9.listOf("abc", "def", "g"));
		Assert.assertEquals(StringTool.splitOnCharNoRegex("..def.g.", '.'), Java9.listOf("", "", "def", "g", ""));
	}

	@Test
	public void testCaseInsensitive(){
		String aa = "dawgy";
		String bb = "dawGy";
		String cc = "dawGy";
		Assert.assertTrue(StringTool.equalsCaseInsensitive(aa, bb));
		Assert.assertFalse(Objects.equals(aa, bb));
		Assert.assertTrue(StringTool.equalsCaseInsensitiveButNotCaseSensitive(aa, bb));
		Assert.assertFalse(StringTool.equalsCaseInsensitiveButNotCaseSensitive(bb, cc));
	}

	@Test
	public void testContainsCaseInsensitive(){
		String baseS = "HelloHowDYhi";
		String ss1 = "howdy";
		String ss2 = "howDy";
		String ss3 = "HowDy";
		String ss4 = "Hola";
		Assert.assertTrue(StringTool.containsCaseInsensitive(baseS, ss1));
		Assert.assertTrue(StringTool.containsCaseInsensitive(baseS, ss2));
		Assert.assertTrue(StringTool.containsCaseInsensitive(baseS, ss3));
		Assert.assertFalse(StringTool.containsCaseInsensitive(baseS, ss4));
	}

	@Test
	public void testEnforceNumeric(){
		Assert.assertEquals(StringTool.enforceNumeric("-8.473.93"), "-8473.93");
		Assert.assertEquals(StringTool.enforceNumeric("8.473.93"), "8473.93");
		Assert.assertEquals(StringTool.enforceNumeric("8473.93"), "8473.93");
		Assert.assertEquals(StringTool.enforceNumeric("5"), "5");
		Assert.assertEquals(StringTool.enforceNumeric("ff5ff"), "5");
		Assert.assertEquals(StringTool.enforceNumeric("ff5%"), "5");
		Assert.assertEquals(StringTool.enforceNumeric("5%"), "5");
		Assert.assertEquals(StringTool.enforceNumeric("%5"), "5");
		Assert.assertEquals(StringTool.enforceNumeric("5."), "5");
		Assert.assertEquals(StringTool.enforceNumeric("5.0.0."), "50.0");
		Assert.assertEquals(StringTool.enforceNumeric("."), "");
		Assert.assertEquals(StringTool.enforceNumeric("ABC400,000DEF"), "400000");
		Assert.assertEquals(StringTool.enforceNumeric("-"), "");
		Assert.assertEquals(StringTool.enforceNumeric("555-555-5555"), "5555555555");
		Assert.assertEquals(StringTool.enforceNumeric("-555-555-5555"), "-5555555555");
	}

	@Test
	public void testRemoveNonStandardCharacters(){
		Assert.assertEquals(StringTool.removeNonStandardCharacters("abc\t\n123"), "abc\t\n123");
		Assert.assertEquals(StringTool.removeNonStandardCharacters("\u0000"), " ");
		Assert.assertEquals(StringTool.removeNonStandardCharacters("\u001B"), " ");
		Assert.assertEquals(StringTool.removeNonStandardCharacters("\u000B"), "\n");
	}

	@Test
	public void testGetStringBeforeFirstOccurrence(){
		Assert.assertEquals(StringTool.getStringBeforeFirstOccurrence('.', "v1.2"), "v1");
		Assert.assertEquals(StringTool.getStringBeforeFirstOccurrence("1.", "v1.2"), "v");
		Assert.assertEquals(StringTool.getStringBeforeFirstOccurrence('.', "v1"), "v1");
		Assert.assertEquals(StringTool.getStringBeforeFirstOccurrence(".", "v1"), "v1");
		Assert.assertEquals(StringTool.getStringBeforeFirstOccurrence('.', ""), "");
	}

	@Test
	public void testGetStringAfterLastOccurrence(){
		Assert.assertEquals(StringTool.getStringAfterLastOccurrence('/', "abcdefxyz"), "");
		Assert.assertEquals(StringTool.getStringAfterLastOccurrence('/', "abc/def/xyz"), "xyz");
		Assert.assertEquals(StringTool.getStringAfterLastOccurrence("/d", "abc/def/xyz"), "ef/xyz");
		Assert.assertEquals(StringTool.getStringAfterLastOccurrence("/z", "abc/def/xyz"), "");
	}

	@Test
	public void testGetStringBeforeLastOccurrence(){
		Assert.assertEquals(StringTool.getStringBeforeLastOccurrence('.', "abc/def.xyz.xml"), "abc/def.xyz");
		Assert.assertEquals(StringTool.getStringBeforeLastOccurrence("/d", "abc/def/xyz"), "abc");
		Assert.assertEquals(StringTool.getStringBeforeLastOccurrence("", null), null);
		Assert.assertEquals(StringTool.getStringBeforeLastOccurrence(".", "no_dot"), "");
	}

	@Test
	public void testEnforceAlphabetic(){
		Assert.assertEquals(StringTool.enforceAlphabetic("abc123"), "abc");
		Assert.assertEquals(StringTool.enforceAlphabetic("1abc123,"), "abc");
		Assert.assertNotEquals(StringTool.enforceAlphabetic("1ABC123,"), "abc");
		Assert.assertEquals(StringTool.enforceAlphabetic("1ABC123,"), "");
	}

	@Test
	public void testReplaceStart(){
		Assert.assertEquals(StringTool.replaceStart("something", "something", "something"), "something");
		Assert.assertEquals(StringTool.replaceStart("something", "some", "no"), "nothing");
		Assert.assertEquals(StringTool.replaceStart("something", "12", "yikes"), "something");
		Assert.assertEquals(StringTool.replaceStart("something", "thing", "yikes"), "something");
	}

	@Test
	public void testNumbers(){
		Assert.assertTrue(StringTool.containsNumbers("a1dkfjaldk"));
		Assert.assertFalse(StringTool.containsOnlyNumbers("a1dlkafj"));
		Assert.assertTrue(StringTool.containsOnlyNumbers("01234567890123412341352109472813740198715"));
	}

	@Test
	public void testGetSimpleClassName(){
		Assert.assertEquals(StringTool.getSimpleClassName("bar.Foo"), "Foo");
		Assert.assertEquals(StringTool.getSimpleClassName("Foo"), "Foo");
	}

	@Test
	public void testEscapeString(){
		String string = "bill's";
		Assert.assertEquals(StringTool.escapeString(string), "'bill\\'s'");
		string = "Renter\\\\\\'s Assurance Program";
		Assert.assertEquals(StringTool.escapeString(string), "'Renter\\\\\\\\\\\\\\'s Assurance Program'");
		string = "no apostrophes";
		Assert.assertEquals(StringTool.escapeString(string), "'no apostrophes'");
	}

	@Test
	public void testTrimToSizeFromEnd(){
		Assert.assertEquals(StringTool.trimToSizeFromEnd("abcd", 2), "cd");
		Assert.assertEquals(StringTool.trimToSizeFromEnd("abcd", 10), "abcd");
	}

}
