/*
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
package io.datarouter.util.tuple;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

import io.datarouter.util.lang.ObjectTool;

/* * null compares first
 * * startInclusive defaults to true
 * * endExclusive defaults to false
 */
public class Range<T>
implements Comparable<Range<T>>{

	private static final Comparator<?> DEFAULT_COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());

	/*------------------------- fields --------------------------------------*/

	private final Comparator<T> comparator;
	private T start;
	private boolean startInclusive;
	private T end;
	private boolean endInclusive;

	/*------------------------- constructors --------------------------------*/

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
		this(defaultComparator(), start, startInclusive, end, endInclusive);
	}

	public Range(Comparator<T> comparator, T start, boolean startInclusive, T end, boolean endInclusive){
		this.comparator = comparator;
		this.start = start;
		this.startInclusive = startInclusive;
		this.end = end;
		this.endInclusive = endInclusive;
	}


	/*------------------------- static constructors -------------------------*/

	@SuppressWarnings("unchecked")
	public static <T> Comparator<T> defaultComparator(){
		return (Comparator<T>)DEFAULT_COMPARATOR;
	}

	public static <T> Range<T> nullSafe(Range<T> in){
		if(in != null){
			return in;
		}
		return everything();
	}

	public static <T> Range<T> everything(){
		return new Range<>(null, true);
	}

	/*------------------------- methods -------------------------------------*/

	public boolean isValid(){
		if(ObjectTool.anyNull(start, end)){
			return true;
		}
		return comparator.compare(start, end) <= 0;
	}

	public Range<T> assertValid(){
		if(!isValid()){
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
		return Objects.equals(start, end);
	}

	public boolean matchesStart(T item){
		if(!hasStart()){
			return true;
		}
		int diff = comparator.compare(item, start);
		return startInclusive ? diff >= 0 : diff > 0;
	}

	public boolean matchesEnd(T item){
		if(!hasEnd()){
			return true;
		}
		int diff = comparator.compare(item, end);
		return endInclusive ? diff <= 0 : diff < 0;
	}

	public boolean contains(T item){
		return matchesStart(item) && matchesEnd(item);
	}

	public boolean isEmpty(){
		return equalsStartEnd() && start != null && !(startInclusive && endInclusive);
	}

	public boolean notEmpty(){
		return !isEmpty();
	}

	@Override
	public Range<T> clone(){
		return new Range<>(start, startInclusive, end, endInclusive);
	}

	public <R extends Comparable<? super R>> Range<R> map(Function<? super T, ? extends R> mapper){
		R newStart = start == null ? null : mapper.apply(start);
		R newEnd = end == null ? null : mapper.apply(end);
		return new Range<>(newStart, startInclusive, newEnd, endInclusive);
	}

	/*------------------------- standard ------------------------------------*/

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
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		Range<?> other = (Range<?>)obj;
		if(end == null){
			if(other.end != null){
				return false;
			}
		}else if(!end.equals(other.end)){
			return false;
		}
		if(endInclusive != other.endInclusive){
			return false;
		}
		if(start == null){
			if(other.start != null){
				return false;
			}
		}else if(!start.equals(other.start)){
			return false;
		}
		if(startInclusive != other.startInclusive){
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
		if(this == that){
			return 0;
		}
		int diff = comparator.compare(start, that.start);
		if(diff != 0){
			return diff;
		}
		if(startInclusive){
			return that.startInclusive ? 0 : -1;
		}
		return that.startInclusive ? 1 : 0;
	}

	@Override
	public String toString(){
		var sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + ":");
		sb.append(startInclusive ? "[" : "(");
		sb.append(start + "," + end);
		sb.append(endInclusive ? "]" : ")");
		return sb.toString();
	}

	/*------------------------- get/set -------------------------------------*/

	public T getStart(){
		return start;
	}

	public Range<T> setStart(T start){
		this.start = start;
		return this;
	}

	public boolean getStartInclusive(){
		return startInclusive;
	}

	public Range<T> setStartInclusive(boolean startInclusive){
		this.startInclusive = startInclusive;
		return this;
	}

	public T getEnd(){
		return end;
	}

	public Range<T> setEnd(T end){
		this.end = end;
		return this;
	}

	public boolean getEndInclusive(){
		return endInclusive;
	}

	public Range<T> setEndInclusive(boolean endInclusive){
		this.endInclusive = endInclusive;
		return this;
	}

}
