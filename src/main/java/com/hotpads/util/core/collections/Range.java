package com.hotpads.util.core.collections;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;

/* * null compares first
 * * startInclusive defaults to true
 * * endExclusive defaults to false
 */
public class Range<T extends Comparable<? super T>> implements Comparable<Range<T>>{
	
	/** fields ****************************************************/

	protected T start;
	protected boolean startInclusive = true;
	protected T end;
	protected boolean endInclusive = false;

	
	/** constructors ****************************************************/
	
	public Range(T start){
		this.start = start;
	}
	
	public Range(T start, boolean startInclusive){
		this.start = start;
		this.startInclusive = startInclusive;
	}
	
	public Range(T start, T end){
		this.start = start;
		this.end = end;
	}
	
	public Range(T start, boolean startInclusive, T end, boolean endInclusive){
		this.start = start;
		this.startInclusive = startInclusive;
		this.end = end;
		this.endInclusive = endInclusive;
	}
	
	
	/** static constructors **********************************************/

	public static <T extends Comparable<? super T>> Range<T> nullSafe(Range<T> in){
		if(in != null){ return in; }
		return everything();
	}
	
	public static <T extends Comparable<? super T>> Range<T> everything(){
		return new Range<T>(null, true);
	}
	
	public static <T extends Comparable<? super T>> Range<T> create(T start){
		return new Range<T>(start);
	}
	
	public static <T extends Comparable<? super T>> Range<T> create(T start, boolean startInclusive){
		return new Range<T>(start, startInclusive);
	}
	
	public static <T extends Comparable<? super T>> Range<T> create(T start, T end){
		return new Range<T>(start, end);
	}
	
	public static <T extends Comparable<? super T>> Range<T> create(T start, boolean startInclusive, T end, 
			boolean endInclusive){
		return new Range<T>(start, startInclusive, end, endInclusive);
	}
	
	
	/** methods ******************************************************/
	
	public boolean isEmptyStart(){
		return start==null;
	}
	
	public boolean hasStart(){
		return start!=null;
	}
	
	public boolean isEmptyEnd(){
		return end==null;
	}
	
	public boolean hasEnd(){
		return end!=null;
	}
	
	public boolean equalsStartEnd(){
		return DrObjectTool.equals(start, end);
	}
	
	public boolean contains(T t){
		return DrComparableTool.between(start, startInclusive, t, end, endInclusive);
	}
	
	public ArrayList<T> filter(Iterable<T> ins){
		ArrayList<T> outs = new ArrayList<>();
		for(T in : DrIterableTool.nullSafe(ins)){
			if(contains(in)){
				outs.add(in);
			}
		}
		return outs;
	}
	
//	public boolean anyOverlap(Range<T> other){
//		
//	}
//
//	public boolean fullyContains(Range<T> other){
//		
//	}
	
	
	/** standard ********************************************************/

	//auto-gen
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + (endInclusive ? 1231 : 1237);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + (startInclusive ? 1231 : 1237);
		return result;
	}

	//auto-gen
	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Range<?> other = (Range<?>)obj;
		if(end == null){
			if(other.end != null) return false;
		}else if(!end.equals(other.end)) return false;
		if(endInclusive != other.endInclusive) return false;
		if(start == null){
			if(other.start != null) return false;
		}else if(!start.equals(other.start)) return false;
		if(startInclusive != other.startInclusive) return false;
		return true;
	}
	
	/*
	 * currently only compares start values, but end values could make sense
	 * 
	 * null comes before any value
	 */
	public int compareTo(Range<T> that){
		return compareStarts(this, that);
	}
	
	public static <T extends Comparable<? super T>> int compareStarts(Range<T> a, Range<T> b){
		if(a==b){ return 0; }
		int c = DrComparableTool.nullFirstCompareTo(a.start, b.start);
		if(c!=0){ return c; }
		if(a.startInclusive){ 
			return b.startInclusive ? 0 : -1;
		}
		return b.startInclusive ? 1 : 0;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()+":");
		sb.append(startInclusive?"[":"(");
		sb.append(start+","+end);
		sb.append(endInclusive?"]":")");
		return sb.toString();
	}
	
	

	/** get/set ****************************************************/

	public T getStart(){
		return start;
	}

	public void setStart(T start){
		this.start = start;
	}

	public boolean getStartInclusive(){
		return startInclusive;
	}

	public void setStartInclusive(boolean startInclusive){
		this.startInclusive = startInclusive;
	}

	public T getEnd(){
		return end;
	}

	public void setEnd(T end){
		this.end = end;
	}

	public boolean getEndInclusive(){
		return endInclusive;
	}

	public void setEndInclusive(boolean endInclusive){
		this.endInclusive = endInclusive;
	}
	
	
	/** tests *******************************************************/
	
	public static class RangeTests{
		@Test public void testCompareStarts(){
			Range<Integer> a = Range.create(null, true, null, true);
			Assert.assertEquals(0, compareStarts(a, a));
			Range<Integer> b = Range.create(null, false, null, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(a, b));
			Range<Integer> c = Range.create(null, true, 999, true);
			Assert.assertEquals(0, DrComparableTool.compareAndAssertReflexive(a, c));
			Range<Integer> d = Range.create(3, true, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(a, d));
			Range<Integer> e = Range.create(3, false, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(a, d));
			Range<Integer> f = Range.create(4, true, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(d, f));
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(e, f));
			Range<Integer> g = Range.create(4, false, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(d, g));
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(e, g));
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(f, g));
		}
	}
	
}
