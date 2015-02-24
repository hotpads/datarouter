package com.hotpads.util.core.collections.accumulator;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MathTool;

public class DoubleAccumulator implements Iterator<Double>, Iterable<Double>{
	
	/************************** static vars ********************************/
	
	public static final int DEFAULT_PAGE_SIZE = 1024;
	
	
	/************************** fields ****************************************/
	
	private int pageSize = DEFAULT_PAGE_SIZE;
	
	private List<double[]> pages = ListTool.createArrayList();

	private int size = 0;
	private int nextIndexInPage = 0;

	private int iteratorPosition = 0;
	
	
	/******************************* constructors ********************************/
	
	public DoubleAccumulator(int pageSize){
		this.pageSize = pageSize;
	}
	
	
	/************************** methods ****************************************/
	
	public DoubleAccumulator add(double d){
		if(nextIndexInPage == 0){
			pages.add(new double[pageSize]);
		}
		pages.get(pages.size() - 1)[nextIndexInPage] = d;
		++nextIndexInPage;
		if(nextIndexInPage == pageSize){
			nextIndexInPage = 0;
		}
		++size;
		return this;
	}
	
	public DoubleAccumulator addAll(DoubleAccumulator da){
		for(Double d : da){
			add(d);
		}
		return this;
	}
	
	
	public int size(){
		return size;
	}
	
	public double get(int index){
		int pageNum = index / pageSize;
		int pageOffset = index % pageSize;
		return pages.get(pageNum)[pageOffset];
	}
	
	public DoubleAccumulator set(int index, double value){
		int pageNum = index / pageSize;
		int pageOffset = index % pageSize;
		pages.get(pageNum)[pageOffset] = value;
		return this;
	}
	
	/**************************** iterator ***************************************/

	@Override
	public Iterator<Double> iterator(){
		iteratorPosition = 0;
		return this;
	}
	
	@Override
	public boolean hasNext() {
		return iteratorPosition < size;
	}

	@Override
	public Double next() {
		return get(iteratorPosition++);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Can't remove from an Accumulator");
	}
	
	
	/************************ standard *********************************/
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(double[] page : CollectionTool.nullSafe(pages)){
			sb.append("["+ArrayTool.toCsvString(page)+"]");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	/******************* sorting (copied from java.lang.Arrays ******************/
	
	public DoubleAccumulator sort(){
		sort1(this, 0, size);
		return this;
	}
	
    /**
     * Sorts the specified sub-array of doubles into ascending order.
     */
    private static void sort1(DoubleAccumulator x, int off, int len) {
	// Insertion sort on smallest arrays
	if (len < 7) {
	    for (int i=off; i<len+off; i++)
		for (int j=i; j>off && x.get(j-1)>x.get(j); j--)
		    swap(x, j, j-1);
	    return;
	}

	// Choose a partition element, v
	int m = off + (len >> 1);       // Small arrays, middle element
	if (len > 7) {
	    int l = off;
	    int n = off + len - 1;
	    if (len > 40) {        // Big arrays, pseudomedian of 9
		int s = len/8;
		l = med3(x, l,     l+s, l+2*s);
		m = med3(x, m-s,   m,   m+s);
		n = med3(x, n-2*s, n-s, n);
	    }
	    m = med3(x, l, m, n); // Mid-size, med of 3
	}
	double v = x.get(m);

	// Establish Invariant: v* (<v)* (>v)* v*
	int a = off, b = a, c = off + len - 1, d = c;
	while(true) {
	    while (b <= c && x.get(b) <= v) {
		if (x.get(b) == v)
		    swap(x, a++, b);
		b++;
	    }
	    while (c >= b && x.get(c) >= v) {
		if (x.get(c) == v)
		    swap(x, c, d--);
		c--;
	    }
	    if (b > c)
		break;
	    swap(x, b++, c--);
	}

	// Swap partition elements back to middle
	int s, n = off + len;
	s = Math.min(a-off, b-a  );  vecswap(x, off, b-s, s);
	s = Math.min(d-c,   n-d-1);  vecswap(x, b,   n-s, s);

	// Recursively sort non-partition-elements
	if ((s = b-a) > 1)
	    sort1(x, off, s);
	if ((s = d-c) > 1)
	    sort1(x, n-s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(DoubleAccumulator x, int a, int b) {
	double t = x.get(a);
	x.set(a, x.get(b));
	x.set(b, t);
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(DoubleAccumulator x, int a, int b, int n) {
	for (int i=0; i<n; i++, a++, b++)
	    swap(x, a, b);
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private static int med3(DoubleAccumulator x, int a, int b, int c) {
	return (x.get(a) < x.get(b) ?
		(x.get(b) < x.get(c) ? b : x.get(a) < x.get(c) ? c : a) :
		(x.get(b) > x.get(c) ? b : x.get(a) > x.get(c) ? c : a));
    }
    
    public static class Tests {
    	DoubleAccumulator da = new DoubleAccumulator(3)
		.add(0d).add(1d).add(2d)
		.add(3d).add(4d).add(5d)
		.add(6d).add(7d).add(8d)
		.add(9d).add(10d).add(11d)
		.add(12d).add(13d).add(14d)
		.add(15d).add(16d);
		
		
		@Test public void testSize() {
			Assert.assertEquals(17, da.size());
		}
		
		@Test public void testIterator(){
			Iterator<Double> iter = da.iterator();
			int idx = 0;
			while(iter.hasNext()){
				Assert.assertEquals(new Double(idx), iter.next());
				++idx;
			}
		}
		
		@Test public void testGetSet(){
			Assert.assertEquals(0d, da.get(0), 0d);
			Assert.assertEquals(3d, da.get(3), 0d);
			Assert.assertEquals(16d, da.get(16), 0d);
			
			da.set(1, -1d);
			Assert.assertEquals(-1d, da.get(1), 0d);
		}
		
		@Test public void testSort(){
			Assert.assertEquals(8d, MathTool.median(da), 0d);
			
			da.set(11, -11d);
			da.sort();
			
			Assert.assertEquals(-11d, da.get(0), 0d);
			Assert.assertEquals(10d, da.get(11), 0d);
			
			Assert.assertEquals(7d, MathTool.median(da), 0d);
		}
    }
}
