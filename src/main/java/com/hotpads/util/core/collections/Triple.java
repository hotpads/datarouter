package com.hotpads.util.core.collections;

import java.io.Serializable;



public class Triple<A, B, C> implements Serializable {
	protected A first;
	protected B second;
	protected C third;
	
	public Triple() { }
	public Triple(A first, B second, C third) {
		this.first = first;
		this.second= second;
		this.third = third;
	}
	
	public static <A,B,C> Triple<A, B, C> create(A a, B b, C c) {
		return new Triple<A,B,C>(a,b,c);
	}
	public static <A,B,C> Triple<A,B,C> create(){
		return new Triple<A,B,C>();
	}
	
	public A getFirst() {
		return first;
	}
	public void setFirst(A first) {
		this.first = first;
	}
	public B getSecond() {
		return second;
	}
	public void setSecond(B second) {
		this.second = second;
	}
	public C getThird() {
		return third;
	}
	public void setThird(C third) {
		this.third = third;
	}
	
	public boolean getIsComplete(){
		return first!=null&&second!=null&&third!=null;
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof Triple)) return false;
		Triple t = (Triple)o;
		return(((first!=null && first.equals(t.getFirst())) 
						|| (first==null && t.getFirst()==null))
				&& ((second!=null && second.equals(t.getSecond())) 
						|| (second==null && t.getSecond()==null))
				&& ((third!=null && third.equals(t.getThird())) 
						|| (third==null && t.getThird()==null)));
	}
		
}
