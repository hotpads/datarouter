package com.hotpads.util.core.number;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

public class VarInt{

	private final VarLong varLong;

	public VarInt(int value){
		this(new VarLong(value));
	}

	private VarInt(VarLong varLong){
		this.varLong = varLong;
	}

	public int getValue(){
		return (int) varLong.getValue();
	}

	public int getNumBytes(){
		return varLong.getNumBytes();
	}

	public byte[] getBytes(){
		return varLong.getBytes();
	}

	//factories

	public static VarInt fromByteArray(byte[] bytes){
		return new VarInt(VarLong.fromByteArray(bytes));
	}

	public static VarInt fromByteArray(byte[] bytes, int offset){
		return new VarInt(VarLong.fromByteArray(bytes, offset));
	}

	public static VarInt fromInputStream(InputStream is) throws IOException{
		return new VarInt(VarLong.fromInputStream(is));
	}

	public static VarInt fromReadableByteChannel(ReadableByteChannel fs) throws IOException{
		return new VarInt(VarLong.fromReadableByteChannel(fs));
	}

}
