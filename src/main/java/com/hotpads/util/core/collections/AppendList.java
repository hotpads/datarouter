package com.hotpads.util.core.collections;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.Functor;
import com.hotpads.util.core.Predicate;
import com.hotpads.util.core.StringTool;

public class AppendList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 2075786671095320178L;

	//factory methods
	public static <K> AppendList<K> create(Collection<? extends K> c) {
		return new AppendList<K>(c);
	}
	public static <K> AppendList<K> create() {
		return new AppendList<K>();
	}
	public static <K> AppendList<K> create(K... ks){
		if(ks==null || ks.length<1) return create();
		return new AppendList<K>(Arrays.asList(ks));
	}
	
	public AppendList(Collection<? extends T> c){
		super(c);
	}
	public AppendList(){
		super();
	}
	
	public AppendList(int i) {
		super(i);
	}
	
	@SuppressWarnings("unchecked")
	public AppendList<T> addLongs(String csv, String delimiter){
		String[] stuffs = csv.split(delimiter);
		for(String s:stuffs){
			if(StringTool.isEmpty(s)) continue;
			this.add((T)new Long(s.trim()));
		}
		return this;
	}
	@SuppressWarnings("unchecked")
	public AppendList<T> addIntegers(String csv, String delimiter){
		String[] stuffs = csv.split(delimiter);
		for(String s:stuffs){
			this.add((T)new Integer(s.trim()));
		}
		return this;
	}
	@SuppressWarnings("unchecked")
	public AppendList<T> addStrings(String csv, String delimiter){
		String[] stuffs = csv.split(delimiter);
		for(String s:stuffs){
			this.add((T)s);
		}
		return this;
	}
	
	public String toString(){
		return toString("");
	}
	public String toString(String delimiter){
		return toString(delimiter, "");
	}
	
	public String toString(String delimiter, String surroundWith){
		return toString(delimiter, surroundWith, null);
	}

	public String toString(
			String delimiter, 
			String surroundWith, 
			String delimiterBeforeLastElement) {
		return toString(null, delimiter, surroundWith, 
						      delimiter, surroundWith, 
						      delimiterBeforeLastElement);
	}
	
	public String toString(
			Predicate<T> predicate, 
			String delimiterTrue, 
			String delimiterFalse) {
		return toString(predicate, delimiterTrue, "", delimiterFalse, "", null);
	}
	public String toString(
			Predicate<T> predicate, 
			String delimiterTrue, 
			String surroundWithTrue, 
			String delimiterFalse, 
			String surroundWithFalse) {
		return toString(predicate, delimiterTrue, surroundWithTrue, 
							   delimiterFalse, surroundWithFalse, null);
	}
	/**
	 * @param func(list item, index of list item, list size)
	 */
	public String toString(Functor<String, Triple<T, Integer, Integer>> func) {
		StringBuilder sb = new StringBuilder();
		int i=0;
		for(T t:this){
			sb.append(
					func.invoke(
							new Triple<T, Integer, Integer>(t, i, size())));
			i++;
		}
		return sb.toString();
	}
	
	public String toString(Functor<String,T> func, String delimiter){
		return toString(func,delimiter,null,null);
	}
	public String toString(
			Functor<String, T> func, 
			String delimiter, 
			String surroundWith, 
			String delimiterBeforeLastElement){
		StringBuilder sb = new StringBuilder();
		int i=0;
		for(T t:this){
			sb.append(
					makeStr(surroundWith,
							func.invoke(t),
							delimiter,
							delimiterBeforeLastElement,
							i,
							size()));
			i++;
		}
		return sb.toString();
	}
	
	public String toString(
			Predicate<T> predicate, 
			String delimiterTrue, 
			String surroundWithTrue,
			String delimiterFalse,
			String surroundWithFalse,
			String delimiterBeforeLastElement) {
		StringBuilder sb = new StringBuilder();
		int i=0, skip=0;
		String delimiter;
		String surroundWith;		
		for(T t:this){
			if(t==null) {
				skip++;
				continue;
			}
			boolean pass = (predicate==null || predicate.check(t));
			surroundWith = (pass?surroundWithTrue:surroundWithFalse);
			delimiter = (pass?delimiterTrue:delimiterFalse);
			sb.append(
					makeStr(surroundWith, 
							t.toString(), 
							delimiter, 
							delimiterBeforeLastElement, 
							i, 
							size()-skip));
			i++;
		}
		return sb.toString();
	}
	
	protected String makeStr(
			String surroundWith, 
			String t, 
			String delimiter, 
			String delimiterBeforeLastElement, 
			int i, 
			int size) {
		if(surroundWith==null)surroundWith="";
		
		String delimiterToUse = "";
		if(i<size-1){
			if(i==size-2 && delimiterBeforeLastElement!=null){
				delimiterToUse = delimiterBeforeLastElement;
			}else{
				delimiterToUse = delimiter;
			}
		}
		
		return surroundWith + t.toString() + surroundWith + delimiterToUse;
	}
	
	/** Tests *****************************************************************/
	public static class Tests {
	
		@Test public void testAppends() {
			AppendList<String> a = new AppendList<String>();
			a.add("a");	a.add("b");	a.add("c");	a.add("d");
			Assert.assertEquals("abcd", a.toString());
			Assert.assertEquals("a,b,c,d", a.toString(","));
			Assert.assertEquals("'a','b','c','d'", a.toString(",", "'"));
			Assert.assertEquals("'a', 'b', 'c' and 'd'", 
					a.toString(", ", "'", " and "));
	
			Predicate<String> predicate = new Predicate<String>() { 
									public boolean check(String t) { 
										return t.equals("a") || t.equals("c");
									}};
			Assert.assertEquals("a,b;c,d", a.toString(predicate, ",", ";"));
			Assert.assertEquals("'a',^b^;'c',^d^",
								a.toString(predicate, ",", "'", ";", "^"));
			Assert.assertEquals("'a',^b^;'c' and ^d^", 
					a.toString(predicate, ",", "'", ";", "^", " and "));
			
			Functor<String, Triple<String,Integer,Integer>> func = 
				new Functor<String,Triple<String,Integer,Integer>>() {
					public String invoke(Triple<String,Integer,Integer> param) {
						String str = param.getFirst();
						int i = param.getSecond();
						int size = param.getThird();
						if (i == 0)
							return "<tr><td>" + str + "</td>";
						else if (i < size-1)
							return "<td>" + str + "</td>";
						else
							return "<td>" + str + "</td></tr>";
					}
				};
			Assert.assertEquals(
					"<tr><td>a</td><td>b</td><td>c</td><td>d</td></tr>", 
					a.toString(func));
		}
		
		@Test public void testAppendNull(){
			AppendList<String> a = AppendList.create("a");
			Assert.assertEquals("a",a.toString());
			a.add(null);
			Assert.assertEquals("a",a.toString());
			a.add(null);
			Assert.assertEquals("a",a.toString());
			a.add("b");
			Assert.assertEquals("ab",a.toString());
			
			Assert.assertEquals("",
					AppendList.create((String)null).toString(" "));
			
			Assert.assertEquals("a", 
					AppendList.create((String)null,"a").toString(" "));
		}
		
		@Test public void testFunctor(){
			AppendList<C> cs = new AppendList<C>();
			cs.add(new C(1));cs.add(new C(2));cs.add(new C(-1));
			String s = cs.toString(new Functor<String,C>(){
				public String invoke(C c){return c.i.toString();}}, ",");
			Assert.assertEquals("1,2,-1",s);
		}
		
		class C {
			public Integer i;
			public C(Integer i){this.i=i;}
		}
	}
}
