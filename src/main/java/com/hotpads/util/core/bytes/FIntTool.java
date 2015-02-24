package com.hotpads.util.core.bytes;

import java.io.OutputStream;

import com.hotpads.util.core.io.OutputStreamTool;

public class FIntTool{

	public static long maxValueForNumBytes(int numBytes){
		return (1L << (numBytes * 8)) - 1;
	}

	public static int numBytes(final long value){
		if(value == 0){ return 1; }// 0 doesn't work with the formula below
		return (64 + 7 - Long.numberOfLeadingZeros(value)) / 8;
	}

	public static byte[] getBytes(int outputWidth, final long value){
		byte[] bytes = new byte[outputWidth];
		writeBytes(outputWidth, value, bytes, 0);
		return bytes;
	}

	public static void writeBytes(int outputWidth, final long value, byte[] bytes, int offset){
		bytes[offset + outputWidth - 1] = (byte)value;
		for(int i = outputWidth - 2; i >= 0; --i){
			bytes[offset + i] = (byte)(value >>> (outputWidth - i - 1) * 8);
		}
	}

	static final long[] MASKS = new long[]{(long)255, (long)255 << 8, (long)255 << 16, (long)255 << 24,
			(long)255 << 32, (long)255 << 40, (long)255 << 48, (long)255 << 56};

	public static void writeBytes(int outputWidth, final long value, OutputStream os){
		for(int i = outputWidth - 1; i >= 0; --i){
			OutputStreamTool.write(os, (byte)((value & MASKS[i]) >>> (8 * i)));
		}
	}

	public static long fromBytes(final byte[] bytes){
		long value = 0;
		value |= bytes[0] & 0xff;// these seem to do ok without casting the byte to int
		for(int i = 1; i < bytes.length; ++i){
			value <<= 8;
			value |= bytes[i] & 0xff;
		}
		return value;
	}

	public static long fromBytes(final byte[] bytes, final int offset, final int width){
		long value = 0;
		value |= bytes[0 + offset] & 0xff;// these seem to do ok without casting the byte to int
		for(int i = 1; i < width; ++i){
			value <<= 8;
			value |= bytes[i + offset] & 0xff;
		}
		return value;
	}

}