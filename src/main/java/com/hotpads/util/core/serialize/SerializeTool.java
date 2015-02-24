package com.hotpads.util.core.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeTool{

	public static byte[] serialize(Object o) {
		assert o != null;
		byte[] rv=null;
		try {
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			ObjectOutputStream os=new ObjectOutputStream(bos);
			os.writeObject(o);
			os.close();
			bos.close();
			rv=bos.toByteArray();
		} catch(IOException e) {
			throw new IllegalArgumentException("Non-serializable object", e);
		}
		return rv;
	}

	public static Object deserialize(byte[] in) {
		Object rv=null;
		assert in != null;
		try {
			ByteArrayInputStream bis=new ByteArrayInputStream(in);
			ObjectInputStream is=new ObjectInputStream(bis);
			rv=is.readObject();
			is.close();
			bis.close();
		} catch(IOException e) {
			throw new IllegalArgumentException("Caught IOException decoding " + in.length + " bytes of data", e);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Caught ClassNotFoundException decoding " + in.length + " bytes of data", e);
		}
		return rv;
	}
	

}
