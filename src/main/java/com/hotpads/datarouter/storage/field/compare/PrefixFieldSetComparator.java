package com.hotpads.datarouter.storage.field.compare;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

/*
 * HBaseEntityReaderNode may return results beyond our endKey.  This happens when we have a multi-Entity scan that stops
 * in the middle of the last Entity
 */
public class PrefixFieldSetComparator{
	
	public static <FS extends FieldSet<?>> List<FS> filterOnEndOfRange(FS endOfRange, boolean inclusive, 
			Iterable<FS> candidates){
		List<FS> matches = ListTool.createArrayList();
		for(FS candidate : IterableTool.nullSafe(candidates)){
			if(isCandidateIncludedForEndOfRange(endOfRange, inclusive, candidate)){
				matches.add(candidate);
			}
		}
		return matches;
	}

	
	public static boolean isCandidateIncludedForEndOfRange(FieldSet<?> endOfRange, boolean inclusive, 
			FieldSet<?> candidate){
		if(endOfRange == null){ return true; }
		if(ObjectTool.noNulls(endOfRange, candidate) && ClassTool.differentClass(endOfRange, candidate)){
			throw new IllegalArgumentException("currently expecting same class");//should subclasses be ok?
		}
		//if we got past the class checks above, then fields should be the same and arrive in the same order
		return isCandidateIncludedForEndOfRange(endOfRange.getFields(), candidate.getFields(), inclusive);
	}
	
	
	private static boolean isCandidateIncludedForEndOfRange(List<Field<?>> endOfRangeFields, 
			List<Field<?>> candidateFields, boolean inclusive){
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
			Assert.assertFalse(isCandidateIncludedForEndOfRange(endOfRange1, true, candidate1));
		}

		@Test
		public void testCloseCall(){
			//the candidate would normally compare after the endOfRange, but should be included here
			SortedBeanKey candidate2 = new SortedBeanKey("emu", "zzz", 55, "zzz");
			Assert.assertTrue(candidate2.compareTo(endOfRange1) > 0);//candidate is after end of range with normal comparison
			Assert.assertTrue(isCandidateIncludedForEndOfRange(endOfRange1, true, candidate2));//but in the prefix range
			Assert.assertTrue(isCandidateIncludedForEndOfRange(endOfRange1, false, candidate2));//even with inclusive=false
		}

		@Test
		public void testInclusiveExclusive(){
			//the candidate would normally compare after the endOfRange, but should be included here
			SortedBeanKey candidate3 = new SortedBeanKey("emu", null, null, null);
			Assert.assertTrue(candidate3.compareTo(endOfRange1) == 0);//candidate is after end of range with normal comparison
			Assert.assertTrue(isCandidateIncludedForEndOfRange(endOfRange1, true, candidate3));//but in the prefix range
			Assert.assertFalse(isCandidateIncludedForEndOfRange(endOfRange1, false, candidate3));//even with inclusive=false
		}
	}
}
