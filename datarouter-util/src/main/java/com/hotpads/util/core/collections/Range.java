package com.hotpads.util.core.collections;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrObjectTool;

/* * null compares first
 * * startInclusive defaults to true
 * * endExclusive defaults to false
 */
public class Range<T extends Comparable<? super T>> implements Comparable<Range<T>>{

	/** fields ****************************************************/

	private T start;
	private boolean startInclusive;
	private T end;
	private boolean endInclusive;


	/** constructors ****************************************************/

	public Range(T start){
		this(start, true, null, false);
	}

	public Range(T start, boolean startInclusive){
		this(start, startInclusive, null, false);
	}

	public Range(T start, T end){
		this(start, true, end, false);
	}

	public Range(T start, boolean startInclusive, T end, boolean endInclusive){
		this.start = start;
		this.startInclusive = startInclusive;
		this.end = end;
		this.endInclusive = endInclusive;
	}


	/** static constructors **********************************************/

	public static <T extends Comparable<? super T>> Range<T> nullSafe(Range<T> in){
		if(in != null) {
			return in;
		}
		return everything();
	}

	public static <T extends Comparable<? super T>> Range<T> everything(){
		return new Range<>(null, true);
	}

	public static <T extends Comparable<? super T>> Range<T> from(T start){
		return new Range<>(start, true);
	}

	public static <T extends Comparable<? super T>> Range<T> create(T start, T end){
		return new Range<>(start, end);
	}

	public static <T extends Comparable<? super T>> Range<T> create(T start, boolean startInclusive, T end,
			boolean endInclusive){
		return new Range<>(start, startInclusive, end, endInclusive);
	}


	/** methods ******************************************************/

	public Range<T> assertValid(){
		if(DrObjectTool.anyNull(start, end)){
			return this;
		}
		if(start.compareTo(end) > 0){
			throw new IllegalStateException("start is after end for " + this);
		}
		return this;
	}

	public boolean isEmptyStart(){
		return start == null;
	}

	public boolean hasStart(){
		return start != null;
	}

	public boolean isEmptyEnd(){
		return end == null;
	}

	public boolean hasEnd(){
		return end != null;
	}

	public boolean equalsStartEnd(){
		return DrObjectTool.equals(start, end);
	}

	public boolean contains(T item){
		return DrComparableTool.between(start, startInclusive, item, end, endInclusive);
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


	/** standard ********************************************************/

	//auto-gen
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (end == null ? 0 : end.hashCode());
		result = prime * result + (endInclusive ? 1231 : 1237);
		result = prime * result + (start == null ? 0 : start.hashCode());
		result = prime * result + (startInclusive ? 1231 : 1237);
		return result;
	}

	//auto-gen
	@Override
	public boolean equals(Object obj){
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		Range<?> other = (Range<?>)obj;
		if(end == null){
			if(other.end != null) {
				return false;
			}
		}else if(!end.equals(other.end)) {
			return false;
		}
		if(endInclusive != other.endInclusive) {
			return false;
		}
		if(start == null){
			if(other.start != null) {
				return false;
			}
		}else if(!start.equals(other.start)) {
			return false;
		}
		if(startInclusive != other.startInclusive) {
			return false;
		}
		return true;
	}

	/*
	 * currently only compares start values, but end values could make sense
	 *
	 * null comes before any value
	 */
	@Override
	public int compareTo(Range<T> that){
		return compareStarts(this, that);
	}

	public static <T extends Comparable<? super T>> int compareStarts(Range<T> itemA, Range<T> itemB){
		if(itemA == itemB) {
			return 0;
		}
		int diff = DrComparableTool.nullFirstCompareTo(itemA.start, itemB.start);
		if(diff != 0) {
			return diff;
		}
		if(itemA.startInclusive){
			return itemB.startInclusive ? 0 : -1;
		}
		return itemB.startInclusive ? 1 : 0;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + ":");
		sb.append(startInclusive ? "[" : "(");
		sb.append(start + "," + end);
		sb.append(endInclusive ? "]" : ")");
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
		@Test
		public void testCompareStarts(){
			Range<Integer> rangeA = Range.create(null, true, null, true);
			Assert.assertEquals(0, compareStarts(rangeA, rangeA));
			Range<Integer> rangeB = Range.create(null, false, null, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeA, rangeB));
			Range<Integer> rangeC = Range.create(null, true, 999, true);
			Assert.assertEquals(0, DrComparableTool.compareAndAssertReflexive(rangeA, rangeC));
			Range<Integer> rangeD = Range.create(3, true, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeA, rangeD));
			Range<Integer> rangeE = Range.create(3, false, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeA, rangeD));
			Range<Integer> rangeF = Range.create(4, true, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeD, rangeF));
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeE, rangeF));
			Range<Integer> rangeG = Range.create(4, false, 999, true);
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeD, rangeG));
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeE, rangeG));
			Assert.assertEquals(-1, DrComparableTool.compareAndAssertReflexive(rangeF, rangeG));
		}
		@Test
		public void testValidAssert(){
			new Range<>(null, null).assertValid();
			new Range<>(0, null).assertValid();
			new Range<>(null, 0).assertValid();
			new Range<>(0, 1).assertValid();
		}
		@Test(expectedExceptions = IllegalStateException.class)
		public void testInvalidAssert(){
			new Range<>(1, 0).assertValid();
		}
	}

}
