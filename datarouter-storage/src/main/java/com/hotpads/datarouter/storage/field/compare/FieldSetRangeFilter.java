package com.hotpads.datarouter.storage.field.compare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.collections.Range;

/*
 * Some scans return extra results that we need to filter out
 */
public class FieldSetRangeFilter{
	
	public static <FS extends FieldSet<?>> List<FS> filter(Iterable<FS> candidates, Range<? extends FS> range){
		List<FS> matches = new ArrayList<>();
		for(FS candidate : DrIterableTool.nullSafe(candidates)){
			if(include(candidate, range)){
				matches.add(candidate);
			}
		}
		return matches;
	}

	
	public static <FS extends FieldSet<?>> boolean include(FieldSet<?> candidate, Range<? extends FS> range){
		boolean matchesStart = true;
		if(range.hasStart()){
			matchesStart = isCandidateAfterStartOfRange(candidate.getFields(), range.getStart().getFields(), range
					.getStartInclusive());
		}
		
		boolean matchesEnd = true;
		if(range.hasEnd()){
			matchesEnd = isCandidateBeforeEndOfRange(candidate.getFields(), range.getEnd().getFields(), range
					.getEndInclusive());
		}
		
		return matchesStart && matchesEnd;
	}
	
	
	private static boolean isCandidateAfterStartOfRange(List<Field<?>> candidateFields, 
			List<Field<?>> startOfRangeFields, boolean inclusive){
		if(startOfRangeFields == null){
			return true;
		}
		if(DrCollectionTool.differentSize(startOfRangeFields, candidateFields)){
			throw new IllegalArgumentException("inputs must have identical field count");
		}
		//field by field comparison
		Iterator<Field<?>> candidateIterator = candidateFields.iterator();
		Iterator<Field<?>> startOfRangeIterator = startOfRangeFields.iterator();
		int counter = 0;
		while(startOfRangeIterator.hasNext()){//they will have the same number of fields
			++counter;
			@SuppressWarnings("rawtypes")
			Field candidate = candidateIterator.next();
			if(candidate.getValue() == null){
				throw new IllegalArgumentException("currently don't support nulls in candidate");
			}
			@SuppressWarnings("rawtypes")
			Field startOfRange = startOfRangeIterator.next();
			if(startOfRange.getValue() == null){
				return true;
			}
			//neither value should be null at this point
			@SuppressWarnings("unchecked")
			int diff = DrComparableTool.nullFirstCompareTo(candidate, startOfRange);
			if(diff > 0){
				return true;
			}
			if(diff < 0){
				return false;
			}
			boolean lastField = counter == startOfRangeFields.size();
			if(lastField){
				return inclusive;
			}
		}
		throw new IllegalStateException("shouldn't get here");
	}
	
	
	private static boolean isCandidateBeforeEndOfRange(List<Field<?>> candidateFields, 
			List<Field<?>> endOfRangeFields, boolean inclusive){
		if(endOfRangeFields == null){
			return true;
		}
		if(DrCollectionTool.differentSize(endOfRangeFields, candidateFields)){
			throw new IllegalArgumentException("inputs must have identical field count");
		}
		//field by field comparison
		Iterator<Field<?>> candidateIterator = candidateFields.iterator();
		Iterator<Field<?>> endOfRangeIterator = endOfRangeFields.iterator();
		int counter = 0;
		while(endOfRangeIterator.hasNext()){//they will have the same number of fields
			++counter;
			@SuppressWarnings("rawtypes")
			Field candidate = candidateIterator.next();
			if(candidate.getValue() == null){
				throw new IllegalArgumentException("currently don't support nulls in candidate");
			}
			@SuppressWarnings("rawtypes")
			Field endOfRange = endOfRangeIterator.next();
			if(endOfRange.getValue() == null){
				return true;
			}
			//neither value should be null at this point
			@SuppressWarnings("unchecked")
			int diff = DrComparableTool.nullFirstCompareTo(candidate, endOfRange);
			if(diff < 0){
				return true;
			}
			if(diff > 0){
				return false;
			}
			boolean lastField = counter == endOfRangeFields.size();
			if(lastField){
				return inclusive;
			}
		}
		throw new IllegalStateException("shouldn't get here");
	}
	
	
	/********************* tests *******************************/
	
	public static class PrefixFieldSetComparatorTests{
		SortedBeanKey endOfRange1 = new SortedBeanKey("emu", null, null, null);
		Range<SortedBeanKey> rangeEndInclusive = new Range<>(null, true, endOfRange1, true);
		Range<SortedBeanKey> rangeEndExclusive = new Range<>(null, true, endOfRange1, false);
		@Test
		public void testObviousFailure(){
			SortedBeanKey candidate1 = new SortedBeanKey("zzz", "zzz", 55, "zzz");
			Assert.assertTrue(candidate1.compareTo(endOfRange1) > 0);//sanity check
			Assert.assertFalse(include(candidate1, rangeEndInclusive));
		}

		@Test
		public void testCloseCall(){
			//the candidate would normally compare after the endOfRange, but should be included here
			SortedBeanKey candidate2 = new SortedBeanKey("emu", "zzz", 55, "zzz");
			Assert.assertTrue(candidate2.compareTo(endOfRange1) > 0);//candidate is after end with normal comparison
			Assert.assertTrue(include(candidate2, rangeEndInclusive));//but in the prefix range
			Assert.assertTrue(include(candidate2, rangeEndExclusive));//even with inclusive=false
		}

		@Test
		public void testInclusiveExclusive(){
			SortedBeanKey endOfRange2 = new SortedBeanKey("emu", "d", 5, "g");
			Range<SortedBeanKey> rangeEnd2Inclusive = new Range<>(null, true, endOfRange2, true);
			Range<SortedBeanKey> rangeEnd2Exclusive = new Range<>(null, true, endOfRange2, false);
			//the candidate would normally compare after the endOfRange, but should be included here
			SortedBeanKey candidate3 = new SortedBeanKey("emu", "d", 5, "g");
			Assert.assertTrue(candidate3.compareTo(endOfRange2) == 0);
			Assert.assertTrue(include(candidate3, rangeEnd2Inclusive));//but in the prefix range
			Assert.assertFalse(include(candidate3, rangeEnd2Exclusive));//even with inclusive=false
		}
	}
}
