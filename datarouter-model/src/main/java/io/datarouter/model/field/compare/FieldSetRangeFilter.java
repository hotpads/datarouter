/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.field.compare;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.tuple.Range;

/*
 * Some scans return extra results that we need to filter out
 */
public class FieldSetRangeFilter{

	public static <FS extends FieldSet<?>> boolean include(
			FieldSet<?> candidate,
			Range<? extends FS> range,
			String nodeName){
		boolean matchesStart = true;
		if(range.hasStart()){
			matchesStart = isCandidateAfterStartOfRange(
					candidate.getFields(),
					range.getStart().getFields(),
					range.getStartInclusive(),
					nodeName);
		}

		boolean matchesEnd = true;
		if(range.hasEnd()){
			matchesEnd = isCandidateBeforeEndOfRange(
					candidate.getFields(),
					range.getEnd().getFields(),
					range.getEndInclusive());
		}

		return matchesStart && matchesEnd;
	}

	//is this any better than range.matchesStart(candidate)?
	public static boolean isCandidateAfterStartOfRange(
			List<Field<?>> candidateFields,
			List<Field<?>> startOfRangeFields,
			boolean inclusive,
			String nodeName){
		if(startOfRangeFields == null){
			return true;
		}
		if(startOfRangeFields.size() != candidateFields.size()){
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
				throw new IllegalArgumentException("currently don't support nulls in node=" + nodeName + " candidate="
						+ candidateFields);
			}
			@SuppressWarnings("rawtypes")
			Field startOfRange = startOfRangeIterator.next();
			if(startOfRange.getValue() == null){
				return inclusive;
			}
			//neither value should be null at this point
			@SuppressWarnings("unchecked")
			int diff = ComparableTool.nullFirstCompareTo(candidate, startOfRange);
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


	public static boolean isCandidateBeforeEndOfRange(
			List<Field<?>> candidateFields,
			List<Field<?>> endOfRangeFields,
			boolean inclusive){
		if(endOfRangeFields == null){
			return true;
		}
		if(endOfRangeFields.size() != candidateFields.size()){
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
			Objects.requireNonNull(candidate.getValue(), "currently don't support nulls, candidate=" + candidateFields);
			@SuppressWarnings("rawtypes")
			Field endOfRange = endOfRangeIterator.next();
			if(endOfRange.getValue() == null){
				return inclusive;
			}
			//neither value should be null at this point
			@SuppressWarnings("unchecked")
			int diff = ComparableTool.nullFirstCompareTo(candidate, endOfRange);
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

}
