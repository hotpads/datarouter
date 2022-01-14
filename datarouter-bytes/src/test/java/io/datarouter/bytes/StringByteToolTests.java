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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StringByteToolTests{

	private String letterA = "a";
	private byte[] letterAUsAscii = letterA.getBytes(StandardCharsets.US_ASCII);
	private byte[] letterAUtf16 = letterA.getBytes(StandardCharsets.UTF_16BE);
	private byte[] letterAUtf8 = letterA.getBytes(StandardCharsets.UTF_8);

	private String euroFromUsAscii = new String(new byte[]{(byte)128, (byte)128}, StandardCharsets.US_ASCII);

	private String euroFromLatin1 = new String(new byte[]{(byte)128},
			StandardCharsets.ISO_8859_1);

	private Character unknownCharacter = euroFromUsAscii.charAt(0);
	private Integer unknownCharacterInt = (int)unknownCharacter.charValue();

	private String euro = euroFromLatin1;
	private byte[] euroUsAscii = euro.getBytes(StandardCharsets.US_ASCII);
	private byte[] euroUtf16 = euro.getBytes(StandardCharsets.UTF_16BE);
	private byte[] euroUtf8 = euro.getBytes(StandardCharsets.UTF_8);

	@Test
	public void testUnicode(){
		Assert.assertEquals(letterAUtf16.length, 2);
		Assert.assertEquals(letterAUtf8.length, 1);
		Assert.assertEquals(letterAUsAscii.length, 1);

		Assert.assertEquals(euroUtf16.length, 2);
		Assert.assertEquals(euroUtf8.length, 2);
		Assert.assertEquals(euroUsAscii.length, 1);

		Assert.assertFalse(euroFromUsAscii.equals(euroFromLatin1));
	}

	@Test
	public void testAsciiExtensions(){
		Assert.assertTrue(unknownCharacterInt.equals(65533));

		for(int i = 0; i < 256; ++i){
			String ascii = new String(new byte[]{(byte)i}, StandardCharsets.US_ASCII);

			String latin1 = new String(new byte[]{(byte)i},
					StandardCharsets.ISO_8859_1);

			String windows1252 = new String(new byte[]{(byte)i}, Charset.forName(
					"windows-1252"));

			String utf16be = new String(new byte[]{(byte)0, (byte)i},
					StandardCharsets.UTF_16BE);

			String utf8 = new String(new byte[]{(byte)i}, StandardCharsets.UTF_8);

			if(i < 0x80){
				Assert.assertEquals(latin1, ascii);
				Assert.assertEquals(windows1252, latin1);
				Assert.assertEquals(utf16be, windows1252);
				Assert.assertEquals(utf8, utf16be);
			}else if(i < 160){
				Assert.assertEquals(unknownCharacter.toString(), ascii);// invalid octet
				Assert.assertEquals(latin1.charAt(0), i);// valid octet, but not not mapped to any character
				Assert.assertTrue(windows1252.length() > 0);
				Assert.assertTrue(latin1.equals(utf16be));
				Assert.assertTrue(!windows1252.equals(utf16be));
				Assert.assertEquals(unknownCharacter.toString(), utf8);// utf8 will expect 2 bytes here, so our 1
																		// byte is junk
			}else{
				Assert.assertEquals(unknownCharacter.toString(), ascii);// invalid octet
				Assert.assertTrue(latin1.length() > 0);
				Assert.assertTrue(windows1252.length() > 0);
				Assert.assertEquals(windows1252, latin1);
				Assert.assertEquals(utf16be, windows1252);
				Assert.assertEquals(unknownCharacter.toString(), utf8);// utf8 will expect 2 bytes here, so our 1
																		// byte is junk
			}
		}
	}

}
