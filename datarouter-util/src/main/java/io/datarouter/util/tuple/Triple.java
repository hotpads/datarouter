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
package io.datarouter.util.tuple;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
public class Triple<A,B,C> implements Serializable{

	protected A first;
	protected B second;
	protected C third;

	public Triple(){
	}

	public Triple(A first, B second, C third){
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public static <A,B,C> Triple<A,B,C> create(A first, B second, C third){
		return new Triple<>(first, second, third);
	}

	public A getFirst(){
		return first;
	}

	public B getSecond(){
		return second;
	}

	public C getThird(){
		return third;
	}

	@Override
	public boolean equals(Object other){
		if(!(other instanceof Triple)){
			return false;
		}
		Triple<?,?,?> otherTriple = (Triple<?,?,?>)other;
		return (first != null && first.equals(otherTriple.getFirst())
				|| first == null && otherTriple.getFirst() == null)
		&& (second != null && second.equals(otherTriple.getSecond())
				|| second == null && otherTriple.getSecond() == null)
		&& (third != null && third.equals(otherTriple.getThird())
				|| third == null && otherTriple.getThird() == null);
	}

	@Override
	public int hashCode(){
		return Objects.hash(first, second, third);
	}

}
