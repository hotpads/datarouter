package com.hotpads.util.core.collections;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Objects;
import com.hotpads.datarouter.util.core.DrObjectTool;


@SuppressWarnings("serial")
public class Pair<L,R> implements Serializable{

	protected L left;
	protected R right;

	public Pair(L left, R right){
		this.left=left;
		this.right=right;
	}

	public Pair(){}

	@Deprecated //use new Pair<>(..)
	public static <T,V> Pair<T,V> create(T left,V right) {
		return new Pair<>(left,right);
	}

	public static <T,V> Pair<T,V> create(){
		return create(null,null);
	}

	public boolean areLeftAndRightEqual(){
		return java.util.Objects.equals(left, right);
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

	@Override
	public String toString(){
		return "(" + left.toString() + ", " + right.toString() + ")";
	}

	@Override
	public boolean equals(Object other){
		if(other == null || ! (other instanceof Pair)){
			return false;
		}
		Pair<?,?> pair = (Pair<?,?>)other;

		return ((pair.getLeft()!=null && pair.getLeft().equals(getLeft()))
					|| (pair.getLeft() == null && getLeft() == null))
				&& ((pair.getRight()!=null && pair.getRight().equals(getRight()))
						|| (pair.getRight() == null && getRight() == null));
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(left, right);
	}

	public static class Tests {
		@Test
		public void testEquals(){
			Assert.assertFalse(Pair.create(null,null).equals("a string"));
			Assert.assertFalse(Pair.create("a","a").equals("a"));
			Assert.assertFalse(Pair.create(null,"a").equals("a"));
			Assert.assertFalse(Pair.create("a",null).equals("a"));

			Assert.assertTrue(Pair.create(null, null)
					.equals(Pair.create(null, null)));
			Assert.assertTrue(Pair.create(null, "a")
					.equals(Pair.create(null, "a")));
			Assert.assertTrue(Pair.create("a", null)
					.equals(Pair.create("a", null)));
			Assert.assertTrue(Pair.create("a", "a")
					.equals(Pair.create("a", "a")));
			Assert.assertTrue(Pair.create("a", "b")
					.equals(Pair.create("a", "b")));

			Assert.assertFalse(Pair.create("a", "b")
					.equals(Pair.create("c", "b")));
			Assert.assertFalse(Pair.create("a", "b")
					.equals(Pair.create("a", "c")));
			Assert.assertFalse(Pair.create("a", "b")
					.equals(Pair.create("a", "a")));
			Assert.assertFalse(Pair.create("a", "b")
					.equals(Pair.create("b", "b")));
		}
	}

}
