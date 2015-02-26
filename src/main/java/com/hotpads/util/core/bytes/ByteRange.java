package com.hotpads.util.core.bytes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;

/**
 * lightweight, reusable class for specifying ranges of byte[]'s
 * 
 * can contain convenience methods for comparing, printing, cloning,
 * spawning new arrays, copying to other arrays, etc.
 *  *
 */
public class ByteRange
implements Comparable<ByteRange>{

	/********************** fields *****************************/
	
	//not making these final.  intention is to reuse objects of this class
	private byte[] bytes;
	private int offset;
	private int length;
	private int hash = 0;
	
	
	/********************** constructors ***********************/
	
	public ByteRange(byte b){
		set(b);
	}
	
	public ByteRange(byte[] bytes){
		set(bytes);
	}
	
	public ByteRange(byte[] bytes, int offset, int length){
		set(bytes, offset, length);
	}
	
	
	/********************** methods *************************/

	public ByteRange set(byte b){
		this.bytes = new byte[]{ b };
		this.offset = 0;
		this.length = 1;
		calculateHashCode();
		return this;
	}

	public ByteRange set(byte[] bytes){
		return set(bytes, 0, bytes.length);
	}

	public ByteRange set(byte[] bytes, int offset, int length){
		if(bytes==null){ throw new NullPointerException("ByteRange does not support null bytes"); }
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
		calculateHashCode();
		return this;
	}
	
	public byte getByte(int innerOffset){
		return bytes[offset + innerOffset];
	}

	public byte[] copyToNewArray(){
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return result;
	}
	
	public byte[] copyToArrayNewArrayAndIncrement(){
		return DrByteTool.unsignedIncrement(toArray());
	}
	
	public ByteRange cloneAndIncrement(){
		return new ByteRange(copyToArrayNewArrayAndIncrement());
	}
	
	public boolean isFullArray(){
		return offset==0 && length==bytes.length;
	}
	
	public byte[] toArray(){
		return isFullArray() ? bytes : copyToNewArray();
	}

	public ByteRange cloneWithNewBackingArray(){
		// TODO copy the hash over as well since it will be the same
		return new ByteRange(copyToNewArray());
	}
	
	public ByteBuffer getNewByteBuffer(){
		return ByteBuffer.wrap(bytes, offset, length);
	}
	
	/******************** static methods ******************/
	
	public static byte[] nullSafeToArray(ByteRange in, boolean emptyArrayForNull){
		if(in != null){ return in.toArray(); }
		return emptyArrayForNull ? new byte[0] : null;
	}
	
	
	/******************* standard methods *********************/

	@Override
	public boolean equals(Object thatObject){
		if(this == thatObject){ return true; }
		if(hashCode() != thatObject.hashCode()){ return false; }
		if(!(thatObject instanceof ByteRange)){ return false; }
		ByteRange that = (ByteRange)thatObject;
		return DrByteTool.equals(bytes, offset, length, that.bytes, that.offset, that.length);
	}

	@Override
	public int hashCode(){
		return hash;
	}

	protected void calculateHashCode(){
		if(DrArrayTool.isEmpty(bytes)){
			hash = 0;
			return;
		}
		int off = offset;
		for(int i = 0; i < length; i++){
			hash = 31 * hash + bytes[off++];
		}
	}

	@Override
	public int compareTo(ByteRange other){
		return DrByteTool.bitwiseCompare(bytes, offset, length, 
				other.bytes, other.offset, other.length);
	}
	
	
	/********************* static *****************************/
	
	public static ArrayList<byte[]> copyToNewArrays(Collection<ByteRange> ranges){
		if(ranges==null){ return new ArrayList<byte[]>(0); }
		ArrayList<byte[]> arrays = Lists.newArrayListWithCapacity(ranges.size());
		for(ByteRange range : ranges){
			arrays.add(range.copyToNewArray());
		}
		return arrays;
	}
	
	public static ArrayList<ByteRange> fromArrays(Collection<byte[]> arrays){
		if(arrays==null){ return new ArrayList<ByteRange>(0); }
		ArrayList<ByteRange> ranges = Lists.newArrayListWithCapacity(arrays.size());
		for(byte[] array : arrays){
			ranges.add(new ByteRange(array));
		}
		return ranges;
	}

	
	/******************** getters *****************************/
	
	public byte[] getBytes(){
		return bytes;
	}

	public int getOffset(){
		return offset;
	}

	public int getLength(){
		return length;
	}
	
	@Override
	public String toString(){
		return Arrays.toString(bytes);
	}
}
