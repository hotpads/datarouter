package com.hotpads.util.core.collections;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
public class Triple<A,B,C> implements Serializable{
	protected A first;
	protected B second;
	protected C third;

	public Triple() { }

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
