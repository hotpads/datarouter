package com.hotpads.util.core.io;

import java.io.IOException;
import java.io.OutputStream;

import com.hotpads.util.core.bytes.ByteRange;

public class OutputStreamTool{
	
	public static void write(OutputStream os, byte b){
		try{
			os.write(b);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void write(OutputStream os, byte[] bytes){
		write(os, bytes, 0, bytes.length);
	}

	public static void write(OutputStream os, byte[] bytes, int offset, int length){
		try{
			os.write(bytes, offset, length);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void write(OutputStream os, ByteRange byteRange){
		write(os, byteRange.getBytes(), byteRange.getOffset(), byteRange.getLength());
	}

	public static void write(OutputStream os, ByteRange byteRange, int byteRangeInnerOffset){
		write(os, byteRange.getBytes(), byteRange.getOffset() + byteRangeInnerOffset, byteRange.getLength()
				- byteRangeInnerOffset);
	}
	
}
