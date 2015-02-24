package com.hotpads.util.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ArrayTool {
	
	public static <T> T getFirst(T[] ins){
		return isEmpty(ins) ? null : ins[0];
	}
	
	public static <T> T getLast(T[] ins){
		return isEmpty(ins) ? null : ins[ins.length - 1];
	}

	@SuppressWarnings("unchecked")
	public static <C extends Collection<T>, T> C 
	fillCollection(List<Object[]> results, int index, C resultCollection)
	{
		for(Object[] row : results){
			resultCollection.add((T)row[index]);
		}
		return resultCollection;
	}
	
	public static void copyWrapException(Object src, int srcPos, Object dest, int destPos, int length){
		try{
			System.arraycopy(src, srcPos, dest, destPos, length);
		}catch(RuntimeException e){
			throw new RuntimeException(e);
		}
	}
	
	public static short[] copyOfWrapException(short[] original, int length){
		try{
			return Arrays.copyOf(original, length);
		}catch(RuntimeException e){
			throw new RuntimeException(e);
		}
	}
	
	public static short[] copyOfRangeWrapException(short[] original, int from, int to){
		try{
			return Arrays.copyOfRange(original, from, to);
		}catch(RuntimeException e){
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] clone(byte[] in){
		if(in==null){ return null; }
		byte[] out = new byte[in.length];
		System.arraycopy(in, 0, out, 0, in.length);
		return out;
	}
	
	public static byte[] nullSafe(byte[] a){
		if(a==null){ return new byte[0]; }
		return a;
	}
	
	public static short[] nullSafe(short[] a){
		if(a==null){ return new short[0]; }
		return a;
	}

	public static int[] nullSafe(int[] a){
		if(a==null){ return new int[0]; }
		return a;
	}

	public static long[] nullSafe(long[] a){
		if(a==null){ return new long[0]; }
		return a;
	}

	public static char[] nullSafe(char[] a){
		if(a==null){ return new char[0]; }
		return a;
	}

	public static Object[] nullSafe(Object[] a){
		if(a==null){ return new Object[0]; }
		return a;
	}

	public static String[] nullSafe(String[] a){
		if(a==null){ return new String[0]; }
		return a;
	}
	
	public static int length(byte[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static int length(short[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static int length(int[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static int length(double[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static int length(long[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static int length(Object[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	
	//TODO remove the "nullSafe" from method names
	public static int nullSafeLength(short[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(int[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(long[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(char[] a){
		if(a==null){ return 0; }
		return a.length;
	}

	public static int nullSafeLength(Object[] a){
		if(a==null){ return 0; }
		return a.length;
	}
	
	public static boolean isEmpty(byte[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(short[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(int[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(double[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(long[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean isEmpty(Object[] a){
		if(a==null){ return true; }
		if(a.length==0){ return true; }
		return false;
	}
	
	public static boolean notEmpty(byte[] a){
		return ! isEmpty(a);
	}
	
	public static boolean notEmpty(short[] a){
		return ! isEmpty(a);
	}
	
	public static boolean notEmpty(Object[] a){
		return ! isEmpty(a);
	}
	
	public static void fillWithIndexes(short[] a){
		for(short i=0; i < a.length; ++i){
			a[i] = i;
		}
	}
	
	public static void fill(int[] a, int filler){
		for(int i=0; i < a.length; ++i){
			a[i] = filler;
		}
	}
	
	public static void moveValueToIndexZero(short[] uniqueArrayIncludingId, short id){
		int position = Arrays.binarySearch(uniqueArrayIncludingId, id);
		uniqueArrayIncludingId[position] = uniqueArrayIncludingId[0];
		uniqueArrayIncludingId[0] = id;
	}
	
	public static short[] reverse(short[] in){
		int length = nullSafeLength(in);
		if(length==0){ return in; }
		short[] out = new short[in.length];
		for(int i=0; i < length; ++i){
			out[length-i-1] = in[i];
		}
		return out;
	}

	public static int[] reverse(int[] in){
		int length = nullSafeLength(in);
		if(length==0){ return in; }
		int[] out = new int[in.length];
		for(int i=0; i < length; ++i){
			out[length-i-1] = in[i];
		}
		return out;
	}

	public static long[] reverse(long[] in){
		int length = nullSafeLength(in);
		if(length==0){ return in; }
		long[] out = new long[in.length];
		for(int i=0; i < length; ++i){
			out[length-i-1] = in[i];
		}
		return out;
	}
	
	public static short[] concatenate(short[]... arrays){
		int totalLength = 0;
		for(int i=0; i < nullSafeLength(arrays); ++i){
			totalLength += nullSafeLength(arrays[i]);
		}
		short[] result = new short[totalLength];
		int nextStartIndex = 0;
		for(int i=0; i < nullSafeLength(arrays); ++i){
			int argArrayLength = nullSafeLength(arrays[i]);
			if(argArrayLength > 0){
				System.arraycopy(arrays[i], 0, result, nextStartIndex, argArrayLength);
				nextStartIndex += argArrayLength;
			}
		}
		return result;
	}
	
	/*
	 * concatenate two arrays of bytes 
	 */
	public static byte[] concatenate(byte[] A, byte[] B) {
		   byte[] C= new byte[A.length+B.length];
		   System.arraycopy(A, 0, C, 0, A.length);
		   System.arraycopy(B, 0, C, A.length, B.length);
		   return C;
		}
	
	/*
	 * shuffle the order of elements from segmentStarts[i] inclusive to segmentStarts[i+1] not-inclusive
	 */
	public static short[] randomShiftEachSegment(short[] in, int[] segmentStarts){
		if(nullSafeLength(in) < 2){ return in; }
		if(segmentStarts==null){
			return randomShift(in);
		}
		short[] out = new short[in.length];
		for(int i=0; i < segmentStarts.length; ++i){
			int segmentEndExclusive = i < segmentStarts.length - 1 ? segmentStarts[i+1] : in.length;
			int segmentLength = segmentEndExclusive - segmentStarts[i];
			short[] oldSegment = Arrays.copyOfRange(in, segmentStarts[i], segmentEndExclusive);
			short[] newSegment = randomShift(oldSegment);
			System.arraycopy(newSegment, 0, out, segmentStarts[i], segmentLength);
		}
		return out;
	}
	
	public static short[] randomShift(short[] in){
		if(nullSafeLength(in) < 2){ return in; }
		int startAt = Math.abs((int)System.nanoTime() % in.length);
//		System.out.println(startAt);
		short[] out = new short[in.length];
		int nextOutIndex = 0;
		for(int i=startAt; i < in.length; ++i){
			out[nextOutIndex++] = in[i];
		}
		for(int i=0; i < startAt; ++i){
			out[nextOutIndex++] = in[i];
		}
		return out;
	}
	
	public static int[] randomShift(int[] in){
		if(nullSafeLength(in) < 2){ return in; }
		int startAt = Math.abs((int)System.nanoTime() % in.length);
//		System.out.println(startAt);
		int[] out = new int[in.length];
		int nextOutIndex = 0;
		for(int i=startAt; i < in.length; ++i){
			out[nextOutIndex++] = in[i];
		}
		for(int i=0; i < startAt; ++i){
			out[nextOutIndex++] = in[i];
		}
		return out;
	}
	
	//good idea for a method... turned out mcorgan didn't need it
//	public static short[] fuseSortedArrays(Collection<short[]> arrays, boolean keepDuplicates){
//		
//	}
	
	
	//see Arrays.copyOf
//	public static short[] subArray(short[] in, int length){
//		if(in==null){
//			in = new short[length];
//		}
//		short[] out = new short[length];
//		int numToCopy = in.length < out.length ? in.length : out.length;
//		System.arraycopy(in, 0, out, 0, numToCopy);
//		return out;
//	}
	
	public static boolean containsUnsorted(byte[] a, byte key){
		if(isEmpty(a)){ return false; }
		for(int i=0; i < a.length; ++i){
			if(a[i]==key){ return true; }
		}
		return false;
	}
	
	public static boolean containsUnsorted(int[] a, Integer key){
		if(key==null){ return false; }
		if(isEmpty(a)){ return false; }
		for(int i=0; i < a.length; ++i){
			if(a[i]==key){ return true; }
		}
		return false;
	}
	
	public static short last(short[] a){
		return a[a.length-1];
	}
	
	public static int numInstancesOf(short[] array, short elementToCount){
		int count = 0;
		for(int i=0; i < array.length; ++i){
			if(array[i]==elementToCount){ ++count; }
		}
		return count;
	}
	
	public static int numNonNull(Object[] array){
		if(array==null){ return 0; }
		int count = 0;
		for(int i=0; i < array.length; ++i){
			if(array[i] != null){ ++count; }
		}
		return count;
	}
	
	public static int numNonNegative(short[] array){
		if(array==null){ return 0; }
		int count = 0;
		for(int i=0; i < array.length; ++i){
			if(array[i]>=0){ ++count; }
		}
		return count;
	}
	
	public static int numNonNegative(int[] array){
		if(array==null){ return 0; }
		int count = 0;
		for(int i=0; i < array.length; ++i){
			if(array[i]>=0){ ++count; }
		}
		return count;
	}
	
	public static int numPositive(short[] array){
		if(array==null){ return 0; }
		int count = 0;
		for(int i=0; i < array.length; ++i){
			if(array[i]>0){ ++count; }
		}
		return count;
	}
	
	public static int numPositive(int[] array){
		if(array==null){ return 0; }
		int count = 0;
		for(int i=0; i < array.length; ++i){
			if(array[i]>0){ ++count; }
		}
		return count;
	}
	
	public static short[] castIntsToShorts(int[] in){
		short[] out = new short[in.length];
		for(int i=0; i < in.length; ++i){
			out[i] = (short)in[i];
		}
		return out;
	}
	
	public static int[] castShortsToInts(short[] in){
		int[] out = new int[in.length];
		for(int i=0; i < in.length; ++i){
			out[i] = (int)in[i];
		}
		return out;
	}
	
	public static short[] resizeIfNecessary(short[] in, int length){
		if(in==null && length==0){ return new short[0]; }
		if(in.length == 0){ return in; }
		short[] out = new short[length];
		ArrayTool.copyWrapException(in, 0, out, 0, length);
		return out;
	}
	
	public static int[] getPositiveIndices(short[] a){
		if(a==null){ return new int[0]; }
		int numPositive = 0;
		for(int i=0; i < a.length; ++i){
			if(a[i] >= 0){ ++numPositive; }
		}
		int[] indices = new int[numPositive];
		int resultSpot = 0;
		for(int i=0; i < a.length; ++i){
			if(a[i] >= 0){ indices[resultSpot++] = i; }
		}
		return indices;
	}

	
	public static String toCsvString(byte[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}

	
	public static String toCsvString(short[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}

	
	public static String toCsvString(int[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}

	
	public static String toCsvString(long[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}

	
	public static String toCsvString(double[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
	public static String toCsvString(Object[] a){
		if(a==null){ return ""; }
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < a.length; ++i){
			if(i>0){ sb.append(","); }
			sb.append(a[i].toString());
		}
		return sb.toString();
	}
	
	public static short[] primitiveShortArray(Collection<Short> ins){
		if(CollectionTool.isEmpty(ins)){ return new short[0]; }
		short[] array = new short[ins.size()];
		int index = 0;
		for(Short s : ins){
			array[index++] = s;
		}
		return array;
	}
	
	public static int[] primitiveIntArray(Collection<Integer> ins){
		if(CollectionTool.isEmpty(ins)){ return new int[0]; }
		int[] array = new int[ins.size()];
		int index = 0;
		for(Integer i : ins){
			array[index++] = i;
		}
		return array;
	}
	
	public static long[] primitiveLongArray(Collection<Long> ins){
		if(CollectionTool.isEmpty(ins)){ return new long[0]; }
		long[] array = new long[ins.size()];
		int index = 0;
		for(long i : ins){
			array[index++] = i;
		}
		return array;
	}
	
	public static boolean isSorted(short... a){
		if(isEmpty(a) || a.length==1){ return true; }
		short max = a[0];
		for(int i=1; i < a.length; ++i){
			if(a[i] < max){ return false; }
			max = a[i];
		}
		return true;
	}

	//same as {@link CollectionTool}
	public static int firstInsertionPoint(Comparable[] sortedList, Object key){
		if(isEmpty(sortedList)){ return 0; }
		int p = Arrays.binarySearch(sortedList, key);
		if(p >= 0){
			p = firstEqualElementInSortedList(sortedList, p);
			return p;
		}else{
			p = (-1*p)-1;
			return p;
		}
	}
	
	//same as {@link CollectionTool}
	public static int lastInsertionPoint(Comparable[] sortedList, Object key){
		if(isEmpty(sortedList)){ return 0; }
		int p = Arrays.binarySearch(sortedList, key);
		if(p >= 0){
			p = 1 + lastEqualElementInSortedList(sortedList, p);
			return p;
		}else{
			p = (-1*p)-1;
			return p;
		}
	}

	//same as {@link CollectionTool}
	public static int firstEqualElementInSortedList(Comparable[] sortedList, int index){
		while(true){
			if(index-1 < 0 || ! sortedList[index].equals(sortedList[index-1])){
				return index;
			}
			index--;
		}
	}
	
	public static int firstEqualElementInSortedList(long[] sortedList, int index){
		while(true){
			if(index-1 < 0 || sortedList[index] != (sortedList[index-1])){
				return index;
			}
			index--;
		}
	}
	
	public static int firstEqualElementInSortedList(double[] sortedList, int index){
		while(true){
			if(index-1 < 0 || sortedList[index] != (sortedList[index-1])){
				return index;
			}
			index--;
		}
	}
	
	public static int firstEqualElementInSortedList(int[] sortedList, int index){
		while(true){
			if(index-1 < 0 || sortedList[index] != (sortedList[index-1])){
				return index;
			}
			index--;
		}
	}
	
	public static int firstEqualElementInSortedList(short[] sortedList, int index){
		while(true){
			if(index-1 < 0 || sortedList[index] != (sortedList[index-1])){
				return index;
			}
			index--;
		}
	}
	
	public static int firstEqualElementInSortedList(byte[] sortedList, int index){
		while(true){
			if(index-1 < 0 || sortedList[index] != (sortedList[index-1])){
				return index;
			}
			index--;
		}
	}

	//same as {@link CollectionTool}
	public static int lastEqualElementInSortedList(Comparable[] sortedList, int index){
		while(true){
			if(index+1 >= sortedList.length || ! sortedList[index].equals(sortedList[index+1])){
				return index;
			}
			index++;
		}
	}

	public static int lastEqualElementInSortedList(long[] sortedList, int index){
		while(true){
			if(index+1 >= sortedList.length || sortedList[index] != (sortedList[index+1])){
				return index;
			}
			index++;
		}
	}

	public static int lastEqualElementInSortedList(double[] sortedList, int index){
		while(true){
			if(index+1 >= sortedList.length || sortedList[index] != (sortedList[index+1])){
				return index;
			}
			index++;
		}
	}

	public static int lastEqualElementInSortedList(int[] sortedList, int index){
		while(true){
			if(index+1 >= sortedList.length || sortedList[index] != (sortedList[index+1])){
				return index;
			}
			index++;
		}
	}

	public static int lastEqualElementInSortedList(short[] sortedList, int index){
		while(true){
			if(index+1 >= sortedList.length || sortedList[index] != (sortedList[index+1])){
				return index;
			}
			index++;
		}
	}

	public static int lastEqualElementInSortedList(byte[] sortedList, int index){
		while(true){
			if(index+1 >= sortedList.length || sortedList[index] != (sortedList[index+1])){
				return index;
			}
			index++;
		}
	}
	
	public static void copyInto(byte[] destination, byte[] source, int offset) {
		for(int i = 0; i < source.length; i++){
			try{
				destination[offset + i] = source[i];
			}
			catch(ArrayIndexOutOfBoundsException e){
				throw new ArrayIndexOutOfBoundsException("array1 + offset is shorter than array2");
			}
		}
	}

	 public static class Tests{
		 @Test public void testFirstEqualElementInSortedList(){
			 Integer[] d = new Integer[]{0,1,1,1,2,3,4,5,8};
			 Assert.assertEquals(0, ArrayTool.firstEqualElementInSortedList(d, 0));
			 Assert.assertEquals(1, ArrayTool.firstEqualElementInSortedList(d, 1));
			 Assert.assertEquals(1, ArrayTool.firstEqualElementInSortedList(d, 2));
			 Assert.assertEquals(1, ArrayTool.firstEqualElementInSortedList(d, 3));
			 Assert.assertEquals(4, ArrayTool.firstEqualElementInSortedList(d, 4));
			 Assert.assertEquals(8, ArrayTool.firstEqualElementInSortedList(d, 8));
		 }
		 @Test public void testLastEqualElementInSortedList(){
			 Integer[] d = new Integer[]{0,1,1,1,2,3,4,5,8};
			 Assert.assertEquals(0, ArrayTool.lastEqualElementInSortedList(d, 0));
			 Assert.assertEquals(3, ArrayTool.lastEqualElementInSortedList(d, 1));
			 Assert.assertEquals(3, ArrayTool.lastEqualElementInSortedList(d, 2));
			 Assert.assertEquals(3, ArrayTool.lastEqualElementInSortedList(d, 3));
			 Assert.assertEquals(4, ArrayTool.lastEqualElementInSortedList(d, 4));
			 Assert.assertEquals(8, ArrayTool.lastEqualElementInSortedList(d, 8));
		 }
		 @Test public void testFirstInsertionPoint(){
			 Integer[] d = new Integer[]{0,1,1,1,2,3,4,5,8};
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(d, -13));
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(d, 0));
			 Assert.assertEquals(1, ArrayTool.firstInsertionPoint(d, 1));
			 Assert.assertEquals(7, ArrayTool.firstInsertionPoint(d, 5));
			 Assert.assertEquals(8, ArrayTool.firstInsertionPoint(d, 6));
			 Assert.assertEquals(8, ArrayTool.firstInsertionPoint(d, 8));
			 Assert.assertEquals(9, ArrayTool.firstInsertionPoint(d, 10));
		 }
		 @Test public void testLastInsertionPoint(){
			 Integer[] d = new Integer[]{0,1,1,1,2,3,4,5,8};
			 Assert.assertEquals(0, ArrayTool.lastInsertionPoint(d, -13));
			 Assert.assertEquals(1, ArrayTool.lastInsertionPoint(d, 0));
			 Assert.assertEquals(4, ArrayTool.lastInsertionPoint(d, 1));
			 Assert.assertEquals(8, ArrayTool.lastInsertionPoint(d, 5));
			 Assert.assertEquals(8, ArrayTool.lastInsertionPoint(d, 6));
			 Assert.assertEquals(9, ArrayTool.lastInsertionPoint(d, 8));
			 Assert.assertEquals(9, ArrayTool.lastInsertionPoint(d, 10));
		 }
		 @Test public void testFirstInsertionPointWithDoubles(){
			 Double[] empty = new Double[]{};
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(empty, 3.2));
			 Double[] d = new Double[]{0.1, 3.4, 6.9, 110.1, 150.5, 160.6, 1540.0};
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(d, -500.0));
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(d, 0.05));
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(d, 0.1));
			 Assert.assertEquals(2, ArrayTool.firstInsertionPoint(d, 5.0));
			 Assert.assertEquals(2, ArrayTool.firstInsertionPoint(d, 6.9));
			 Assert.assertEquals(6, ArrayTool.firstInsertionPoint(d, 1540.0));
			 Assert.assertEquals(7, ArrayTool.firstInsertionPoint(d, 999999.9));
			 Double[] d2 = new Double[]{3.3, 3.3, 3.3};
			 Assert.assertEquals(0, firstEqualElementInSortedList(d2, 1));
			 Assert.assertEquals(0, ArrayTool.firstInsertionPoint(d2, 3.3));
		 }
		 @Test public void testLastInsertionPointWithDoubles(){
			 Double[] d = new Double[]{0.1, 3.4, 6.9, 110.1, 150.5, 160.6, 1540.0};
			 Assert.assertEquals(0, ArrayTool.lastInsertionPoint(d, -500.0));
			 Assert.assertEquals(0, ArrayTool.lastInsertionPoint(d, 0.05));
			 Assert.assertEquals(1, ArrayTool.lastInsertionPoint(d, 0.1));
			 Assert.assertEquals(2, ArrayTool.lastInsertionPoint(d, 5.0));
			 Assert.assertEquals(3, ArrayTool.lastInsertionPoint(d, 6.9));
			 Assert.assertEquals(7, ArrayTool.lastInsertionPoint(d, 1540.0));
			 Assert.assertEquals(7, ArrayTool.lastInsertionPoint(d, 999999.9));
			 Double[] d2 = new Double[]{3.3, 3.3, 3.3};
			 Assert.assertEquals(2, lastEqualElementInSortedList(d2, 1));
			 Assert.assertEquals(3, ArrayTool.lastInsertionPoint(d2, 3.3));
		 }
		 @Test public void simpleCompare(){
			 Double one = 1.0;
			 Double two = 2.0;
			 Assert.assertEquals(0, one.compareTo(one));
			 Assert.assertEquals(-1, one.compareTo(two));
			 Assert.assertEquals(1, two.compareTo(one));
		 }
		 @Test public void testIsSorted(){
			 Assert.assertTrue(isSorted(new short[]{0,0,3,4,5,6}));
			 Assert.assertTrue(isSorted(new short[]{-1000,3}));
			 Assert.assertFalse(isSorted(new short[]{-8,-9}));
		 }
		 @Test public void testMoveValueToIndexZero(){
			 short[] a1 = new short[]{0,3,4,5,6};
			 moveValueToIndexZero(a1, (short)6);
			 Assert.assertArrayEquals(new short[]{6,3,4,5,0}, a1);
		 }
		 @Test public void testConcatenate(){
			 short[] a1 = new short[]{0,3,4,5,6};
			 short[] a2 = null;
			 short[] a3 = new short[]{};
			 short[] a4 = new short[]{87,99,99,99,87};
			 short[] a5 = null;
			 short[] a6 = new short[]{};
			 Assert.assertArrayEquals(new short[]{0,3,4,5,6,87,99,99,99,87}, concatenate(a1,a2,a3,a4,a5,a6));
		 }
		 @Test public void testReverse(){
			 short[] a1 = new short[]{0,3,4,5,6};
			 short[] reversed = reverse(a1);
			 Assert.assertArrayEquals(new short[]{6,5,4,3,0}, reversed);
		 }
		 @Test public void testRandomShift(){//just make sure it doesn't throw exceptions
			 randomShift(new short[]{});
			 randomShift(new short[]{1});
			 
			 short[] a1 = new short[]{0,3,4,5,6};
			 randomShift(a1);
		 }
		 @Test public void testRandomShiftEachSegment(){//just make sure it doesn't throw exceptions
			 randomShiftEachSegment(null, null);
			 randomShiftEachSegment(new short[]{}, null);
			 randomShiftEachSegment(new short[]{1}, null);
			 
			 short[] a1 = new short[]{0,3,4,5,6};
			 randomShiftEachSegment(a1,null);
			 randomShiftEachSegment(a1,new int[]{0});
			 randomShiftEachSegment(a1,new int[]{0,2,3});
			 randomShiftEachSegment(a1,new int[]{0,2,3});
		 }
	 }


}
















