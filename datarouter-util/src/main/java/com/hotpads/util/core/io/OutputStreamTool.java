package com.hotpads.util.core.io;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamTool{

	public static void write(OutputStream os, byte octet){
		try{
			os.write(octet);
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

}
