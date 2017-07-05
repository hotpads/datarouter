package io.datarouter.util.bytes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import io.datarouter.util.string.StringTool;

import org.testng.Assert;

public class StringByteTool{

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

		private String euroFromUsAscii = new String(new byte[]{ByteTool.fromUnsignedInt0To255(128), ByteTool
				.fromUnsignedInt0To255(128)}, StandardCharsets.US_ASCII);

		private String euroFromLatin1 = new String(new byte[]{ByteTool.fromUnsignedInt0To255(128)},
				StandardCharsets.ISO_8859_1);

		private Character unknownCharacter = euroFromUsAscii.charAt(0);
		private Integer unknownCharacterInt = (int)unknownCharacter.charValue();

		private String euro = euroFromLatin1;
		private byte[] euroUsAscii = getByteArray(euro, StandardCharsets.US_ASCII);
		private byte[] euroUtf16 = getByteArray(euro, StandardCharsets.UTF_16BE);
		private byte[] euroUtf8 = getByteArray(euro, StandardCharsets.UTF_8);

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
				String ascii = new String(new byte[]{ByteTool.fromUnsignedInt0To255(i)}, StandardCharsets.US_ASCII);

				String latin1 = new String(new byte[]{ByteTool.fromUnsignedInt0To255(i)},
						StandardCharsets.ISO_8859_1);

				String windows1252 = new String(new byte[]{ByteTool.fromUnsignedInt0To255(i)}, Charset.forName(
						"windows-1252"));

				String utf16be = new String(new byte[]{(byte)0, ByteTool.fromUnsignedInt0To255(i)},
						StandardCharsets.UTF_16BE);

				String utf8 = new String(new byte[]{ByteTool.fromUnsignedInt0To255(i)}, StandardCharsets.UTF_8);

				if(i < 0x80){
					Assert.assertEquals(latin1, ascii);
					Assert.assertEquals(windows1252, latin1);
					Assert.assertEquals(utf16be, windows1252);
					Assert.assertEquals(utf8, utf16be);
				}else if(i < 160){
					Assert.assertEquals(unknownCharacter.toString(), ascii);// invalid octet
					Assert.assertEquals(latin1.charAt(0), i);// valid octet, but not not mapped to any character
					Assert.assertTrue(StringTool.notEmpty(windows1252));
					Assert.assertTrue(latin1.equals(utf16be));
					Assert.assertTrue(!windows1252.equals(utf16be));
					Assert.assertEquals(unknownCharacter.toString(), utf8);// utf8 will expect 2 bytes here, so our 1
																			// byte is junk
				}else{
					Assert.assertEquals(unknownCharacter.toString(), ascii);// invalid octet
					Assert.assertTrue(StringTool.notEmpty(latin1));
					Assert.assertTrue(StringTool.notEmpty(windows1252));
					Assert.assertEquals(windows1252, latin1);
					Assert.assertEquals(utf16be, windows1252);
					Assert.assertEquals(unknownCharacter.toString(), utf8);// utf8 will expect 2 bytes here, so our 1
																			// byte is junk
				}
			}
		}

	}

}
