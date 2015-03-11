package com.hotpads.util.core.bytes;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class StringByteTool{

	
	public static final Charset
		CHARSET_USASCII = Charset.forName("US-ASCII"),
		CHARSET_WINDOWS1252 = Charset.forName("windows-1252"),
		CHARSET_LATIN1 = Charset.forName("ISO-8859-1"),
		CHARSET_UTF8 = Charset.forName("UTF-8"),
		CHARSET_UTF16BE = Charset.forName("UTF-16BE");//java internal "char" representation.  Need to investigate BOM more
	
	

//	public static void appendUtf8Bytes(byte[] dest, int startIndex, String s){
//		if(s==null){ return; }
//		s.getBytes(0, s.length(), dest, startIndex);
//	}

	public static byte[] getByteArray(String s, Charset charset){
		if(s==null){ return null; }
		return s.getBytes(charset);
	}
	
	public static int numUtf8Bytes(String s){
		return getUtf8Bytes(s).length;
	}

	public static byte[] getUtf8Bytes(String s){
		if(s==null){ return null; }
		return s.getBytes(CHARSET_UTF8);
	}
	
	public static int toUtf8Bytes(String s, byte[] destination, int offset){
		byte[] bytes = getUtf8Bytes(s);
		System.arraycopy(bytes, 0, destination, offset, bytes.length);
		return bytes.length;
	}

	public static List<byte[]> getUtf8ByteArrays(List<String> strings){
		List<byte[]> byteArrays = DrListTool.createArrayListWithSize(strings);
		for(String s : DrIterableTool.nullSafe(strings)){
			byteArrays.add(getUtf8Bytes(s));
		}
		return byteArrays;
	}
	
	public static String fromUtf8Bytes(byte[] bytes){
		return new String(bytes, CHARSET_UTF8);
	}
	
	public static String fromUtf8BytesOffset(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, CHARSET_UTF8);
	}
	
	public static String fromUtf8Bytes(byte[] bytes, int offset, int length){
		return new String(bytes, offset, length, CHARSET_UTF8);
	}
	
	public static int getNumBytesInMemoryWithPointers(String s){
		return 2 * DrByteTool.BYTES_PER_POINTER //object overhead for string
				+ DrByteTool.BYTES_PER_INTEGER//hash
				+ DrByteTool.BYTES_PER_POINTER //for the pointer to the char[]??
				+ 3 * DrByteTool.BYTES_PER_POINTER //array overhead (object overhead + length)
				+ s.length() * 2; //actual char[] data
	}
	
	
	public static class Tests{
		
		String a = "a";
		byte[] aLatin1 = getByteArray(a, StringByteTool.CHARSET_LATIN1);
		byte[] aUsAscii = getByteArray(a, StringByteTool.CHARSET_USASCII);
		byte[] aUtf16 = getByteArray(a, StringByteTool.CHARSET_UTF16BE);
		byte[] aUtf8 = getByteArray(a, StringByteTool.CHARSET_UTF8);
		
		String euroFromUsAscii = new String(
				new byte[]{DrByteTool.fromUnsignedInt0To255(128),DrByteTool.fromUnsignedInt0To255(128)},
				StringByteTool.CHARSET_USASCII);
		
		String euroFromLatin1 = new String(
				new byte[]{DrByteTool.fromUnsignedInt0To255(128)},
				StringByteTool.CHARSET_LATIN1);
		
		String quarterSymbolFromWindows1252 = new String(
				new byte[]{DrByteTool.fromUnsignedInt0To255(128)},
				StringByteTool.CHARSET_WINDOWS1252);
		
		Character unknownCharacter = euroFromUsAscii.charAt(0);
		Integer unknownCharacterInt = (int)unknownCharacter.charValue();
				
		String b = euroFromLatin1;
		byte[] bLatin1 = getByteArray(b, StringByteTool.CHARSET_LATIN1);
		byte[] bUsAscii = getByteArray(b, StringByteTool.CHARSET_USASCII);
		byte[] bUtf16 = getByteArray(b, StringByteTool.CHARSET_UTF16BE);
		byte[] bUtf8 = getByteArray(b, StringByteTool.CHARSET_UTF8);
		
		@Test public void testUnicode(){
			Assert.assertEquals(2, aUtf16.length);
			Assert.assertEquals(1, aUtf8.length);
			Assert.assertEquals(1, aUsAscii.length);

			Assert.assertEquals(2, bUtf16.length);
			Assert.assertEquals(2, bUtf8.length);
			Assert.assertEquals(1, bUsAscii.length);
			
			Assert.assertFalse(euroFromUsAscii.equals(euroFromLatin1));
		}
		
		@Test public void testAsciiExtensions(){
			Assert.assertTrue(unknownCharacterInt.equals(65533));
			
			for(int i=0; i <256; ++i){
				String ascii = new String(
						new byte[]{DrByteTool.fromUnsignedInt0To255(i)},
						StringByteTool.CHARSET_USASCII);
				
				String latin1 = new String(
						new byte[]{DrByteTool.fromUnsignedInt0To255(i)},
						StringByteTool.CHARSET_LATIN1);

				String windows1252 = new String(
						new byte[]{DrByteTool.fromUnsignedInt0To255(i)},
						StringByteTool.CHARSET_WINDOWS1252);

				String utf16be = new String(
						new byte[]{(byte)0, DrByteTool.fromUnsignedInt0To255(i)},
						StringByteTool.CHARSET_UTF16BE);

				String utf8 = new String(
						new byte[]{DrByteTool.fromUnsignedInt0To255(i)},
						StringByteTool.CHARSET_UTF8);

//				System.out.print("\n"+StringTool.pad(""+i, ' ', 3));
//				System.out.print(" "+(int)ascii.charAt(0));
//				System.out.print(" "+(int)latin1.charAt(0));
//				System.out.print(" "+(int)windows1252.charAt(0));
//				System.out.print(" "+(int)utf16be.charAt(0));
//				System.out.print(" "+(int)utf8.charAt(0));
				
				if(i < 0x80){
					Assert.assertEquals(ascii, latin1);
					Assert.assertEquals(latin1, windows1252);
					Assert.assertEquals(windows1252, utf16be);
					Assert.assertEquals(utf16be, utf8);
				}else if(i < 160){
					Assert.assertEquals(ascii, unknownCharacter.toString());//invalid octet
					Assert.assertEquals(i, latin1.charAt(0));//valid octet, but not not mapped to any character
					Assert.assertTrue(DrStringTool.notEmpty(windows1252));
					Assert.assertTrue(latin1.equals(utf16be));
					Assert.assertTrue( ! windows1252.equals(utf16be));
					Assert.assertEquals(utf8, unknownCharacter.toString());//utf8 will expect 2 bytes here, so our 1 byte is junk
				}else{
					Assert.assertEquals(ascii, unknownCharacter.toString());//invalid octet
					Assert.assertTrue(DrStringTool.notEmpty(latin1));
					Assert.assertTrue(DrStringTool.notEmpty(windows1252));
					Assert.assertEquals(latin1, windows1252);
					Assert.assertEquals(windows1252, utf16be);
					Assert.assertEquals(utf8, unknownCharacter.toString());//utf8 will expect 2 bytes here, so our 1 byte is junk
				}
				
			}
			
		}
		
	}
	
	public static void main(String... args){
		for(Map.Entry<String,Charset> charsetByName : Charset.availableCharsets().entrySet()){
			System.out.println(charsetByName.getKey());
		}
		
		System.out.println("System Default:"+Charset.defaultCharset());
		
	}

}
