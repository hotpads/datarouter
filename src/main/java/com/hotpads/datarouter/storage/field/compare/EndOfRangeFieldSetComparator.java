package com.hotpads.datarouter.storage.field.compare;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.util.core.ClassTool;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.ComparableTool;
import com.hotpads.datarouter.util.core.IterableTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.ObjectTool;

/*
 * HBaseEntityReaderNode may return results beyond our endKey.  This happens when we have a multi-Entity scan that stops
 * in the middle of the last Entity
 */
public class EndOfRangeFieldSetComparator<FS extends FieldSet<?>>
implements Comparator<FS>{
	
	private boolean inclusive;
	
	
	public EndOfRangeFieldSetComparator(boolean inclusive){
		this.inclusive = inclusive;
	}

	@Override//warning: not tested or used yet
	public int compare(FS candidate, FS endOfRange){
		return isCandidateIncludedForEndOfRange(candidate, endOfRange, inclusive) ? -1 : 1;
	}
	
	
	/********************* static **************************/
	
	public static <FS extends FieldSet<?>> List<FS> filterOnEndOfRange(Iterable<FS> candidates, FS endOfRange, 
			boolean inclusive){
		List<FS> matches = ListTool.createArrayList();
		for(FS candidate : IterableTool.nullSafe(candidates)){
			if(isCandidateIncludedForEndOfRange(candidate, endOfRange, inclusive)){
				matches.add(candidate);
			}
		}
		return matches;
	}

	
	public static boolean isCandidateIncludedForEndOfRange(FieldSet<?> candidate, FieldSet<?> endOfRange, 
			boolean inclusive){
		if(endOfRange == null){ return true; }
		if(ObjectTool.noNulls(endOfRange, candidate) && ClassTool.differentClass(endOfRange, candidate)){
			throw new IllegalArgumentException("currently expecting same class");//should subclasses be ok?
		}
		//if we got past the class checks above, then fields should be the same and arrive in the same order
		return isCandidateIncludedForEndOfRange(candidate.getFields(), endOfRange.getFields(), inclusive);
	}
	
	
	private static boolean isCandidateIncludedForEndOfRange(List<Field<?>> candidateFields, 
			List<Field<?>> endOfRangeFields, boolean inclusive){
		if(endOfRangeFields == null){ return true; }
		if(CollectionTool.differentSize(endOfRangeFields, candidateFields)){
			throw new IllegalArgumentException("inputs must have identical field count");
		}
		//field by field comparison
		Iterator<Field<?>> candidateIterator = candidateFields.iterator();
		Iterator<Field<?>> endOfRangeIterator = endOfRangeFields.iterator();
		int counter = 0;
		while(endOfRangeIterator.hasNext()){//they will have the same number of fields
			++counter;
			//be sure to compare the values, not the container field which won't be null
			//assume values are comparable since this should only be called on key fields
			Comparable candidateValue = (Comparable)candidateIterator.next().getValue();
			if(candidateValue == null){
				throw new IllegalArgumentException("currently don't support nulls in candidate");
			}
			Comparable endOfRangeValue = (Comparable)endOfRangeIterator.next().getValue();
			if(endOfRangeValue == null){
				return true;
			}else{
				//neither value should be null at this point
				int diff = ComparableTool.nullFirstCompareTo(candidateValue, endOfRangeValue);
				if(diff < 0){ return true; }
				if(diff > 0){ return false; }
				boolean lastField = counter == endOfRangeFields.size();
				if(diff == 0 && lastField){
					return inclusive;
				}
			}
		}
		throw new IllegalStateException("shouldn't get here");
	}
	
	
	/********************* tests *******************************/
	
	public static class PrefixFieldSetComparatorTests{
		SortedBeanKey endOfRange1 = new SortedBeanKey("emu", null, null, null);
		@Test
		public void testObviousFailure(){
			SortedBeanKey candidate1 = new SortedBeanKey("zzz", "zzz", 55, "zzz");
			Assert.assertTrue(candidate1.compareTo(endOfRange1) > 0);//sanity check
			Assert.assertFalse(isCandidateIncludedForEndOfRange(candidate1, endOfRange1, true));
		}

		@Test
		public void testCloseCall(){
			//the candidate would normally compare after the endOfRange, but should be included here
			SortedBeanKey candidate2 = new SortedBeanKey("emu", "zzz", 55, "zzz");
			Assert.assertTrue(candidate2.compareTo(endOfRange1) > 0);//candidate is after end of range with normal comparison
			Assert.assertTrue(isCandidateIncludedForEndOfRange(candidate2, endOfRange1, true));//but in the prefix range
			Assert.assertTrue(isCandidateIncludedForEndOfRange(candidate2, endOfRange1, false));//even with inclusive=false
		}

		@Test
		public void testInclusiveExclusive(){
			SortedBeanKey endOfRange2 = new SortedBeanKey("emu", "d", 5, "g");
			//the candidate would normally compare after the endOfRange, but should be included here
			SortedBeanKey candidate3 = new SortedBeanKey("emu", "d", 5, "g");
			Assert.assertTrue(candidate3.compareTo(endOfRange2) == 0);
			Assert.assertTrue(isCandidateIncludedForEndOfRange(candidate3, endOfRange2, true));//but in the prefix range
			Assert.assertFalse(isCandidateIncludedForEndOfRange(candidate3, endOfRange2, false));//even with inclusive=false
		}
	}
}
