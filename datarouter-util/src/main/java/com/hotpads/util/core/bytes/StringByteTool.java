package com.hotpads.util.core.bytes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class StringByteTool{

	/**
	 * @deprecated inline
	 */
	@Deprecated
	public static final Charset
			CHARSET_USASCII = StandardCharsets.US_ASCII,
			CHARSET_UTF8 = StandardCharsets.UTF_8;

	public static byte[] getByteArray(String str, Charset charset){
		if(str == null){
			return null;
		}
		return str.getBytes(charset);
	}

	public static int numUtf8Bytes(String str){
		return getUtf8Bytes(str).length;
	}

	public static byte[] getUtf8Bytes(String str){
		if(str == null){
			return null;
		}
		return str.getBytes(StandardCharsets.UTF_8);
	}

	public static int toUtf8Bytes(String str, byte[] destination, int offset){
		byte[] bytes = getUtf8Bytes(str);
		System.arraycopy(bytes, 0, destination, offset, bytes.length);
		return bytes.length;
	}

	public static String fromUtf8Bytes(byte[] bytes, int offset, int length){
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	public static String fromUtf8Bytes(byte[] bytes){
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static String fromUtf8BytesOffset(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	public static class Tests{

		private String letterA = "a";
		private byte[] letterAUsAscii = getByteArray(letterA, StandardCharsets.US_ASCII);
		private byte[] letterAUtf16 = getByteArray(letterA, StandardCharsets.UTF_16BE);
		private byte[] letterAUtf8 = getByteArray(letterA, StandardCharsets.UTF_8);

		private String euroFromUsAscii = new String(new byte[]{DrByteTool.fromUnsignedInt0To255(128), DrByteTool
				.fromUnsignedInt0To255(128)}, StandardCharsets.US_ASCII);

		private String euroFromLatin1 = new String(new byte[]{DrByteTool.fromUnsignedInt0To255(128)},
				StandardCharsets.ISO_8859_1);

		private Character unknownCharacter = euroFromUsAscii.charAt(0);
		private Integer unknownCharacterInt = (int)unknownCharacter.charValue();

		private String euro = euroFromLatin1;
		private byte[] euroUsAscii = getByteArray(euro, StandardCharsets.US_ASCII);
		private byte[] euroUtf16 = getByteArray(euro, StandardCharsets.UTF_16BE);
		private byte[] euroUtf8 = getByteArray(euro, StandardCharsets.UTF_8);

		@Test
		public void testUnicode(){
			Assert.assertEquals(2, letterAUtf16.length);
			Assert.assertEquals(1, letterAUtf8.length);
			Assert.assertEquals(1, letterAUsAscii.length);

			Assert.assertEquals(2, euroUtf16.length);
			Assert.assertEquals(2, euroUtf8.length);
			Assert.assertEquals(1, euroUsAscii.length);

			Assert.assertFalse(euroFromUsAscii.equals(euroFromLatin1));
		}

		@Test
		public void testAsciiExtensions(){
			Assert.assertTrue(unknownCharacterInt.equals(65533));

			for(int i = 0; i < 256; ++i){
				String ascii = new String(new byte[]{DrByteTool.fromUnsignedInt0To255(i)}, StandardCharsets.US_ASCII);

				String latin1 = new String(new byte[]{DrByteTool.fromUnsignedInt0To255(i)},
						StandardCharsets.ISO_8859_1);

				String windows1252 = new String(new byte[]{DrByteTool.fromUnsignedInt0To255(i)}, Charset.forName(
						"windows-1252"));

				String utf16be = new String(new byte[]{(byte)0, DrByteTool.fromUnsignedInt0To255(i)},
						StandardCharsets.UTF_16BE);

				String utf8 = new String(new byte[]{DrByteTool.fromUnsignedInt0To255(i)}, StandardCharsets.UTF_8);

				if(i < 0x80){
					Assert.assertEquals(ascii, latin1);
					Assert.assertEquals(latin1, windows1252);
					Assert.assertEquals(windows1252, utf16be);
					Assert.assertEquals(utf16be, utf8);
				}else if(i < 160){
					Assert.assertEquals(ascii, unknownCharacter.toString());// invalid octet
					Assert.assertEquals(i, latin1.charAt(0));// valid octet, but not not mapped to any character
					Assert.assertTrue(DrStringTool.notEmpty(windows1252));
					Assert.assertTrue(latin1.equals(utf16be));
					Assert.assertTrue(!windows1252.equals(utf16be));
					Assert.assertEquals(utf8, unknownCharacter.toString());// utf8 will expect 2 bytes here, so our 1
																			// byte is junk
				}else{
					Assert.assertEquals(ascii, unknownCharacter.toString());// invalid octet
					Assert.assertTrue(DrStringTool.notEmpty(latin1));
					Assert.assertTrue(DrStringTool.notEmpty(windows1252));
					Assert.assertEquals(latin1, windows1252);
					Assert.assertEquals(windows1252, utf16be);
					Assert.assertEquals(utf8, unknownCharacter.toString());// utf8 will expect 2 bytes here, so our 1
																			// byte is junk
				}
			}
		}

	}

}
