package com.hotpads.util.core.bytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hotpads.util.core.io.OutputStreamTool;

public class VLongTool{

	public static final byte BYTE_7_RIGHT_BITS_SET = 127, BYTE_LEFT_BIT_SET = -128;

	public static final long LONG_7_RIGHT_BITS_SET = 127, LONG_8TH_BIT_SET = 128;

	public static final byte[] MAX_VALUE_BYTES = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, 127};

	/********************* int -> bytes **************************/

	public static int numBytes(long in){// do a check for illegal arguments if not protected
		if(in == 0){ return 1; }// doesn't work with the formula below
		return (70 - Long.numberOfLeadingZeros(in)) / 7;// 70 comes from 64+(7-1)
	}

	public static byte[] getBytes(long value){
		int numBytes = numBytes(value);
		byte[] bytes = new byte[numBytes];
		long remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			bytes[i] = (byte)((remainder & LONG_7_RIGHT_BITS_SET) | LONG_8TH_BIT_SET);// set the left bit
			remainder >>= 7;
		}
		bytes[numBytes - 1] = (byte)(remainder & LONG_7_RIGHT_BITS_SET);// do not set the left bit
		return bytes;
	}

	public static int writeBytes(long value, OutputStream os){
		int numBytes = numBytes(value);
		long remainder = value;
		for(int i = 0; i < numBytes - 1; ++i){
			// set the left bit
			OutputStreamTool.write(os, (byte)((remainder & LONG_7_RIGHT_BITS_SET) | LONG_8TH_BIT_SET));
			remainder >>= 7;
		}
		// do not set the left bit
		OutputStreamTool.write(os, (byte)(remainder & LONG_7_RIGHT_BITS_SET));
		return numBytes;
	}

	/******************** bytes -> int **************************/
	
	public static int getInt(byte[] bytes){
		return (int)getLong(bytes, 0);
	}

	public static long getLong(byte[] bytes){
		return getLong(bytes, 0);
	}

	public static int getInt(byte[] bytes, int offset){
		return (int)getLong(bytes, offset);
	}
	
	public static long getLong(byte[] bytes, int offset){
		long value = 0;
		for(int i = 0;; ++i){
			byte b = bytes[offset + i];
			long shifted = BYTE_7_RIGHT_BITS_SET & b;// kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			if(b >= 0){
				break;
			}// first bit was 0, so that's the last byte in the VarLong
		}
		return value;
	}

	public static long getLong(InputStream is) throws IOException{
		long value = 0;
		int i = 0;
		int b;
		do{
			b = is.read();
			long shifted = BYTE_7_RIGHT_BITS_SET & b;// kill leftmost bit
			shifted <<= 7 * i;
			value |= shifted;
			++i;
		}while(b > Byte.MAX_VALUE);
		return value;
	}
}